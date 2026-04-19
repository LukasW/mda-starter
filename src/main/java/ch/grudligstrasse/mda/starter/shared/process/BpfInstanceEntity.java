package ch.grudligstrasse.mda.starter.shared.process;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bpf_instance")
public class BpfInstanceEntity {

    @Id
    public UUID id;

    @Column(name = "process_name", nullable = false, length = 128)
    public String processName;

    @Column(name = "aggregate_type", nullable = false, length = 128)
    public String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    public UUID aggregateId;

    @Column(name = "current_stage", nullable = false, length = 64)
    public String currentStage;

    @Column(name = "started_at", nullable = false)
    public Instant startedAt;

    @Column(name = "completed_at")
    public Instant completedAt;

    @Version
    @Column(name = "version_number", nullable = false)
    public long versionNumber;
}
