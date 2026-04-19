-- V1 — Domaenen-Schema fuer CLM (Contract Management, Approval, Obligation)
-- H2- und PostgreSQL-kompatibel (keine DB-spezifischen Typen).

CREATE TABLE vertrag (
    id                                  UUID PRIMARY KEY,
    mandant_id                          UUID        NOT NULL,
    titel                               VARCHAR(255) NOT NULL,
    vertragsart                         VARCHAR(64) NOT NULL,
    partner_id                          UUID        NOT NULL,
    status                              VARCHAR(32) NOT NULL,
    start_datum                         DATE        NULL,
    end_datum                           DATE        NULL,
    kuendigungsfrist_tage               INTEGER     NULL,
    vertragsverantwortlicher_user_id    UUID        NULL,
    erstellt_am                         TIMESTAMP   NOT NULL,
    erstellt_von                        UUID        NOT NULL,
    version                             BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_vertrag_mandant ON vertrag (mandant_id);
CREATE INDEX idx_vertrag_status  ON vertrag (status);

CREATE TABLE dokument_version (
    id                  UUID PRIMARY KEY,
    vertrag_id          UUID         NOT NULL,
    version_nummer      INTEGER      NOT NULL,
    blob_referenz       VARCHAR(512) NOT NULL,
    pruefsumme_sha256   VARCHAR(128) NOT NULL,
    dateiname           VARCHAR(255) NOT NULL,
    mime_type           VARCHAR(128) NOT NULL,
    groesse_bytes       BIGINT       NOT NULL,
    aenderungskommentar VARCHAR(2000) NULL,
    hochgeladen_am      TIMESTAMP    NOT NULL,
    hochgeladen_von     UUID         NOT NULL,
    CONSTRAINT fk_dokument_vertrag FOREIGN KEY (vertrag_id) REFERENCES vertrag(id),
    CONSTRAINT uq_dokument_version UNIQUE (vertrag_id, version_nummer)
);

CREATE TABLE freigabe (
    id                  UUID PRIMARY KEY,
    vertrag_id          UUID         NOT NULL,
    version_id          UUID         NOT NULL,
    reviewer_user_id    UUID         NOT NULL,
    entscheidung        VARCHAR(32)  NULL,
    begruendung         VARCHAR(2000) NULL,
    entschieden_am      TIMESTAMP    NULL,
    angefordert_am      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_freigabe_vertrag ON freigabe (vertrag_id);

CREATE TABLE frist (
    id                       UUID PRIMARY KEY,
    vertrag_id               UUID        NOT NULL,
    art                      VARCHAR(32) NOT NULL,
    faelligkeits_datum       DATE        NOT NULL,
    vorlauf_tage             INTEGER     NOT NULL,
    erinnerungs_datum        DATE        NOT NULL,
    status                   VARCHAR(32) NOT NULL,
    verantwortlicher_user_id UUID        NOT NULL
);

CREATE INDEX idx_frist_vertrag ON frist (vertrag_id);
CREATE INDEX idx_frist_erinnerung ON frist (erinnerungs_datum);
