package ch.grudligstrasse.mda.starter.obligation.adapter.out.persistence;

import ch.grudligstrasse.mda.starter.obligation.domain.FristArt;
import ch.grudligstrasse.mda.starter.obligation.domain.FristStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "frist")
public class FristJpaEntity {

    @Id
    public UUID id;

    @Column(name = "vertrag_id", nullable = false)
    public UUID vertragId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public FristArt art;

    @Column(name = "faelligkeits_datum", nullable = false)
    public LocalDate faelligkeitsDatum;

    @Column(name = "vorlauf_tage", nullable = false)
    public int vorlaufTage;

    @Column(name = "erinnerungs_datum", nullable = false)
    public LocalDate erinnerungsDatum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public FristStatus status;

    @Column(name = "verantwortlicher_user_id", nullable = false)
    public UUID verantwortlicherUserId;
}
