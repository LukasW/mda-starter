package ch.grudligstrasse.mda.clm.person.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {
        Objects.requireNonNull(value, "Email.value must not be null");
        String normalized = value.trim().toLowerCase();
        if (!RE.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Ungültige E-Mail-Adresse: " + value);
        }
        value = normalized;
    }
}
