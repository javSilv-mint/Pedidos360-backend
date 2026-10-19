package cl.duoc.pedidos360.productos.service;

import cl.duoc.pedidos360.productos.model.Producto;
import cl.duoc.pedidos360.productos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void cantidadNegativaNoAumentaStock() {
        assertThatThrownBy(() -> service.descontarStock(1L, -5))
            .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).descontarStockSiDisponible(anyLong(), anyInt());
    }

    @Test
    void stockInsuficienteLanzaConflicto() {
        Producto p = Producto.builder().id(1L).nombre("Pizza").precio(BigDecimal.TEN).stock(2).activo(true).build();
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(p));
        when(repository.descontarStockSiDisponible(1L, 5)).thenReturn(0);
        assertThatThrownBy(() -> service.descontarStock(1L, 5))
            .isInstanceOf(IllegalStateException.class);
    }
}
