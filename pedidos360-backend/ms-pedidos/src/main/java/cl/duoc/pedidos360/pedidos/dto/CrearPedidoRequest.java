package cl.duoc.pedidos360.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CrearPedidoRequest {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    private String clienteEmail;

    private String direccionEntrega;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<LineaPedido> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class LineaPedido {
        @NotNull private Long productoId;
        private String nombreProducto;
        @NotNull @Min(1) private Integer cantidad;
        @NotNull @DecimalMin("0.0") private BigDecimal precioUnitario;
    }
}
