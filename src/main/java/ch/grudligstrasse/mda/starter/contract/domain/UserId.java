package ch.grudligstrasse.mda.starter.contract.domain;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        Objects.requireNonNull(value, "UserId darf nicht null sein");
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }
}
