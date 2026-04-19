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
    private Instant deletedAt;
    private long versionNumber;

    private Person(PersonId id, UUID tenantId, String vorname, String nachname, Email email,
                   String organisation, String funktion, PersonenQuelle quelleTyp, String externeId,
                   Instant erstelltAm, Instant modifiedAt, Instant deletedAt, long versionNumber) {
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
        this.deletedAt = deletedAt;
        this.versionNumber = versionNumber;
        if (quelleTyp == PersonenQuelle.EXTERN_API && (externeId == null || externeId.isBlank())) {
            throw new IllegalArgumentException("externeId ist Pflicht bei quelleTyp=EXTERN_API.");
        }
    }

    public static Person erfassen(String vorname, String nachname, Email email,
                                  String organisation, String funktion, UUID tenantId) {
        Instant now = Instant.now();
        return new Person(PersonId.generate(), tenantId, vorname, nachname, email,
                organisation, funktion, PersonenQuelle.INTERN, null, now, now, null, 0);
    }

    public static Person snapshotExtern(String externeId, String vorname, String nachname, Email email,
                                        String organisation, String funktion, UUID tenantId) {
        Instant now = Instant.now();
        return new Person(PersonId.generate(), tenantId, vorname, nachname, email,
                organisation, funktion, PersonenQuelle.EXTERN_API, externeId, now, now, null, 0);
    }

    public static Person rehydrate(PersonId id, UUID tenantId, String vorname, String nachname, Email email,
                                   String organisation, String funktion, PersonenQuelle quelleTyp, String externeId,
                                   Instant erstelltAm, Instant modifiedAt, long versionNumber) {
        return new Person(id, tenantId, vorname, nachname, email, organisation, funktion,
                quelleTyp, externeId, erstelltAm, modifiedAt, null, versionNumber);
    }

    public static Person rehydrate(PersonId id, UUID tenantId, String vorname, String nachname, Email email,
                                   String organisation, String funktion, PersonenQuelle quelleTyp, String externeId,
                                   Instant erstelltAm, Instant modifiedAt, Instant deletedAt, long versionNumber) {
        return new Person(id, tenantId, vorname, nachname, email, organisation, funktion,
                quelleTyp, externeId, erstelltAm, modifiedAt, deletedAt, versionNumber);
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
    public Instant deletedAt() { return deletedAt; }
    public long versionNumber() { return versionNumber; }

    // mda-generator: manual-edits-below

    public boolean istGeloescht() {
        return deletedAt != null;
    }

    public boolean istIntern() {
        return quelleTyp == PersonenQuelle.INTERN;
    }

    /**
     * Aendert eine interne Person mit Optimistic-Locking-Pruefung.
     * EXTERN_API-Snapshots sind read-only ({@link PersonReadOnlyException}).
     * Versions-Konflikte erzeugen {@link PersonVersionKonfliktException}.
     */
    public void aktualisiereSicher(String vorname, String nachname, Email email,
                                   String organisation, String funktion, long expectedVersion) {
        if (!istIntern()) {
            throw new PersonReadOnlyException(
                    "Person mit quelleTyp=EXTERN_API ist read-only und kann nicht geaendert werden.");
        }
        if (this.versionNumber != expectedVersion) {
            throw new PersonVersionKonfliktException(
                    "Version-Konflikt: erwartet=" + expectedVersion + ", aktuell=" + this.versionNumber);
        }
        aktualisieren(vorname, nachname, email, organisation, funktion);
    }

    /**
     * Markiert die interne Person als geloescht (Soft-Delete).
     * EXTERN_API-Snapshots koennen nicht geloescht werden ({@link PersonReadOnlyException}).
     * Doppeltes Loeschen ist no-op (Idempotenz).
     */
    public void loeschen(long expectedVersion) {
        if (!istIntern()) {
            throw new PersonReadOnlyException(
                    "Person mit quelleTyp=EXTERN_API kann nicht geloescht werden.");
        }
        if (istGeloescht()) {
            return;
        }
        if (this.versionNumber != expectedVersion) {
            throw new PersonVersionKonfliktException(
                    "Version-Konflikt: erwartet=" + expectedVersion + ", aktuell=" + this.versionNumber);
        }
        Instant now = Instant.now();
        this.deletedAt = now;
        this.modifiedAt = now;
        this.versionNumber++;
    }

    /** Read-Only-Verstoss (Aendern oder Loeschen eines EXTERN_API-Snapshots). */
    public static final class PersonReadOnlyException extends RuntimeException {
        public PersonReadOnlyException(String msg) { super(msg); }
    }

    /** Optimistic-Locking-Konflikt beim Aendern oder Loeschen. */
    public static final class PersonVersionKonfliktException extends RuntimeException {
        public PersonVersionKonfliktException(String msg) { super(msg); }
    }
}
