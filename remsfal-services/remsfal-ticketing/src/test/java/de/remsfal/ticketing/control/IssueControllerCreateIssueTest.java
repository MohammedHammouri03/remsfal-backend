package de.remsfal.ticketing.control;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.TicketCreated;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueControllerCreateIssueTest {

    @Mock
    IssueRepository issueRepository;

    @Mock
    PriorityEventProducer priorityEventProducer;

    @Mock
    UserModel user;

    @Mock
    IssueModel issue;

    IssueController controller;

    @BeforeEach
    void setUp() {
        controller = new IssueController();
        controller.logger = Logger.getLogger(IssueController.class);
        controller.repository = issueRepository;
        controller.priorityEventProducer = priorityEventProducer;
    }

    @Test
    void createIssueShouldPublishTicketCreatedEvent() {
        UUID projectId = UUID.randomUUID();
        UUID tenancyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-05-05T08:00:00Z");

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(issue.getProjectId()).thenReturn(projectId);
        when(issue.getType()).thenReturn(IssueModel.Type.MAINTENANCE);
        when(issue.getTitle()).thenReturn("Heizung Beschädigt");
        when(issue.getDescription()).thenReturn("Heizung funktioniert nicht");

        when(issueRepository.insert(any(IssueEntity.class))).thenAnswer(invocation -> {
            IssueEntity entity = invocation.getArgument(0);
            entity.setTenancyId(tenancyId);
            entity.setCreatedAt(createdAt);
            entity.setModifiedAt(createdAt);
            return entity;
        });

        IssueModel result = controller.createIssue(user, issue);

        assertNotNull(result);
        InOrder order = inOrder(issueRepository, priorityEventProducer);
        order.verify(issueRepository).insert(any(IssueEntity.class));
        order.verify(priorityEventProducer, times(1)).send(any(TicketCreated.class));

        ArgumentCaptor<TicketCreated> eventCaptor = ArgumentCaptor.forClass(TicketCreated.class);
        verify(priorityEventProducer).send(eventCaptor.capture());
        TicketCreated event = eventCaptor.getValue();

        IssueEntity persisted = (IssueEntity) result;
        assertEquals(persisted.getId(), event.ticketId());
        assertEquals(tenancyId, event.tenantId());
        assertEquals("Radiator is leaking", event.text());
        assertEquals("MAINTENANCE", event.metadata().get("location"));
        assertEquals(createdAt.toString(), event.metadata().get("submittedAt"));
    }
}
