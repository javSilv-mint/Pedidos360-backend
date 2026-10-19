package cl.duoc.lab.inventario;

import cl.duoc.lab.config.RabbitNamesProperties;
import cl.duoc.lab.config.RabbitPedidosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class InventarioDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventarioDlqConsumer.class);

    private final String dlq;

    public InventarioDlqConsumer(RabbitNamesProperties names) {
        this.dlq = names.pedidos().inventarioDlq();
    }

    @RabbitListener(queues = "${app.rabbit.pedidos.inventario-dlq}",
            autoStartup = "${app.rabbit.policies.dlq.listener-enabled}",
            containerFactory = RabbitPedidosConfig.RAW_LISTENER_FACTORY)
    public void onMensajeMuerto(Message message) {
        String motivo = "desconocido";
        String colaOrigen = "desconocida";
        String muertes = "?";
        String rutasOriginales = "?";

        // RabbitMQ agrega el header x-death con la historia de por qué murió el mensaje
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        if (xDeath instanceof List<?> lista && !lista.isEmpty()
                && lista.get(0) instanceof Map<?, ?> primera) {
            motivo = String.valueOf(primera.get("reason"));          // rejected | expired | maxlen
            colaOrigen = String.valueOf(primera.get("queue"));
            muertes = String.valueOf(primera.get("count"));
            rutasOriginales = String.valueOf(primera.get("routing-keys"));
        }

        String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8).replaceAll("\\s+", " ");
        if (cuerpo.length() > 200) {
            cuerpo = cuerpo.substring(0, 200) + "...";
        }

        log.error("dominio=inventario evento=alerta_dlq dlq={} colaOrigen={} motivoRabbit={} muertes={} rutasOriginales={} messageId={} cuerpo=\"{}\"",
                dlq, colaOrigen, motivo, muertes, rutasOriginales,
                message.getMessageProperties().getMessageId(), cuerpo);
    }
}