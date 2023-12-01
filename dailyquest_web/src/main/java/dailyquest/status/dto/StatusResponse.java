package dailyquest.status.dto;

public record StatusResponse(
    Long registeredCount,
    Long completedCount,
    Long discardedCount,
    Long failedCount
) {}