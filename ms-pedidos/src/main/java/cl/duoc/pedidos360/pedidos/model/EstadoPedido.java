package cl.duoc.pedidos360.pedidos.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida del pedido como maquina de estados explicita.
 * Evita transiciones ilegales (p.ej. ENTREGADO -> PENDIENTE o cancelar algo ya despachado)
 * y deja preparada la base para publicar eventos de cambio de estado en la EV3.
 */
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    DESPACHADO,
    ENTREGADO,
    CANCELADO;

    public Set<EstadoPedido> siguientesPermitidos() {
        return switch (this) {
            case PENDIENTE      -> EnumSet.of(CONFIRMADO, CANCELADO);
            case CONFIRMADO     -> EnumSet.of(EN_PREPARACION, CANCELADO);
            case EN_PREPARACION -> EnumSet.of(DESPACHADO, CANCELADO);
            case DESPACHADO     -> EnumSet.of(ENTREGADO);
            case ENTREGADO, CANCELADO -> EnumSet.noneOf(EstadoPedido.class);
        };
    }

    public boolean puedeCambiarA(EstadoPedido destino) {
        return siguientesPermitidos().contains(destino);
    }

    public boolean esCancelable() {
        return puedeCambiarA(CANCELADO);
    }
}
