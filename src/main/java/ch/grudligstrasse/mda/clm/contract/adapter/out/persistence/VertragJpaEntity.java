package ch.grudligstrasse.mda.clm.contract.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vertrag")
public class VertragJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "titel", nullable = false, length = 200)
    public String titel;

    @Column(name = "typ", nullable = false, length = 32)
    public String typ;

    @Column(name = "stage", nullable = false, length = 32)
    public String stage;

    @Column(name = "gueltig_von")
    public LocalDate gueltigVon;

    @Column(name = "gueltig_bis")
    public LocalDate gueltigBis;

    @Column(name = "ersteller_id", nullable = false)
    public UUID erstellerId;

    @Column(name = "erstellt_am", nullable = false)
    public Instant erstelltAm;

    @Column(name = "modified_at", nullable = false)
    public Instant modifiedAt;

    @Version
    @Column(name = "version_number", nullable = false)
    public long versionNumber;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "vertrag_id", nullable = false)
    @OrderBy("rolle ASC")
    public List<VertragsParteiJpaEntity> parteien = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "vertrag_id", nullable = false)
    @OrderBy("versionNummer ASC")
    public List<VertragsVersionJpaEntity> versionen = new ArrayList<>();
}
