package cl.duoc.lab.admin.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rabbit.admin")
public record RabbitAdminProperties(
        @NotNull List<String> protectedQueues,
        @NotNull List<String> protectedExchanges,
        @Positive int maxMessageTtlMs,
        @Positive int maxQueueLength) {
}
