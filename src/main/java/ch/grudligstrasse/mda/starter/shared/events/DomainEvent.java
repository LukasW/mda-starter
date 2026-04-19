package ch.grudligstrasse.mda.starter.shared.events;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    String eventType();
    UUID correlationId();
}
