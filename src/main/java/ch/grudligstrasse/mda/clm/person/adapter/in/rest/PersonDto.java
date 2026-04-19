package ch.grudligstrasse.mda.clm.person.adapter.in.rest;

import ch.grudligstrasse.mda.clm.person.domain.Person;

public record PersonDto(
        String id,
        String vorname,
        String nachname,
        String email,
        String organisation,
        String funktion,
        String quelleTyp,
        String externeId) {

    public static PersonDto of(Person p) {
        return new PersonDto(
                p.id().asString(),
                p.vorname(),
                p.nachname(),
                p.email().value(),
                p.organisation(),
                p.funktion(),
                p.quelleTyp().name(),
                p.externeId());
    }
}
