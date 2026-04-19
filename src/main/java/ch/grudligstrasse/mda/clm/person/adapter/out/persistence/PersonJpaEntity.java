package ch.grudligstrasse.mda.clm.person.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "person")
public class PersonJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "vorname", nullable = false, length = 120)
    public String vorname;

    @Column(name = "nachname", nullable = false, length = 120)
    public String nachname;

    @Column(name = "email", nullable = false, length = 200)
    public String email;

    @Column(name = "organisation", length = 200)
    public String organisation;

    @Column(name = "funktion", length = 120)
    public String funktion;

    @Column(name = "quelle_typ", nullable = false, length = 16)
    public String quelleTyp;

    @Column(name = "externe_id", length = 128)
    public String externeId;

    @Column(name = "erstellt_am", nullable = false)
    public Instant erstelltAm;

    @Column(name = "modified_at", nullable = false)
    public Instant modifiedAt;

    @Version
    @Column(name = "version_number", nullable = false)
    public long versionNumber;
}
