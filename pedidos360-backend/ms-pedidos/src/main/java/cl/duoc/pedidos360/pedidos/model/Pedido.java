package cl.duoc.pedidos360.pedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Guardamos solo el ID del cliente, no la entidad completa:
     * cada microservicio tiene su propia base de datos.
     */
    @NotNull(message = "El pedido debe tener un cliente asociado")
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    /**
     * Email del usuario autenticado. Se llena
     * automaticamente desde el claim del JWT y se usa para que un
     * usuario con rol Cliente solo vea SUS propios pedidos.
     */
    @Column(name = "cliente_email", length = 120)
    private String clienteEmail;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 250)
    private String direccionEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Helper para mantener la relacion bidireccional consistente. */
    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    /** Recalcula el total sumando los subtotales de cada linea. */
    public void recalcularTotal() {
        this.total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
