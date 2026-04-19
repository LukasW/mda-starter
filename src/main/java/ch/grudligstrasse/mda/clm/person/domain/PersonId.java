package ch.grudligstrasse.mda.clm.person.domain;

import java.util.Objects;
import java.util.UUID;

public record PersonId(UUID value) {
    public PersonId {
        Objects.requireNonNull(value, "PersonId.value must not be null");
    }
    public static PersonId generate() { return new PersonId(UUID.randomUUID()); }
    public static PersonId parse(String v) { return new PersonId(UUID.fromString(v)); }
    public String asString() { return value.toString(); }
}
