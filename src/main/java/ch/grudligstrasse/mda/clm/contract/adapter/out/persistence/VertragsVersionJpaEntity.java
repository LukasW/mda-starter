package ch.grudligstrasse.mda.clm.contract.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vertrag_version")
public class VertragsVersionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "version_nummer", nullable = false)
    public int versionNummer;

    @Column(name = "erstellt", nullable = false)
    public Instant erstellt;

    @Column(name = "erstellt_von", nullable = false)
    public UUID erstelltVon;

    @Column(name = "speicher_typ", nullable = false, length = 16)
    public String speicherTyp;

    @Column(name = "pfad_lokal", length = 512)
    public String pfadLokal;

    @Column(name = "archiv_extern_id", length = 128)
    public String archivExternId;

    @Column(name = "mime_type", nullable = false, length = 64)
    public String mimeType;

    @Column(name = "groesse_byte", nullable = false)
    public long groesseByte;

    @Column(name = "inhalt_hash", length = 128)
    public String inhaltHash;
}
