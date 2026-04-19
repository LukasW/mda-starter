package ch.grudligstrasse.mda.clm.contract.domain;

import java.util.Objects;

/**
 * Fachliche Referenz auf das Vertragsdokument. Bleibt im CLM auch dann erhalten,
 * wenn das Dokument extern archiviert ist.
 */
public record DokumentReferenz(
        SpeicherTyp speicherTyp,
        String pfadLokal,
        String archivExternId,
        String mimeType,
        long groesseByte,
        String inhaltHash) {

    public DokumentReferenz {
        Objects.requireNonNull(speicherTyp, "speicherTyp darf nicht null sein.");
        Objects.requireNonNull(mimeType, "mimeType darf nicht null sein.");
        if (groesseByte < 0) {
            throw new IllegalArgumentException("groesseByte darf nicht negativ sein.");
        }
        if (speicherTyp == SpeicherTyp.INTERN && (pfadLokal == null || pfadLokal.isBlank())) {
            throw new IllegalArgumentException("pfadLokal ist Pflicht bei SpeicherTyp=INTERN.");
        }
        if (speicherTyp == SpeicherTyp.ARCHIV_EXTERN && (archivExternId == null || archivExternId.isBlank())) {
            throw new IllegalArgumentException("archivExternId ist Pflicht bei SpeicherTyp=ARCHIV_EXTERN.");
        }
    }

    public static DokumentReferenz intern(String pfad, String mimeType, long groesseByte, String inhaltHash) {
        return new DokumentReferenz(SpeicherTyp.INTERN, pfad, null, mimeType, groesseByte, inhaltHash);
    }

    public DokumentReferenz mitArchivId(String archivId) {
        return new DokumentReferenz(SpeicherTyp.ARCHIV_EXTERN, pfadLokal, archivId, mimeType, groesseByte, inhaltHash);
    }
}
