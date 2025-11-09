package de.remsfal.ticketing.entity.dto;

import java.util.Map;
import java.util.UUID;

public record TicketCreated(
        UUID ticketId,
        UUID tenantId,
        String text,
        Map<String, String> metadata
) {}
