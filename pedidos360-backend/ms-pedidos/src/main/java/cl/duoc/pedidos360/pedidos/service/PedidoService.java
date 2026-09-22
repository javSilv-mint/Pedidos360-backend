package cl.duoc.pedidos360.pedidos.service;

import cl.duoc.pedidos360.pedidos.dto.CrearPedidoRequest;
import cl.duoc.pedidos360.pedidos.model.DetallePedido;
import cl.duoc.pedidos360.pedidos.model.EstadoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorEmail(String email) {
        return repository.findByClienteEmailIgnoreCase(email);
    }

    @Transactional
    public Pedido crear(CrearPedidoRequest request) {
        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .clienteEmail(request.getClienteEmail())
                .direccionEntrega(request.getDireccionEntrega())
                .estado(EstadoPedido.PENDIENTE)
                .build();

        for (CrearPedidoRequest.LineaPedido item : request.getItems()) {
            DetallePedido detalle = DetallePedido.builder()
                    .productoId(item.getProductoId())
                    .nombreProducto(item.getNombreProducto())
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .subtotal(item.getPrecioUnitario()
                              .multiply(BigDecimal.valueOf(item.getCantidad())))
                    .build();
            pedido.agregarDetalle(detalle);
        }

        pedido.recalcularTotal();
        return repository.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);
        if (pedido.getEstado() == EstadoPedido.ENTREGADO
                || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException(
                "No se puede modificar un pedido en estado " + pedido.getEstado());
        }
        pedido.setEstado(nuevoEstado);
        return repository.save(pedido);
    }

    @Transactional
    public Pedido cancelar(Long id) {
        return cambiarEstado(id, EstadoPedido.CANCELADO);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtenerPorId(id));
    }
}
