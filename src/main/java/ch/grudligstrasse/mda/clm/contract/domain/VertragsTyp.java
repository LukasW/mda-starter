package ch.grudligstrasse.mda.clm.contract.domain;

public enum VertragsTyp {
    LIEFERANTENVERTRAG,
    KUNDENVERTRAG,
    ARBEITSVERTRAG,
    KOOPERATIONSVERTRAG,
    SONSTIGES;

    public static VertragsTyp parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("VertragsTyp darf nicht null sein.");
        }
        try {
            return VertragsTyp.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unbekannter VertragsTyp: " + value, e);
        }
    }
}
