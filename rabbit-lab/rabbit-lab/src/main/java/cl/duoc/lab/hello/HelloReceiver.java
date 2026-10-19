package cl.duoc.lab.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class HelloReceiver {

    private static final Logger log = LoggerFactory.getLogger(HelloReceiver.class);

    @RabbitListener(queues = "${app.rabbit.hello-queue}")
    public void onMessage(String message) {
        log.info("evento=hello_recibido message={}", message);
    }
}