package cl.duoc.pedidos360.pedidos.service;

import cl.duoc.pedidos360.pedidos.exception.AccesoDenegadoException;
import cl.duoc.pedidos360.pedidos.model.EstadoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.repository.PedidoRepository;
import cl.duoc.pedidos360.pedidos.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Reglas de negocio: control de propiedad (anti-IDOR) y maquina de estados. */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final UsuarioAutenticado DUENO = new UsuarioAutenticado("dueno@test.cl", false);
    private static final UsuarioAutenticado OTRO  = new UsuarioAutenticado("otro@test.cl", false);
    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado("admin@test.cl", true);

    @Mock
    private PedidoRepository repository;

    @InjectMocks
    private PedidoService service;

    private Pedido pedido(EstadoPedido estado) {
        Pedido p = Pedido.builder().id(1L).clienteId(1L).clienteEmail("Dueno@test.cl").estado(estado).build();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void duenoPuedeVerSuPedidoSinImportarMayusculas() {
        pedido(EstadoPedido.PENDIENTE);
        assertThat(service.obtenerParaUsuario(1L, DUENO).getId()).isEqualTo(1L);
    }

    @Test
    void otroUsuarioNoPuedeVerPedidoAjeno() {
        pedido(EstadoPedido.PENDIENTE);
        assertThatThrownBy(() -> service.obtenerParaUsuario(1L, OTRO))
            .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void adminPuedeVerCualquierPedido() {
        pedido(EstadoPedido.PENDIENTE);
        assertThat(service.obtenerParaUsuario(1L, ADMIN)).isNotNull();
    }

    @Test
    void otroUsuarioNoPuedeCancelarPedidoAjeno() {
        pedido(EstadoPedido.PENDIENTE);
        assertThatThrownBy(() -> service.cancelar(1L, OTRO))
            .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void duenoPuedeCancelarPedidoPendiente() {
        pedido(EstadoPedido.PENDIENTE);
        when(repository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.cancelar(1L, DUENO).getEstado()).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    void noSePuedeCancelarPedidoDespachado() {
        pedido(EstadoPedido.DESPACHADO);
        assertThatThrownBy(() -> service.cancelar(1L, DUENO))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noSePuedeRetrocederEstado() {
        pedido(EstadoPedido.ENTREGADO);
        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoPedido.PENDIENTE))
            .isInstanceOf(IllegalStateException.class);
    }
}
