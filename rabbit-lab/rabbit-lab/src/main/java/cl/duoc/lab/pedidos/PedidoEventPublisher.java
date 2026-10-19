package cl.duoc.lab.pedidos;

import cl.duoc.lab.config.RabbitNamesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

import java.time.Instant;

@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitNamesProperties names;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate, RabbitNamesProperties names) {
        this.rabbitTemplate = rabbitTemplate;
        this.names = names;
    }

    public void publicar(Long pedidoId, TipoEvento tipo, String emailCliente) {
        RabbitNamesProperties.Pedidos p = names.pedidos();
        String routingKey = switch (tipo) {
            case CREADO -> p.rkCreado();
            case PAGADO -> p.rkPagado();
            case CANCELADO -> p.rkCancelado();
        };
        PedidoEvento evento = new PedidoEvento(pedidoId, tipo, emailCliente, Instant.now());
            rabbitTemplate.convertAndSend(p.exchange(), routingKey, evento, mensaje -> {
            mensaje.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            return mensaje;
        });
        log.info("evento=pedido_publicado exchange={} routingKey={} pedidoId={}",
                p.exchange(), routingKey, pedidoId);
    }
}