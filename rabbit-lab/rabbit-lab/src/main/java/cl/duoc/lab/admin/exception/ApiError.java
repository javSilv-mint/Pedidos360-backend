package cl.duoc.lab.admin.exception;

import java.util.List;

public record ApiError(String timestamp, int status, String error,
                       String message, List<String> details, String path) {
}