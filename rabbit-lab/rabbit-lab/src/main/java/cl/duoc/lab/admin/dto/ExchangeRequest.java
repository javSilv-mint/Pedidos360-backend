package  cl.duoc.lab.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = NamePolicy.NAME_REGEX, message = NamePolicy.NAME_MESSAGE)
        String name,

        @NotNull(message = "El tipo de exchange es obligatorio (DIRECT, TOPIC, FANOUT o HEADERS)")
        Kind type) {

    public enum Kind { DIRECT, TOPIC, FANOUT, HEADERS }
}