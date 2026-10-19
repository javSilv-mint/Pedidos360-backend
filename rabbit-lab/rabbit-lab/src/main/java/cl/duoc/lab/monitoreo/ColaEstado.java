package cl.duoc.lab.monitoreo;

/** Foto de una cola en el broker. existe=false si la cola no está declarada. */
public record ColaEstado(String nombre, boolean existe, long mensajes, int consumidores) {
}
