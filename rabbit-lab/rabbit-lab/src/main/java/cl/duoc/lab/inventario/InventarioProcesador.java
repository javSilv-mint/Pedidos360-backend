package cl.duoc.lab.inventario;

import cl.duoc.lab.pedidos.PedidoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventarioProcesador {

    private static final Logger log = LoggerFactory.getLogger(InventarioProcesador.class);

    // SOLO PARA LA DEMO: disparadores de fallo según el email. Se borran al conectar con ms-productos.
    private static final String FALLO_PERMANENTE = "error-permanente";
    private static final String FALLO_AGOTADO = "reintento-agotado";
    private static final String FALLO_Y_RECUPERA = "reintento-ok";
    private static final int INTENTO_EN_QUE_RECUPERA = 3;

    public void procesar(PedidoEvento evento, int intento) {
        if (evento == null || evento.pedidoId() == null || evento.tipo() == null) {
            throw new ErrorNoRecuperableException("Evento incompleto");
        }
        String email = evento.emailCliente() == null ? "" : evento.emailCliente();

        if (email.contains(FALLO_PERMANENTE)) {
            throw new ErrorNoRecuperableException("Datos del pedido irrecuperables");
        }
        if (email.contains(FALLO_AGOTADO)) {
            throw new ErrorRecuperableException("Servicio de stock no disponible (simulado)");
        }
        if (email.contains(FALLO_Y_RECUPERA) && intento < INTENTO_EN_QUE_RECUPERA) {
            throw new ErrorRecuperableException("Servicio de stock caído temporalmente (simulado)");
        }
        log.info("dominio=inventario evento=stock_actualizado pedidoId={} tipo={} intento={}",
                evento.pedidoId(), evento.tipo(), intento);
    }
}