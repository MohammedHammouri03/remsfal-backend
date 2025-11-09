package de.remsfal.ticketing.entity.dto;

import java.util.UUID;

public record TicketPrioritized(
        UUID ticketId,
        String priority,
        double score,
        String modelVersion,
        String createdAt
) {}
