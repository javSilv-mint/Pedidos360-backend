package cl.duoc.lab.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rabbit.policies")
public record RabbitPoliciesProperties(
        @Positive int inventarioTtlMs,
        @Positive int inventarioMaxLength,
        @Positive int dlqTtlMs,
        @Positive int dlqMaxLength,
        @Positive int prefetch,
        @NotNull @Valid Retry retry,
        @NotNull @Valid Dlq dlq) {

    public record Retry(
            @Min(1) int maxAttempts,
            @Positive long initialIntervalMs,
            @Positive double multiplier,
            @Positive long maxIntervalMs) {
    }

    public record Dlq(boolean listenerEnabled, @Min(1) int alertaUmbral) {
    }
}