package ch.grudligstrasse.mda.clm.shared.events;

import java.util.Collection;

public interface DomainEventPublisher {
    void publish(Collection<? extends DomainEvent> events);
}
