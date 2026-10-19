package cl.duoc.pedidos360.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload de POST /api/pedidos.
 * NO incluye clienteEmail: el dueno del pedido siempre se toma del JWT verificado,
 * nunca del cuerpo (evita suplantacion / mass assignment).
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CrearPedidoRequest {

    @NotNull(message = "El clienteId es obligatorio")
    @Positive(message = "El clienteId debe ser positivo")
    private Long clienteId;

    @Size(max = 250, message = "La direccion no puede superar 250 caracteres")
    private String direccionEntrega;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Size(max = 50, message = "Un pedido admite como maximo 50 lineas")
    private List<@Valid @NotNull(message = "La linea del pedido no puede ser nula") LineaPedido> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class LineaPedido {
        @NotNull(message = "El productoId es obligatorio")
        @Positive(message = "El productoId debe ser positivo")
        private Long productoId;

        @Size(max = 120, message = "El nombre del producto no puede superar 120 caracteres")
        private String nombreProducto;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad minima es 1")
        @Max(value = 1000, message = "La cantidad maxima por linea es 1000")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio admite 10 enteros y 2 decimales")
        private BigDecimal precioUnitario;
    }
}
