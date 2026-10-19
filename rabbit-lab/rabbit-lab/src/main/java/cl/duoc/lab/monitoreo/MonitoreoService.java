package cl.duoc.lab.monitoreo;

import cl.duoc.lab.config.RabbitNamesProperties;
import cl.duoc.lab.config.RabbitPoliciesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MonitoreoService {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoService.class);

    private final AmqpAdmin admin;
    private final String colaPrincipal;
    private final String dlq;
    private final int umbral;
    // Recuerda el último estado para loguear solo cuando la alerta se ENCIENDE o se APAGA
    private final AtomicBoolean alertaActiva = new AtomicBoolean(false);

    public MonitoreoService(AmqpAdmin admin, RabbitNamesProperties names, RabbitPoliciesProperties policies) {
        this.admin = admin;
        this.colaPrincipal = names.pedidos().inventarioQueue();
        this.dlq = names.pedidos().inventarioDlq();
        this.umbral = policies.dlq().alertaUmbral();
    }

    public MonitoreoColas estado() {
        ColaEstado principal = consultar(colaPrincipal);
        ColaEstado muerta = consultar(dlq);
        boolean alerta = muerta.mensajes() >= umbral;
        if (alertaActiva.compareAndSet(!alerta, alerta)) {
            if (alerta) {
                log.warn("dominio=inventario evento=alerta_dlq_activada dlq={} mensajes={} umbral={}",
                        dlq, muerta.mensajes(), umbral);
            } else {
                log.info("dominio=inventario evento=alerta_dlq_resuelta dlq={} mensajes={}", dlq, muerta.mensajes());
            }
        }
        return new MonitoreoColas(principal, muerta, alerta, umbral, Instant.now());
    }

    // getQueueInfo hace un queue.declare pasivo: no crea ni modifica la cola, solo la lee
    private ColaEstado consultar(String nombre) {
        QueueInformation info = admin.getQueueInfo(nombre);
        if (info == null) {
            return new ColaEstado(nombre, false, 0, 0);
        }
        return new ColaEstado(nombre, true, info.getMessageCount(), info.getConsumerCount());
    }
}
