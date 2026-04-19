package ch.grudligstrasse.mda.starter.shared.events;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
@DefaultBean
public class InMemoryDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = Logger.getLogger(InMemoryDomainEventPublisher.class);

    private final List<DomainEvent> recorded = new CopyOnWriteArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        recorded.add(event);
        LOG.infof("domain-event type=%s id=%s correlationId=%s", event.eventType(), event.eventId(), event.correlationId());
    }

    public List<DomainEvent> recorded() {
        return List.copyOf(recorded);
    }

    public void reset() {
        recorded.clear();
    }
}
