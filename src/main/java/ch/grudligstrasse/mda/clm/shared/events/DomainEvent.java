package ch.grudligstrasse.mda.clm.shared.events;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    String eventType();
}
