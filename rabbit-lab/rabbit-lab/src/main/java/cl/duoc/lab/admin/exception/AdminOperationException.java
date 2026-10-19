package cl.duoc.lab.admin.exception;

import org.springframework.http.HttpStatus;

public class AdminOperationException extends RuntimeException {

    private final HttpStatus status;

    private AdminOperationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static AdminOperationException badRequest(String message) {
        return new AdminOperationException(HttpStatus.BAD_REQUEST, message);
    }

    public static AdminOperationException notFound(String message) {
        return new AdminOperationException(HttpStatus.NOT_FOUND, message);
    }

    public static AdminOperationException conflict(String message) {
        return new AdminOperationException(HttpStatus.CONFLICT, message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}