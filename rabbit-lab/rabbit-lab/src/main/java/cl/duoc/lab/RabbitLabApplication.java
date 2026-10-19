package cl.duoc.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RabbitLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitLabApplication.class, args);
    }
}
