package ch.grudligstrasse.mda.clm.person.adapter.out.rest;

/**
 * JSON-Payload des externen Personen-API gemaess Fachspec §7.2.
 * Felder werden 1:1 auf {@code ExternePersonenverwaltungClient.ExternePerson} gemappt.
 */
public record ExternePersonPayload(
        String externeId,
        String vorname,
        String nachname,
        String email,
        String organisation,
        String funktion) {}
