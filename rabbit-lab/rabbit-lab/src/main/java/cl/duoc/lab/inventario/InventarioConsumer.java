package cl.duoc.lab.inventario;

import cl.duoc.lab.config.RabbitNamesProperties;
import cl.duoc.lab.config.RabbitPoliciesProperties;
import cl.duoc.lab.pedidos.PedidoEvento;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import cl.duoc.lab.config.RabbitPedidosConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class InventarioConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventarioConsumer.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final InventarioProcesador procesador;
    private final String cola;
    private final RabbitPoliciesProperties.Retry retry;

    public InventarioConsumer(InventarioProcesador procesador,
                              RabbitNamesProperties names,
                              RabbitPoliciesProperties policies) {
        this.procesador = procesador;
        this.cola = names.pedidos().inventarioQueue();
        this.retry = policies.retry();
    }

    // ackMode MANUAL solo en este listener: hello y notificaciones siguen con ACK automático
    @RabbitListener(queues = "${app.rabbit.pedidos.inventario-queue}",
        containerFactory = RabbitPedidosConfig.RAW_LISTENER_FACTORY,
        ackMode = "MANUAL")
    public void consumir(Message message, Channel channel) throws IOException {

        // 1) Deserializar: si el JSON está roto, no tiene sentido reintentar
        PedidoEvento evento;
        try {
            evento = JSON.readValue(message.getBody(), PedidoEvento.class);
        } catch (RuntimeException e) {
            rechazar(message, channel, "MENSAJE_ILEGIBLE", 0, e);
            return;
        }

        // 2) Procesar con reintentos y backoff exponencial
        int intento = 0;
        long espera = retry.initialIntervalMs();
        while (true) {
            intento++;
            try {
                procesador.procesar(evento, intento);
                break; // éxito: salimos del bucle y hacemos ACK
            } catch (ErrorNoRecuperableException e) {
                rechazar(message, channel, "NO_RECUPERABLE", intento, e);
                return;
            } catch (ErrorRecuperableException e) {
                if (intento >= retry.maxAttempts()) {
                    rechazar(message, channel, "REINTENTOS_AGOTADOS", intento, e);
                    return;
                }
                log.warn("dominio=inventario evento=reintento cola={} intento={}/{} esperaMs={} causa=\"{}\"",
                        cola, intento, retry.maxAttempts(), espera, e.getMessage());
                espera = pausar(espera);
            } catch (RuntimeException e) {
                rechazar(message, channel, "ERROR_INESPERADO", intento, e);
                return;
            }
        }

        // 3) Todo OK: confirmamos
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        log.info("dominio=inventario evento=pedido_procesado cola={} pedidoId={} tipo={} intentos={} messageId={}",
                cola, evento.pedidoId(), evento.tipo(), intento, message.getMessageProperties().getMessageId());
    }

    // NACK sin requeue: RabbitMQ envía el mensaje al DLX configurado en la cola
    private void rechazar(Message message, Channel channel, String motivo, int intentos, Exception causa)
            throws IOException {
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        log.error("dominio=inventario evento=mensaje_rechazado cola={} motivo={} intentos={} messageId={} causa=\"{}\" cuerpo=\"{}\"",
                cola, motivo, intentos, message.getMessageProperties().getMessageId(),
        abreviar(String.valueOf(causa.getMessage())), abreviar(new String(message.getBody(), StandardCharsets.UTF_8)));    }

    private long pausar(long espera) {
        try {
            Thread.sleep(espera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Math.min((long) (espera * retry.multiplier()), retry.maxIntervalMs());
    }

    private String abreviar(String texto) {
        String limpio = texto.replaceAll("\\s+", " ");
        return limpio.length() > 200 ? limpio.substring(0, 200) + "..." : limpio;
    }
}