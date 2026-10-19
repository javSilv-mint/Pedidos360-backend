package cl.duoc.lab.admin.dto;

public record QueueInfoResponse(String name, int messageCount, int consumerCount) {
}