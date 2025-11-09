package de.remsfal.ticketing.control;

import de.remsfal.ticketing.entity.dto.TicketPrioritized;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriorityResultConsumerTest {

    @Mock
    IssueRepository issueRepository;

    PriorityResultConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PriorityResultConsumer();
        consumer.log = Logger.getLogger(PriorityResultConsumer.class);
        consumer.issueRepository = issueRepository;
    }

    @Test
    void consumeShouldUpdateIssueWithPriority() {
        UUID ticketId = UUID.randomUUID();
        TicketPrioritized dto =
                new TicketPrioritized(ticketId, "HIGH", 0.87d, "xlm-roberta-v1", "2024-05-09T08:00:00Z");
        IssueEntity issue = new IssueEntity();
        issue.generateId();

        when(issueRepository.findByIssueId(ticketId)).thenReturn(Optional.of(issue));

        consumer.consume(dto);                                     // ✅ Signatur muss dto konsumieren

        verify(issueRepository).update(issue);
        assertEquals("HIGH", issue.getPriority());
        assertEquals(0.87d, issue.getPriorityScore());
        assertEquals("xlm-roberta-v1", issue.getPriorityModelVersion());
    }

    @Test
    void consumeShouldIgnoreMissingIssue() {
        UUID ticketId = UUID.randomUUID();
        TicketPrioritized dto =
                new TicketPrioritized(ticketId, "LOW", 0.15d, "baseline", "2024-05-09T08:00:00Z");

        when(issueRepository.findByIssueId(ticketId)).thenReturn(Optional.empty());

        consumer.consume(dto);

        verify(issueRepository, never()).update(any(IssueEntity.class)); // ✅ Typ angeben
    }
}
