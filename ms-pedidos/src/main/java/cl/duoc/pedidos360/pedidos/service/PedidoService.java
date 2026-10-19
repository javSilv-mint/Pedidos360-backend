package cl.duoc.pedidos360.pedidos.service;

import cl.duoc.pedidos360.pedidos.dto.CrearPedidoRequest;
import cl.duoc.pedidos360.pedidos.exception.AccesoDenegadoException;
import cl.duoc.pedidos360.pedidos.model.DetallePedido;
import cl.duoc.pedidos360.pedidos.model.EstadoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.repository.PedidoRepository;
import cl.duoc.pedidos360.pedidos.security.UsuarioAutenticado;
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

    /**
     * Lectura con control de propiedad: un Admin ve cualquier pedido;
     * cualquier otro usuario solo los pedidos cuyo email coincide con el del token.
     */
    @Transactional(readOnly = true)
    public Pedido obtenerParaUsuario(Long id, UsuarioAutenticado usuario) {
        Pedido pedido = obtenerPorId(id);
        verificarPropiedad(pedido, usuario);
        return pedido;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorEmail(String email) {
        return repository.findByClienteEmailIgnoreCase(email);
    }

    /**
     * Crea el pedido. El email del dueno viene SIEMPRE del token (parametro separado del DTO).
     * Limitacion conocida: precioUnitario lo informa el cliente; en la EV3 debe validarse
     * contra ms-productos (sincrono o via evento de reserva de stock).
     */
    @Transactional
    public Pedido crear(CrearPedidoRequest request, String emailDueno) {
        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .clienteEmail(emailDueno)
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

    /** Cambio de estado administrativo, respetando la maquina de estados. */
    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        return transicionar(obtenerPorId(id), nuevoEstado);
    }

    /** Cancelacion por el propio dueno (o Admin), solo si el estado actual lo permite. */
    @Transactional
    public Pedido cancelar(Long id, UsuarioAutenticado usuario) {
        Pedido pedido = obtenerPorId(id);
        verificarPropiedad(pedido, usuario);
        return transicionar(pedido, EstadoPedido.CANCELADO);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtenerPorId(id));
    }

    private Pedido transicionar(Pedido pedido, EstadoPedido destino) {
        EstadoPedido actual = pedido.getEstado();
        if (!actual.puedeCambiarA(destino)) {
            throw new IllegalStateException(
                "Transicion de estado no permitida: " + actual + " -> " + destino
                + ". Permitidas: " + actual.siguientesPermitidos());
        }
        pedido.setEstado(destino);
        return repository.save(pedido);
    }

    private static void verificarPropiedad(Pedido pedido, UsuarioAutenticado usuario) {
        if (usuario.esAdmin()) return;
        String email = usuario.emailObligatorio();
        if (pedido.getClienteEmail() == null || !pedido.getClienteEmail().equalsIgnoreCase(email)) {
            throw new AccesoDenegadoException("El pedido " + pedido.getId() + " no pertenece al usuario autenticado");
        }
    }
}
