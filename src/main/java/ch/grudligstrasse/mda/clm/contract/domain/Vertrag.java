package ch.grudligstrasse.mda.clm.contract.domain;

import ch.grudligstrasse.mda.clm.contract.domain.event.VertragDomainEvent;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragErstellt;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragPersonZugeordnet;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragStageGeaendert;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragVersionErstellt;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root fuer Vertraege.
 * <p>
 * Invarianten:
 * <ul>
 *   <li>titel darf nicht leer sein und maximal 200 Zeichen haben.</li>
 *   <li>Bei {@link #dokumentHochladen(UUID, DokumentReferenz)} entsteht eine neue Version; versionsnummer ist monoton.</li>
 *   <li>Die Aggregate-Stage ist die autoritative Stage und wird parallel von der BPF-Engine gespiegelt.</li>
 * </ul>
 */
public class Vertrag {

    private final VertragId id;
    private final UUID tenantId;
    private String titel;
    private VertragsTyp typ;
    private LocalDate gueltigVon;
    private LocalDate gueltigBis;
    private VertragStage stage;
    private final UUID erstellerId;
    private final Instant erstelltAm;
    private Instant modifiedAt;
    private long versionNumber;

    private final List<VertragsPartei> parteien = new ArrayList<>();
    private final List<VertragsVersion> versionen = new ArrayList<>();

    private final transient List<VertragDomainEvent> pendingEvents = new ArrayList<>();

    private Vertrag(VertragId id, UUID tenantId, String titel, VertragsTyp typ,
                    UUID erstellerId, VertragStage stage, Instant erstelltAm) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.titel = requireValidTitel(titel);
        this.typ = Objects.requireNonNull(typ);
        this.erstellerId = Objects.requireNonNull(erstellerId);
        this.stage = Objects.requireNonNull(stage);
        this.erstelltAm = Objects.requireNonNull(erstelltAm);
        this.modifiedAt = erstelltAm;
        this.versionNumber = 0;
    }

    public static Vertrag erstellen(String titel, VertragsTyp typ, UUID erstellerId, UUID tenantId) {
        VertragId id = VertragId.generate();
        Vertrag v = new Vertrag(id, tenantId, titel, typ, erstellerId, VertragStage.ENTWURF, Instant.now());
        v.pendingEvents.add(VertragErstellt.of(id, titel, typ, erstellerId, tenantId));
        return v;
    }

    public static Vertrag rehydrate(VertragId id, UUID tenantId, String titel, VertragsTyp typ,
                                    LocalDate gueltigVon, LocalDate gueltigBis,
                                    VertragStage stage, UUID erstellerId,
                                    Instant erstelltAm, Instant modifiedAt, long versionNumber,
                                    List<VertragsPartei> parteien, List<VertragsVersion> versionen) {
        Vertrag v = new Vertrag(id, tenantId, titel, typ, erstellerId, stage, erstelltAm);
        v.gueltigVon = gueltigVon;
        v.gueltigBis = gueltigBis;
        v.modifiedAt = modifiedAt;
        v.versionNumber = versionNumber;
        if (parteien != null) {
            v.parteien.addAll(parteien);
        }
        if (versionen != null) {
            v.versionen.addAll(versionen);
        }
        return v;
    }

    public void metadatenSetzen(String titel, LocalDate gueltigVon, LocalDate gueltigBis) {
        guardEditable();
        this.titel = requireValidTitel(titel);
        if (gueltigVon != null && gueltigBis != null && gueltigBis.isBefore(gueltigVon)) {
            throw new IllegalArgumentException("gueltigBis darf nicht vor gueltigVon liegen.");
        }
        this.gueltigVon = gueltigVon;
        this.gueltigBis = gueltigBis;
        touch();
    }

    public void dokumentHochladen(UUID erstelltVon, DokumentReferenz dokument) {
        guardEditable();
        Objects.requireNonNull(erstelltVon, "erstelltVon darf nicht null sein.");
        Objects.requireNonNull(dokument, "dokument darf nicht null sein.");
        int nextVersion = versionen.size() + 1;
        VertragsVersion version = new VertragsVersion(nextVersion, Instant.now(), erstelltVon, dokument);
        versionen.add(version);
        pendingEvents.add(VertragVersionErstellt.of(id, nextVersion, dokument.inhaltHash(), erstelltVon));
        touch();
    }

    public void personZuordnen(ParteiRolle rolle, UUID personId) {
        guardEditable();
        Objects.requireNonNull(rolle, "rolle darf nicht null sein.");
        Objects.requireNonNull(personId, "personId darf nicht null sein.");
        parteien.add(new VertragsPartei(rolle, personId));
        pendingEvents.add(VertragPersonZugeordnet.of(id, personId, rolle));
        touch();
    }

    public void stageWechseln(VertragStage neu, String trigger, String actor) {
        Objects.requireNonNull(neu, "neu darf nicht null sein.");
        if (neu == stage) {
            return;
        }
        VertragStage alt = this.stage;
        this.stage = neu;
        pendingEvents.add(VertragStageGeaendert.of(id, alt, neu, trigger, actor));
        touch();
    }

    private void guardEditable() {
        if (stage == VertragStage.ARCHIVIERT || stage.isFinal()) {
            throw new IllegalStateException("Vertrag ist nicht mehr editierbar (Stage=" + stage + ").");
        }
    }

    private static String requireValidTitel(String titel) {
        if (titel == null || titel.isBlank()) {
            throw new IllegalArgumentException("titel darf nicht leer sein.");
        }
        if (titel.length() > 200) {
            throw new IllegalArgumentException("titel darf maximal 200 Zeichen haben.");
        }
        return titel.trim();
    }

    private void touch() {
        this.modifiedAt = Instant.now();
        this.versionNumber++;
    }

    public List<VertragDomainEvent> pullEvents() {
        List<VertragDomainEvent> out = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return out;
    }

    public List<VertragDomainEvent> pendingEvents() {
        return Collections.unmodifiableList(pendingEvents);
    }

    public VertragId id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String titel() { return titel; }
    public VertragsTyp typ() { return typ; }
    public LocalDate gueltigVon() { return gueltigVon; }
    public LocalDate gueltigBis() { return gueltigBis; }
    public VertragStage stage() { return stage; }
    public UUID erstellerId() { return erstellerId; }
    public Instant erstelltAm() { return erstelltAm; }
    public Instant modifiedAt() { return modifiedAt; }
    public long versionNumber() { return versionNumber; }
    public List<VertragsPartei> parteien() { return Collections.unmodifiableList(parteien); }
    public List<VertragsVersion> versionen() { return Collections.unmodifiableList(versionen); }

    // mda-generator: manual-edits-below
}
