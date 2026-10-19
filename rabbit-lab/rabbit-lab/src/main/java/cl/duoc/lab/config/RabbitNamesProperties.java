package cl.duoc.lab.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rabbit")
public record RabbitNamesProperties(
        @NotBlank String helloQueue,
        @NotNull @Valid Pedidos pedidos) {

    public record Pedidos(
            @NotBlank String exchange,
            @NotBlank String notificacionesQueue,
            @NotBlank String inventarioQueue,
            @NotBlank String rkCreado,
            @NotBlank String rkPagado,
            @NotBlank String rkCancelado,
            @NotBlank String dlxExchange,
            @NotBlank String inventarioDlq,
            @NotBlank String inventarioDlqRk) {
    }
}