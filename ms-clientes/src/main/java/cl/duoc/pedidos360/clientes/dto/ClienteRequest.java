package cl.duoc.pedidos360.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload de entrada para crear/actualizar clientes.
 * Separa el contrato HTTP de la entidad JPA: el cliente no puede fijar id ni "activo"
 * (mass assignment) y las reglas de formato quedan declaradas en un solo lugar.
 */
public record ClienteRequest(
        @NotBlank(message = "El RUT es obligatorio")
        @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El RUT debe tener formato 12345678-9 (sin puntos)")
        String rut,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80, message = "El apellido no puede superar 80 caracteres")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es valido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email,

        @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
        String telefono,

        @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
        String direccion
) {
    /** Copia con el email reemplazado (se usa para forzar el email del token). */
    public ClienteRequest conEmail(String nuevoEmail) {
        return new ClienteRequest(rut, nombre, apellido, nuevoEmail, telefono, direccion);
    }
}
