package cl.duoc.lab.inventario;

public class ErrorRecuperableException extends RuntimeException {
    public ErrorRecuperableException(String mensaje) {
        super(mensaje);
    }
}