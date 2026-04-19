package ch.grudligstrasse.mda.starter.approval.domain.event;

import ch.grudligstrasse.mda.starter.shared.events.DomainEvent;

public sealed interface FreigabeDomainEvent extends DomainEvent
        permits FreigabeAngefordert, FreigabeEntschieden {
}
