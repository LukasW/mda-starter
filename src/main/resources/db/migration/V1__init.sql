-- CLM V1 — Initial schema (contract + person)
-- Additiv; diese Datei ist nach Commit unveraenderlich.

CREATE TABLE vertrag (
    id              UUID         NOT NULL,
    tenant_id       UUID         NOT NULL,
    titel           VARCHAR(200) NOT NULL,
    typ             VARCHAR(32)  NOT NULL,
    stage           VARCHAR(32)  NOT NULL,
    gueltig_von     DATE,
    gueltig_bis     DATE,
    ersteller_id    UUID         NOT NULL,
    erstellt_am     TIMESTAMP    NOT NULL,
    modified_at     TIMESTAMP    NOT NULL,
    version_number  BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_vertrag PRIMARY KEY (id)
);

CREATE INDEX ix_vertrag_tenant ON vertrag(tenant_id);
CREATE INDEX ix_vertrag_stage  ON vertrag(stage);

CREATE TABLE vertrag_partei (
    id          UUID         NOT NULL,
    vertrag_id  UUID         NOT NULL,
    rolle       VARCHAR(32)  NOT NULL,
    person_id   UUID         NOT NULL,
    CONSTRAINT pk_vertrag_partei PRIMARY KEY (id),
    CONSTRAINT fk_vertrag_partei_vertrag FOREIGN KEY (vertrag_id) REFERENCES vertrag(id) ON DELETE CASCADE
);
CREATE INDEX ix_vertrag_partei_vertrag ON vertrag_partei(vertrag_id);

CREATE TABLE vertrag_version (
    id               UUID         NOT NULL,
    vertrag_id       UUID         NOT NULL,
    version_nummer   INT          NOT NULL,
    erstellt         TIMESTAMP    NOT NULL,
    erstellt_von     UUID         NOT NULL,
    speicher_typ     VARCHAR(16)  NOT NULL,
    pfad_lokal       VARCHAR(512),
    archiv_extern_id VARCHAR(128),
    mime_type        VARCHAR(64)  NOT NULL,
    groesse_byte     BIGINT       NOT NULL,
    inhalt_hash      VARCHAR(128),
    CONSTRAINT pk_vertrag_version PRIMARY KEY (id),
    CONSTRAINT fk_vertrag_version_vertrag FOREIGN KEY (vertrag_id) REFERENCES vertrag(id) ON DELETE CASCADE,
    CONSTRAINT uq_vertrag_version UNIQUE (vertrag_id, version_nummer)
);
CREATE INDEX ix_vertrag_version_vertrag ON vertrag_version(vertrag_id);

CREATE TABLE person (
    id              UUID         NOT NULL,
    tenant_id       UUID         NOT NULL,
    vorname         VARCHAR(120) NOT NULL,
    nachname        VARCHAR(120) NOT NULL,
    email           VARCHAR(200) NOT NULL,
    organisation    VARCHAR(200),
    funktion        VARCHAR(120),
    quelle_typ      VARCHAR(16)  NOT NULL,
    externe_id      VARCHAR(128),
    erstellt_am     TIMESTAMP    NOT NULL,
    modified_at     TIMESTAMP    NOT NULL,
    version_number  BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_person PRIMARY KEY (id)
);
CREATE INDEX ix_person_tenant ON person(tenant_id);
CREATE INDEX ix_person_lastname ON person(nachname);
