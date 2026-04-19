package ch.grudligstrasse.mda.starter.approval.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Freigabe {

    private final FreigabeId id;
    private final UUID vertragId;
    private final UUID versionId;
    private final UUID reviewerUserId;
    private Entscheidung entscheidung;
    private String begruendung;
    private Instant entschiedenAm;
    private final Instant angefordertAm;

    private Freigabe(FreigabeId id, UUID vertragId, UUID versionId, UUID reviewerUserId,
                     Entscheidung entscheidung, String begruendung, Instant entschiedenAm, Instant angefordertAm) {
        this.id = Objects.requireNonNull(id);
        this.vertragId = Objects.requireNonNull(vertragId);
        this.versionId = Objects.requireNonNull(versionId);
        this.reviewerUserId = Objects.requireNonNull(reviewerUserId);
        this.entscheidung = entscheidung;
        this.begruendung = begruendung;
        this.entschiedenAm = entschiedenAm;
        this.angefordertAm = Objects.requireNonNull(angefordertAm);
    }

    public static Freigabe anfordern(UUID vertragId, UUID versionId, UUID reviewerUserId) {
        return new Freigabe(FreigabeId.newId(), vertragId, versionId, reviewerUserId, null, null, null, Instant.now());
    }

    public static Freigabe rehydrate(FreigabeId id, UUID vertragId, UUID versionId, UUID reviewerUserId,
                                     Entscheidung entscheidung, String begruendung, Instant entschiedenAm,
                                     Instant angefordertAm) {
        return new Freigabe(id, vertragId, versionId, reviewerUserId, entscheidung, begruendung,
                entschiedenAm, angefordertAm);
    }

    public void entscheiden(Entscheidung entscheidung, String begruendung) {
        Objects.requireNonNull(entscheidung);
        if (this.entscheidung != null) {
            throw new DomainException("MDA-APV-001", "Freigabe ist bereits entschieden");
        }
        if (entscheidung == Entscheidung.ABGELEHNT && (begruendung == null || begruendung.isBlank())) {
            throw new DomainException("MDA-APV-002", "Bei Ablehnung ist eine Begruendung erforderlich");
        }
        this.entscheidung = entscheidung;
        this.begruendung = begruendung;
        this.entschiedenAm = Instant.now();
    }

    public FreigabeId id() { return id; }
    public UUID vertragId() { return vertragId; }
    public UUID versionId() { return versionId; }
    public UUID reviewerUserId() { return reviewerUserId; }
    public Optional<Entscheidung> entscheidung() { return Optional.ofNullable(entscheidung); }
    public Optional<String> begruendung() { return Optional.ofNullable(begruendung); }
    public Optional<Instant> entschiedenAm() { return Optional.ofNullable(entschiedenAm); }
    public Instant angefordertAm() { return angefordertAm; }
}
