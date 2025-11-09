package de.remsfal.ticketing.control;

import de.remsfal.ticketing.entity.dto.TicketCreated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import io.smallrye.reactive.messaging.kafka.Record;

import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;


@ApplicationScoped
public class PriorityEventProducer {

    @Inject
    Logger log;

    @Channel("priority-request")
    Emitter<Record<String, TicketCreated>> out;

    public void send(TicketCreated dto) {
        var key = dto.ticketId().toString();
        log.infov("Kafka send ticket.created key={0}", key);
        CompletionStage<Void> ack = out.send(Record.of(key, dto));
        ack.whenComplete((v, ex) -> {
            if (ex != null) {
                log.errorf(ex, "Failed to send ticket.created key=%s", key);
            } else {
                log.infov("Sent ticket.created key={0}", key);
            }
        });
    }
}