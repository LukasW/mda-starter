package ch.grudligstrasse.mda.clm.shared.process;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bpf_transition_log")
public class BpfTransitionLogEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "instance_id", nullable = false)
    public UUID instanceId;

    @Column(name = "from_stage", length = 64)
    public String fromStage;

    @Column(name = "to_stage", nullable = false, length = 64)
    public String toStage;

    @Column(name = "trigger_name", nullable = false, length = 64)
    public String triggerName;

    @Column(name = "guard_result", length = 16)
    public String guardResult;

    @Column(name = "occurred_at", nullable = false)
    public Instant occurredAt;

    @Column(name = "actor", nullable = false, length = 128)
    public String actor;
}
