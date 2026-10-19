package cl.duoc.lab.hello;

import cl.duoc.lab.config.RabbitNamesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class HelloSender {

    private static final Logger log = LoggerFactory.getLogger(HelloSender.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitNamesProperties names;

    public HelloSender(RabbitTemplate rabbitTemplate, RabbitNamesProperties names) {
        this.rabbitTemplate = rabbitTemplate;
        this.names = names;
    }

    public void send(String message) {
        // Sin exchange explícito: usa el exchange por defecto ("").
        // Ahí la routing key = nombre de la cola destino.
        rabbitTemplate.convertAndSend(names.helloQueue(), message);
        log.info("evento=hello_enviado queue={} message={}", names.helloQueue(), message);
    }
}