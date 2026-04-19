-- CLM V3 — Soft-Delete fuer Person (Feature: person-stammdaten-verwaltung)
-- Additiv; diese Datei ist nach Commit unveraenderlich.

ALTER TABLE person ADD COLUMN deleted_at TIMESTAMP NULL;
CREATE INDEX ix_person_deleted_at ON person(deleted_at);
