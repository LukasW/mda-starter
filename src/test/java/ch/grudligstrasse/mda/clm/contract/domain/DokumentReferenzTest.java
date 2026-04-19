package ch.grudligstrasse.mda.clm.contract.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DokumentReferenzTest {

    @Test
    void internBenoetigtPfad() {
        assertThrows(IllegalArgumentException.class,
                () -> new DokumentReferenz(SpeicherTyp.INTERN, null, null, "application/pdf", 100, "h"));
    }

    @Test
    void externBenoetigtArchivId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DokumentReferenz(SpeicherTyp.ARCHIV_EXTERN, null, null, "application/pdf", 100, "h"));
    }

    @Test
    void negativeGroesseUnzulaessig() {
        assertThrows(IllegalArgumentException.class,
                () -> DokumentReferenz.intern("/x", "application/pdf", -1L, "h"));
    }

    @Test
    void mitArchivIdWechseltSpeicherTyp() {
        DokumentReferenz intern = DokumentReferenz.intern("/x", "application/pdf", 10, "h");
        DokumentReferenz extern = intern.mitArchivId("ARCH-1");
        assertEquals(SpeicherTyp.ARCHIV_EXTERN, extern.speicherTyp());
        assertEquals("ARCH-1", extern.archivExternId());
    }
}
