package ch.grudligstrasse.mda.starter.contract.domain.event;

import ch.grudligstrasse.mda.starter.shared.events.DomainEvent;

public sealed interface VertragDomainEvent extends DomainEvent
        permits VertragErfasst, VertragEingereicht, VertragFreigegeben, VertragAbgelehnt, NeueVersionHochgeladen {
}
