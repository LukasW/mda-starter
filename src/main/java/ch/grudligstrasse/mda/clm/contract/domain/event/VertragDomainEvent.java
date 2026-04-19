package ch.grudligstrasse.mda.clm.contract.domain.event;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.shared.events.DomainEvent;

public sealed interface VertragDomainEvent extends DomainEvent
        permits VertragErstellt, VertragVersionErstellt, VertragStageGeaendert, VertragPersonZugeordnet {

    VertragId vertragId();
}
