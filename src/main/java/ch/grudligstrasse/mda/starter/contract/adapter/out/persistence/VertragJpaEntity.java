package ch.grudligstrasse.mda.starter.contract.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vertrag")
public class VertragJpaEntity {

    @Id
    public UUID id;

    @Column(name = "mandant_id", nullable = false)
    public UUID mandantId;

    @Column(nullable = false, length = 255)
    public String titel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    public ch.grudligstrasse.mda.starter.contract.domain.Vertragsart vertragsart;

    @Column(name = "partner_id", nullable = false)
    public UUID partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public ch.grudligstrasse.mda.starter.contract.domain.VertragStatus status;

    @Column(name = "start_datum")
    public LocalDate startDatum;

    @Column(name = "end_datum")
    public LocalDate endDatum;

    @Column(name = "kuendigungsfrist_tage")
    public Integer kuendigungsfristTage;

    @Column(name = "vertragsverantwortlicher_user_id")
    public UUID vertragsverantwortlicherUserId;

    @Column(name = "erstellt_am", nullable = false)
    public Instant erstelltAm;

    @Column(name = "erstellt_von", nullable = false)
    public UUID erstelltVon;

    @Version
    @Column(nullable = false)
    public long version;
}
