package ch.grudligstrasse.mda.starter.obligation.domain.event;

import ch.grudligstrasse.mda.starter.shared.events.DomainEvent;

public sealed interface FristDomainEvent extends DomainEvent
        permits FristAktiviert, FristErinnerungAusgeloest, FristGesichtet {
}
