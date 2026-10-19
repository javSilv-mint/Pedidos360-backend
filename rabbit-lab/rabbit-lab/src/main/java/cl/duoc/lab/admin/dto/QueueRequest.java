package cl.duoc.lab.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record QueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = NamePolicy.NAME_REGEX, message = NamePolicy.NAME_MESSAGE)
        String name,

        @NotNull(message = "El tipo de cola es obligatorio (CLASSIC o QUORUM)")
        Type type,

        @Positive(message = "messageTtlMs debe ser mayor que 0")
        Integer messageTtlMs,

        @Positive(message = "maxLength debe ser mayor que 0")
        Integer maxLength,

        @Pattern(regexp = NamePolicy.NAME_REGEX, message = NamePolicy.NAME_MESSAGE)
        String deadLetterExchange,

        @Pattern(regexp = NamePolicy.ROUTING_KEY_REGEX, message = NamePolicy.ROUTING_KEY_MESSAGE)
        String deadLetterRoutingKey) {

    public enum Type { CLASSIC, QUORUM }
}