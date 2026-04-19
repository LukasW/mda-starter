package ch.grudligstrasse.mda.starter.obligation.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Frist {

    private final FristId id;
    private final UUID vertragId;
    private final FristArt art;
    private final LocalDate faelligkeitsDatum;
    private final int vorlaufTage;
    private LocalDate erinnerungsDatum;
    private FristStatus status;
    private final UUID verantwortlicherUserId;

    private Frist(FristId id, UUID vertragId, FristArt art, LocalDate faelligkeit,
                  int vorlaufTage, LocalDate erinnerung, FristStatus status, UUID verantwortlicher) {
        this.id = Objects.requireNonNull(id);
        this.vertragId = Objects.requireNonNull(vertragId);
        this.art = Objects.requireNonNull(art);
        this.faelligkeitsDatum = Objects.requireNonNull(faelligkeit);
        if (vorlaufTage < 0) {
            throw new DomainException("MDA-OBL-001", "vorlaufTage darf nicht negativ sein");
        }
        this.vorlaufTage = vorlaufTage;
        this.erinnerungsDatum = Objects.requireNonNull(erinnerung);
        this.status = Objects.requireNonNull(status);
        this.verantwortlicherUserId = Objects.requireNonNull(verantwortlicher);
    }

    public static Frist neu(UUID vertragId, FristArt art, LocalDate faelligkeit, int vorlaufTage,
                            UUID verantwortlicher) {
        LocalDate erinnerung = faelligkeit.minusDays(vorlaufTage);
        return new Frist(FristId.newId(), vertragId, art, faelligkeit, vorlaufTage, erinnerung,
                FristStatus.OFFEN, verantwortlicher);
    }

    public static Frist rehydrate(FristId id, UUID vertragId, FristArt art, LocalDate faelligkeit,
                                  int vorlaufTage, LocalDate erinnerung, FristStatus status, UUID verantwortlicher) {
        return new Frist(id, vertragId, art, faelligkeit, vorlaufTage, erinnerung, status, verantwortlicher);
    }

    public void markiereErinnert() {
        if (status != FristStatus.OFFEN) {
            throw new DomainException("MDA-OBL-010", "Erinnerung nur aus OFFEN moeglich, aktuell=" + status);
        }
        this.status = FristStatus.ERINNERT;
    }

    public void markiereGesichtet() {
        if (status != FristStatus.ERINNERT && status != FristStatus.ESKALIERT) {
            throw new DomainException("MDA-OBL-011", "Sichten nur aus ERINNERT/ESKALIERT moeglich, aktuell=" + status);
        }
        this.status = FristStatus.ERLEDIGT;
    }

    public void markiereEskaliert() {
        if (status != FristStatus.ERINNERT) {
            throw new DomainException("MDA-OBL-012", "Eskalation nur aus ERINNERT moeglich, aktuell=" + status);
        }
        this.status = FristStatus.ESKALIERT;
    }

    public boolean istFaelligZurErinnerung(LocalDate heute) {
        return status == FristStatus.OFFEN && !heute.isBefore(erinnerungsDatum);
    }

    public FristId id() { return id; }
    public UUID vertragId() { return vertragId; }
    public FristArt art() { return art; }
    public LocalDate faelligkeitsDatum() { return faelligkeitsDatum; }
    public int vorlaufTage() { return vorlaufTage; }
    public LocalDate erinnerungsDatum() { return erinnerungsDatum; }
    public FristStatus status() { return status; }
    public UUID verantwortlicherUserId() { return verantwortlicherUserId; }
}
