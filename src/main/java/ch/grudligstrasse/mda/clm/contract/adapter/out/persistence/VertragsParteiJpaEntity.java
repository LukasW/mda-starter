package ch.grudligstrasse.mda.clm.contract.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vertrag_partei")
public class VertragsParteiJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "rolle", nullable = false, length = 32)
    public String rolle;

    @Column(name = "person_id", nullable = false)
    public UUID personId;
}
