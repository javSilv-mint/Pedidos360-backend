package cl.duoc.lab.hello;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HelloRequest(
        @NotBlank(message = "message no puede estar vacío")
        @Size(max = 200, message = "message admite máximo 200 caracteres")
        String message) {
}