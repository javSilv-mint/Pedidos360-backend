package cl.duoc.lab.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitHelloConfig {

    @Bean
    public Queue helloQueue(RabbitNamesProperties names) {
        return QueueBuilder.durable(names.helloQueue()).build();
    }
}