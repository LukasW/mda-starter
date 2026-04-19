package ch.grudligstrasse.mda.clm.contract.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Vertragspartei: Kopplung ueber PersonId, keine direkte Person-Referenz (Cross-BC).
 */
public record VertragsPartei(ParteiRolle rolle, UUID personId) {

    public VertragsPartei {
        Objects.requireNonNull(rolle, "rolle darf nicht null sein.");
        Objects.requireNonNull(personId, "personId darf nicht null sein.");
    }
}
