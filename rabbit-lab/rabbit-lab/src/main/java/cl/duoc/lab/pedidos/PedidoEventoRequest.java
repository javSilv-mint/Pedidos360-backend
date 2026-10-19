package cl.duoc.lab.pedidos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoEventoRequest(
        @NotNull(message = "pedidoId es obligatorio")
        @Positive(message = "pedidoId debe ser positivo")
        Long pedidoId,

        @NotNull(message = "tipo es obligatorio (CREADO, PAGADO o CANCELADO)")
        TipoEvento tipo,

        @NotBlank(message = "emailCliente es obligatorio")
        @Email(message = "emailCliente inválido")
        String emailCliente) {
}