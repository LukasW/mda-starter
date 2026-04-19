package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Vertrag {

    private final VertragId id;
    private final MandantId mandantId;
    private String titel;
    private Vertragsart vertragsart;
    private PartnerId partnerId;
    private VertragStatus status;
    private LocalDate startDatum;
    private LocalDate endDatum;
    private Integer kuendigungsfristTage;
    private UserId vertragsverantwortlicherUserId;
    private final Instant erstelltAm;
    private final UserId erstelltVon;
    private final List<DokumentVersion> versionen = new ArrayList<>();

    private Vertrag(VertragId id, MandantId mandantId, String titel, Vertragsart vertragsart,
                    PartnerId partnerId, VertragStatus status, LocalDate startDatum, LocalDate endDatum,
                    Integer kuendigungsfristTage, UserId vertragsverantwortlicherUserId,
                    Instant erstelltAm, UserId erstelltVon) {
        this.id = Objects.requireNonNull(id);
        this.mandantId = Objects.requireNonNull(mandantId);
        this.titel = validateTitel(titel);
        this.vertragsart = Objects.requireNonNull(vertragsart);
        this.partnerId = Objects.requireNonNull(partnerId);
        this.status = Objects.requireNonNull(status);
        this.startDatum = startDatum;
        this.endDatum = endDatum;
        this.kuendigungsfristTage = kuendigungsfristTage;
        this.vertragsverantwortlicherUserId = vertragsverantwortlicherUserId;
        this.erstelltAm = Objects.requireNonNull(erstelltAm);
        this.erstelltVon = Objects.requireNonNull(erstelltVon);
        if (startDatum != null && endDatum != null && endDatum.isBefore(startDatum)) {
            throw new DomainException("MDA-CON-010", "endDatum darf nicht vor startDatum liegen");
        }
        if (kuendigungsfristTage != null && kuendigungsfristTage < 0) {
            throw new DomainException("MDA-CON-011", "kuendigungsfristTage darf nicht negativ sein");
        }
    }

    public static Vertrag erfassen(MandantId mandantId, String titel, Vertragsart vertragsart,
                                   PartnerId partnerId, LocalDate startDatum, LocalDate endDatum,
                                   Integer kuendigungsfristTage, UserId ersteller) {
        return new Vertrag(
                VertragId.newId(),
                mandantId,
                titel,
                vertragsart,
                partnerId,
                VertragStatus.ENTWURF,
                startDatum,
                endDatum,
                kuendigungsfristTage,
                ersteller,
                Instant.now(),
                ersteller);
    }

    public static Vertrag rehydrate(VertragId id, MandantId mandantId, String titel, Vertragsart vertragsart,
                                    PartnerId partnerId, VertragStatus status, LocalDate startDatum,
                                    LocalDate endDatum, Integer kuendigungsfristTage,
                                    UserId vertragsverantwortlicher, Instant erstelltAm, UserId erstelltVon,
                                    List<DokumentVersion> versionen) {
        Vertrag v = new Vertrag(id, mandantId, titel, vertragsart, partnerId, status, startDatum, endDatum,
                kuendigungsfristTage, vertragsverantwortlicher, erstelltAm, erstelltVon);
        v.versionen.addAll(versionen);
        return v;
    }

    public DokumentVersion neueVersionHochladen(String blobReferenz, String pruefsummeSha256,
                                                String dateiname, String mimeType, long groesseBytes,
                                                String aenderungskommentar, UserId hochladender) {
        if (status == VertragStatus.ARCHIVIERT || status == VertragStatus.BEENDET || status == VertragStatus.ABGELAUFEN) {
            throw new DomainException("MDA-CON-020",
                    "Vertrag im Status " + status + " akzeptiert keine neuen Versionen");
        }
        if (aenderungskommentar == null || aenderungskommentar.isBlank()) {
            throw new DomainException("MDA-CON-021", "Aenderungskommentar ist Pflicht");
        }
        int next = versionen.stream().mapToInt(DokumentVersion::versionNummer).max().orElse(0) + 1;
        DokumentVersion dv = new DokumentVersion(
                DokumentVersionId.newId(),
                id,
                next,
                blobReferenz,
                pruefsummeSha256,
                dateiname,
                mimeType,
                groesseBytes,
                aenderungskommentar,
                Instant.now(),
                hochladender);
        versionen.add(dv);
        if (status == VertragStatus.UEBERARBEITUNG || status == VertragStatus.FREIGEGEBEN) {
            this.status = VertragStatus.IN_PRUEFUNG;
        }
        return dv;
    }

    public void markiereEingereicht() {
        if (status != VertragStatus.ENTWURF && status != VertragStatus.UEBERARBEITUNG) {
            throw new DomainException("MDA-CON-030",
                    "Einreichen nur aus ENTWURF oder UEBERARBEITUNG erlaubt, aktuell=" + status);
        }
        if (versionen.isEmpty()) {
            throw new DomainException("MDA-CON-031", "Vertrag kann ohne hochgeladene Version nicht eingereicht werden");
        }
        this.status = VertragStatus.IN_PRUEFUNG;
    }

    public void markiereFreigegeben() {
        if (status != VertragStatus.IN_PRUEFUNG) {
            throw new DomainException("MDA-CON-032", "Freigeben nur aus IN_PRUEFUNG erlaubt, aktuell=" + status);
        }
        this.status = VertragStatus.FREIGEGEBEN;
    }

    public void markiereAbgelehnt() {
        if (status != VertragStatus.IN_PRUEFUNG) {
            throw new DomainException("MDA-CON-033", "Ablehnen nur aus IN_PRUEFUNG erlaubt, aktuell=" + status);
        }
        this.status = VertragStatus.UEBERARBEITUNG;
    }

    public void wechsleZu(VertragStatus neu) {
        this.status = Objects.requireNonNull(neu);
    }

    private static String validateTitel(String titel) {
        if (titel == null || titel.isBlank()) {
            throw new DomainException("MDA-CON-001", "Titel darf nicht leer sein");
        }
        if (titel.length() > 255) {
            throw new DomainException("MDA-CON-002", "Titel darf maximal 255 Zeichen lang sein");
        }
        return titel;
    }

    public VertragId id() { return id; }
    public MandantId mandantId() { return mandantId; }
    public String titel() { return titel; }
    public Vertragsart vertragsart() { return vertragsart; }
    public PartnerId partnerId() { return partnerId; }
    public VertragStatus status() { return status; }
    public LocalDate startDatum() { return startDatum; }
    public LocalDate endDatum() { return endDatum; }
    public Integer kuendigungsfristTage() { return kuendigungsfristTage; }
    public Optional<UserId> vertragsverantwortlicherUserId() { return Optional.ofNullable(vertragsverantwortlicherUserId); }
    public Instant erstelltAm() { return erstelltAm; }
    public UserId erstelltVon() { return erstelltVon; }
    public List<DokumentVersion> versionen() { return Collections.unmodifiableList(versionen); }

    public Optional<DokumentVersion> aktiveVersion() {
        return versionen.stream()
                .max((a, b) -> Integer.compare(a.versionNummer(), b.versionNummer()));
    }
}
