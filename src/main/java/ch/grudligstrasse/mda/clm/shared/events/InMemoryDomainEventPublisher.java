package ch.grudligstrasse.mda.clm.shared.events;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
@DefaultBean
public class InMemoryDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = Logger.getLogger(InMemoryDomainEventPublisher.class);

    private final List<DomainEvent> captured = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void publish(Collection<? extends DomainEvent> events) {
        for (DomainEvent event : events) {
            captured.add(event);
            LOG.debugf("Event published: type=%s id=%s occurredAt=%s",
                    event.eventType(), event.eventId(), event.occurredAt());
        }
    }

    public List<DomainEvent> captured() {
        synchronized (captured) {
            return List.copyOf(captured);
        }
    }

    public void reset() {
        captured.clear();
    }
}
