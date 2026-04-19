package ch.grudligstrasse.mda.starter.contract.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dokument_version",
        uniqueConstraints = @UniqueConstraint(name = "uq_dokument_version", columnNames = {"vertrag_id", "version_nummer"}))
public class DokumentVersionJpaEntity {

    @Id
    public UUID id;

    @Column(name = "vertrag_id", nullable = false)
    public UUID vertragId;

    @Column(name = "version_nummer", nullable = false)
    public int versionNummer;

    @Column(name = "blob_referenz", nullable = false, length = 512)
    public String blobReferenz;

    @Column(name = "pruefsumme_sha256", nullable = false, length = 128)
    public String pruefsummeSha256;

    @Column(nullable = false, length = 255)
    public String dateiname;

    @Column(name = "mime_type", nullable = false, length = 128)
    public String mimeType;

    @Column(name = "groesse_bytes", nullable = false)
    public long groesseBytes;

    @Column(length = 2000)
    public String aenderungskommentar;

    @Column(name = "hochgeladen_am", nullable = false)
    public Instant hochgeladenAm;

    @Column(name = "hochgeladen_von", nullable = false)
    public UUID hochgeladenVon;
}
