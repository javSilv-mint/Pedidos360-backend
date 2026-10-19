package  cl.duoc.lab.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BindingRequest(
        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = NamePolicy.NAME_REGEX, message = NamePolicy.NAME_MESSAGE)
        String exchange,

        @NotBlank(message = "La cola destino es obligatoria")
        @Pattern(regexp = NamePolicy.NAME_REGEX, message = NamePolicy.NAME_MESSAGE)
        String queue,

        @NotNull(message = "routingKey es obligatoria (puede ser cadena vacía)")
        @Pattern(regexp = NamePolicy.ROUTING_KEY_REGEX, message = NamePolicy.ROUTING_KEY_MESSAGE)
        String routingKey) {
}