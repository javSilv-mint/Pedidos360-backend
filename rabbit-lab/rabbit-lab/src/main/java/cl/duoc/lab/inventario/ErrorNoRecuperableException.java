package cl.duoc.lab.inventario;

public class ErrorNoRecuperableException extends RuntimeException {
    public ErrorNoRecuperableException(String mensaje) {
        super(mensaje);
    }
}