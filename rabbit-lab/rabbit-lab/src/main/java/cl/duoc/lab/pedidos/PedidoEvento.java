package cl.duoc.lab.pedidos;

import java.time.Instant;

public record PedidoEvento(Long pedidoId, TipoEvento tipo, String emailCliente, Instant fecha) {
}