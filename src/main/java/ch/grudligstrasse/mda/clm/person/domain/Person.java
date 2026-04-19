package ch.grudligstrasse.mda.clm.person.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root fuer Personen. Enthaelt sowohl intern erfasste Personen als auch
 * lokale Snapshots aus der externen Personenverwaltung (quelleTyp = EXTERN_API).
 */
public class Person {

    private final PersonId id;
    private final UUID tenantId;
    private String vorname;
    private String nachname;
    private Email email;
    private String organisation;
    private String funktion;
    private PersonenQuelle quelleTyp;
    private String externeId;
    private final Instant erstelltAm;
    private Instant modifiedAt;
    private long versionNumber;

    private Person(PersonId id, UUID tenantId, String vorname, String nachname, Email email,
                   String organisation, String funktion, PersonenQuelle quelleTyp, String externeId,
                   Instant erstelltAm, Instant modifiedAt, long versionNumber) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.vorname = requireNonBlank(vorname, "vorname");
        this.nachname = requireNonBlank(nachname, "nachname");
        this.email = Objects.requireNonNull(email, "email darf nicht null sein.");
        this.organisation = organisation;
        this.funktion = funktion;
        this.quelleTyp = Objects.requireNonNull(quelleTyp);
        this.externeId = externeId;
        this.erstelltAm = Objects.requireNonNull(erstelltAm);
        this.modifiedAt = Objects.requireNonNull(modifiedAt);
        this.versionNumber = versionNumber;
        if (quelleTyp == PersonenQuelle.EXTERN_API && (externeId == null || externeId.isBlank())) {
            throw new IllegalArgumentException("externeId ist Pflicht bei quelleTyp=EXTERN_API.");
        }
    }

    public static Person erfassen(String vorname, String nachname, Email email,
                                  String organisation, String funktion, UUID tenantId) {
        Instant now = Instant.now();
        return new Person(PersonId.generate(), tenantId, vorname, nachname, email,
                organisation, funktion, PersonenQuelle.INTERN, null, now, now, 0);
    }

    public static Person snapshotExtern(String externeId, String vorname, String nachname, Email email,
                                        String organisation, String funktion, UUID tenantId) {
        Instant now = Instant.now();
        return new Person(PersonId.generate(), tenantId, vorname, nachname, email,
                organisation, funktion, PersonenQuelle.EXTERN_API, externeId, now, now, 0);
    }

    public static Person rehydrate(PersonId id, UUID tenantId, String vorname, String nachname, Email email,
                                   String organisation, String funktion, PersonenQuelle quelleTyp, String externeId,
                                   Instant erstelltAm, Instant modifiedAt, long versionNumber) {
        return new Person(id, tenantId, vorname, nachname, email, organisation, funktion,
                quelleTyp, externeId, erstelltAm, modifiedAt, versionNumber);
    }

    public void aktualisieren(String vorname, String nachname, Email email,
                              String organisation, String funktion) {
        this.vorname = requireNonBlank(vorname, "vorname");
        this.nachname = requireNonBlank(nachname, "nachname");
        this.email = Objects.requireNonNull(email, "email darf nicht null sein.");
        this.organisation = organisation;
        this.funktion = funktion;
        this.modifiedAt = Instant.now();
        this.versionNumber++;
    }

    private static String requireNonBlank(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(name + " darf nicht leer sein.");
        }
        return v.trim();
    }

    public PersonId id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String vorname() { return vorname; }
    public String nachname() { return nachname; }
    public Email email() { return email; }
    public String organisation() { return organisation; }
    public String funktion() { return funktion; }
    public PersonenQuelle quelleTyp() { return quelleTyp; }
    public String externeId() { return externeId; }
    public Instant erstelltAm() { return erstelltAm; }
    public Instant modifiedAt() { return modifiedAt; }
    public long versionNumber() { return versionNumber; }

    // mda-generator: manual-edits-below
}
