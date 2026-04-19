-- V2 — BPF-Statemachine (Spec Kap. 12)

CREATE TABLE bpf_instance (
    id              UUID PRIMARY KEY,
    process_name    VARCHAR(128) NOT NULL,
    aggregate_type  VARCHAR(128) NOT NULL,
    aggregate_id    UUID         NOT NULL,
    current_stage   VARCHAR(64)  NOT NULL,
    started_at      TIMESTAMP    NOT NULL,
    completed_at    TIMESTAMP    NULL,
    version_number  BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_bpf_instance UNIQUE (process_name, aggregate_type, aggregate_id)
);

CREATE TABLE bpf_transition_log (
    id              UUID PRIMARY KEY,
    instance_id     UUID         NOT NULL,
    from_stage      VARCHAR(64)  NULL,
    to_stage        VARCHAR(64)  NOT NULL,
    trigger_name    VARCHAR(64)  NOT NULL,
    occurred_at     TIMESTAMP    NOT NULL,
    actor           VARCHAR(128) NOT NULL,
    CONSTRAINT fk_bpf_trans_instance FOREIGN KEY (instance_id) REFERENCES bpf_instance(id)
);

CREATE INDEX idx_bpf_instance_lookup ON bpf_instance (aggregate_type, aggregate_id);
CREATE INDEX idx_bpf_trans_instance ON bpf_transition_log (instance_id);
