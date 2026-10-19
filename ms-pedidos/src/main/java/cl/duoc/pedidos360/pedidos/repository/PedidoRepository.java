package cl.duoc.pedidos360.pedidos.repository;

import cl.duoc.pedidos360.pedidos.model.EstadoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEstado(EstadoPedido estado);
    // Usado por /mis-pedidos: filtra por el email del token
    List<Pedido> findByClienteEmailIgnoreCase(String clienteEmail);
}
