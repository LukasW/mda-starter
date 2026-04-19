package ch.grudligstrasse.mda.starter.approval.adapter.out.persistence;

import ch.grudligstrasse.mda.starter.approval.domain.Entscheidung;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "freigabe")
public class FreigabeJpaEntity {

    @Id
    public UUID id;

    @Column(name = "vertrag_id", nullable = false)
    public UUID vertragId;

    @Column(name = "version_id", nullable = false)
    public UUID versionId;

    @Column(name = "reviewer_user_id", nullable = false)
    public UUID reviewerUserId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    public Entscheidung entscheidung;

    @Column(length = 2000)
    public String begruendung;

    @Column(name = "entschieden_am")
    public Instant entschiedenAm;

    @Column(name = "angefordert_am", nullable = false)
    public Instant angefordertAm;
}
