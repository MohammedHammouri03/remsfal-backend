package de.remsfal.ticketing.control;

import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.TicketPrioritized;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class PriorityResultConsumer {

    @Inject
    Logger log;

    @Inject
    IssueRepository issueRepository;

    @Incoming("priority-result")
    public void consume(TicketPrioritized ev) {
        var ticketId = ev.ticketId();
        log.infov("Kafka received ticket.prioritized ticketId={0} prio={1} score={2} model={3}",
                ticketId, ev.priority(), ev.modelVersion());

        Optional<IssueEntity> opt = issueRepository.findByIssueId(ticketId);
        if (opt.isEmpty()){
            log.warnv("ticket.prioritized for unknown ticketId {0}", ticketId);
            return;
        }

        var issue = opt.get();
        issue.setPriority(ev.priority());
        issue.setPriorityScore(ev.score());
        issue.setPriorityModelVersion(ev.modelVersion());
        issue.touch();
        issueRepository.update(issue);
    }
}
