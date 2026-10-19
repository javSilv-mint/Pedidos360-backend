package cl.duoc.lab.notificaciones;

import cl.duoc.lab.pedidos.PedidoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionesConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesConsumer.class);

    @RabbitListener(queues = "${app.rabbit.pedidos.notificaciones-queue}")
    public void onEvento(PedidoEvento evento) {
        log.info("dominio=notificaciones evento={} pedidoId={} email={}",
                evento.tipo(), evento.pedidoId(), evento.emailCliente());
    }
}
