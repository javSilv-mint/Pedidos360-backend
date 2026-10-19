package cl.duoc.pedidos360.pedidos.exception;

/**
 * El usuario esta autenticado y tiene el rol correcto, pero intenta operar
 * sobre un recurso que no le pertenece (control de propiedad / anti-IDOR).
 * Se traduce a HTTP 403 en GlobalExceptionHandler.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
