package cl.duoc.lab.admin.service;

import cl.duoc.lab.admin.config.RabbitAdminProperties;
import cl.duoc.lab.admin.dto.BindingRequest;
import cl.duoc.lab.admin.dto.ExchangeRequest;
import cl.duoc.lab.admin.dto.NamePolicy;
import cl.duoc.lab.admin.dto.QueueInfoResponse;
import cl.duoc.lab.admin.dto.QueueRequest;
import cl.duoc.lab.admin.exception.AdminOperationException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;
    private final RabbitAdminProperties properties;

    public RabbitAdminService(RabbitAdmin rabbitAdmin, RabbitAdminProperties properties) {
        this.rabbitAdmin = rabbitAdmin;
        this.properties = properties;
    }

    // ===================== COLAS =====================

    public QueueInfoResponse createQueue(QueueRequest request) {
        validateQueueRequest(request);
        if (queueExists(request.name())) {
            throw AdminOperationException.conflict("La cola '" + request.name() + "' ya existe");
        }
        QueueBuilder builder = QueueBuilder.durable(request.name());
        if (request.type() == QueueRequest.Type.QUORUM) {
            builder.quorum();
        }
        if (request.messageTtlMs() != null) {
            builder.ttl(request.messageTtlMs());
        }
        if (request.maxLength() != null) {
            builder.maxLength(request.maxLength());
        }
        if (StringUtils.hasText(request.deadLetterExchange())) {
            builder.deadLetterExchange(request.deadLetterExchange());
        }
        if (StringUtils.hasText(request.deadLetterRoutingKey())) {
            builder.deadLetterRoutingKey(request.deadLetterRoutingKey());
        }
        rabbitAdmin.declareQueue(builder.build());
        log.info("event=admin.queue.created queue={} type={} ttlMs={} maxLength={} dlx={} dlrk={}",
                request.name(), request.type(), request.messageTtlMs(), request.maxLength(),
                request.deadLetterExchange(), request.deadLetterRoutingKey());
        return getQueue(request.name());
    }

    public QueueInfoResponse getQueue(String name) {
        NamePolicy.requireValidName(name);
        QueueInformation info = rabbitAdmin.getQueueInfo(name);
        if (info == null) {
            throw AdminOperationException.notFound("La cola '" + name + "' no existe");
        }
        return new QueueInfoResponse(info.getName(), Math.toIntExact(info.getMessageCount()), info.getConsumerCount());
    }

    public void deleteQueue(String name) {
        NamePolicy.requireValidName(name);
        if (properties.protectedQueues().contains(name)) {
            throw AdminOperationException.conflict(
                    "La cola '" + name + "' es parte de la topología base y está protegida");
        }
        if (!queueExists(name)) {
            throw AdminOperationException.notFound("La cola '" + name + "' no existe");
        }
        rabbitAdmin.deleteQueue(name);
        log.info("event=admin.queue.deleted queue={}", name);
    }

    public int purgeQueue(String name) {
        NamePolicy.requireValidName(name);
        if (!queueExists(name)) {
            throw AdminOperationException.notFound("La cola '" + name + "' no existe");
        }
        int purged = rabbitAdmin.purgeQueue(name);
        log.info("event=admin.queue.purged queue={} purgedMessages={}", name, purged);
        return purged;
    }

    // ===================== EXCHANGES =====================

    public void createExchange(ExchangeRequest request) {
        if (exchangeExists(request.name())) {
            throw AdminOperationException.conflict("El exchange '" + request.name() + "' ya existe");
        }
        Exchange exchange = switch (request.type()) {
            case DIRECT -> new DirectExchange(request.name(), true, false);
            case TOPIC -> new TopicExchange(request.name(), true, false);
            case FANOUT -> new FanoutExchange(request.name(), true, false);
            case HEADERS -> new HeadersExchange(request.name(), true, false);
        };
        rabbitAdmin.declareExchange(exchange);
        log.info("event=admin.exchange.created exchange={} type={}", request.name(), request.type());
    }

    public void deleteExchange(String name) {
        NamePolicy.requireValidName(name);
        if (properties.protectedExchanges().contains(name)) {
            throw AdminOperationException.conflict(
                    "El exchange '" + name + "' es parte de la topología base y está protegido");
        }
        if (!exchangeExists(name)) {
            throw AdminOperationException.notFound("El exchange '" + name + "' no existe");
        }
        rabbitAdmin.deleteExchange(name);
        log.info("event=admin.exchange.deleted exchange={}", name);
    }

    // ===================== BINDINGS =====================

    public void createBinding(BindingRequest request) {
        requireExistingTargets(request);
        rabbitAdmin.declareBinding(toBinding(request));
        log.info("event=admin.binding.created exchange={} queue={} routingKey={}",
                request.exchange(), request.queue(), request.routingKey());
    }

    public void deleteBinding(BindingRequest request) {
        requireExistingTargets(request);
        rabbitAdmin.removeBinding(toBinding(request));
        log.info("event=admin.binding.deleted exchange={} queue={} routingKey={}",
                request.exchange(), request.queue(), request.routingKey());
    }

    // ===================== PRIVADOS =====================

    private void validateQueueRequest(QueueRequest request) {
        if (request.messageTtlMs() != null && request.messageTtlMs() > properties.maxMessageTtlMs()) {
            throw AdminOperationException.badRequest(
                    "messageTtlMs no puede superar " + properties.maxMessageTtlMs() + " ms");
        }
        if (request.maxLength() != null && request.maxLength() > properties.maxQueueLength()) {
            throw AdminOperationException.badRequest(
                    "maxLength no puede superar " + properties.maxQueueLength());
        }
        boolean hasDlx = StringUtils.hasText(request.deadLetterExchange());
        if (StringUtils.hasText(request.deadLetterRoutingKey()) && !hasDlx) {
            throw AdminOperationException.badRequest("deadLetterRoutingKey requiere deadLetterExchange");
        }
        // Si el DLX no existe, RabbitMQ descarta en silencio los mensajes muertos.
        if (hasDlx && !exchangeExists(request.deadLetterExchange())) {
            throw AdminOperationException.badRequest("El dead-letter exchange '"
                    + request.deadLetterExchange() + "' no existe; créelo antes con POST /exchanges");
        }
    }

    private void requireExistingTargets(BindingRequest request) {
        if (!exchangeExists(request.exchange())) {
            throw AdminOperationException.notFound("El exchange '" + request.exchange() + "' no existe");
        }
        if (!queueExists(request.queue())) {
            throw AdminOperationException.notFound("La cola '" + request.queue() + "' no existe");
        }
    }

    private Binding toBinding(BindingRequest request) {
        return new Binding(request.queue(), Binding.DestinationType.QUEUE,
                request.exchange(), request.routingKey(), Map.of());
    }

    private boolean queueExists(String name) {
        return rabbitAdmin.getQueueInfo(name) != null;
    }

    /** RabbitAdmin no tiene "exchangeExists": se usa una declaración pasiva (no crea nada). */
    private boolean exchangeExists(String name) {
        try {
            return Boolean.TRUE.equals(rabbitAdmin.getRabbitTemplate().execute(channel -> {
                channel.exchangeDeclarePassive(name);
                return true;
            }));
        } catch (AmqpConnectException e) {
            throw e;                 // broker caído: no confundir con "no existe"
        } catch (AmqpException e) {
            return false;            // 404 NOT_FOUND del broker
        }
    }
}