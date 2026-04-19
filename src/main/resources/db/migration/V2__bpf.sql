-- CLM V2 — BPF runtime tables (shared across bounded contexts)
CREATE TABLE bpf_instance (
    id              UUID         NOT NULL,
    tenant_id       UUID         NOT NULL,
    process_name    VARCHAR(64)  NOT NULL,
    aggregate_id    UUID         NOT NULL,
    aggregate_type  VARCHAR(64)  NOT NULL,
    current_stage   VARCHAR(64)  NOT NULL,
    started_at      TIMESTAMP    NOT NULL,
    completed_at    TIMESTAMP,
    version_number  BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_bpf_instance PRIMARY KEY (id)
);
-- Partielle Eindeutigkeit (aktive Instanz pro Aggregat) wird durch
-- BpfService.start() applikationsseitig garantiert; H2 unterstuetzt filtered
-- UNIQUE INDEX nicht. Spaeter kann PostgreSQL dies per Migration nachziehen.
CREATE INDEX ix_bpf_instance_active
    ON bpf_instance(tenant_id, process_name, aggregate_id);

CREATE TABLE bpf_transition_log (
    id            UUID         NOT NULL,
    instance_id   UUID         NOT NULL,
    from_stage    VARCHAR(64),
    to_stage      VARCHAR(64)  NOT NULL,
    trigger_name  VARCHAR(64)  NOT NULL,
    guard_result  VARCHAR(16),
    occurred_at   TIMESTAMP    NOT NULL,
    actor         VARCHAR(128) NOT NULL,
    CONSTRAINT pk_bpf_transition PRIMARY KEY (id),
    CONSTRAINT fk_bpf_transition_instance FOREIGN KEY (instance_id) REFERENCES bpf_instance(id)
);
CREATE INDEX ix_bpf_tx_instance ON bpf_transition_log(instance_id);
