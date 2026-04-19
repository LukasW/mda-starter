package ch.grudligstrasse.mda.clm.shared;

import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import ch.grudligstrasse.mda.clm.shared.problem.ProblemDetail;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemDetailTest {

    @Test
    void ofBaut7807KompatiblenTypUrl() {
        ProblemDetail pd = ProblemDetail.of("Nicht gefunden", 404, "MDA-CON-404", "Vertrag fehlt");
        assertEquals(404, pd.status());
        assertEquals("MDA-CON-404", pd.code());
        assertTrue(pd.type().endsWith("MDA-CON-404"));
    }

    @Test
    void domainException_liefertStatusUndCode() {
        DomainException e = DomainException.conflict("MDA-BPF-002", "Aktive Instanz existiert.");
        assertEquals(409, e.status());
        assertEquals("MDA-BPF-002", e.code());
    }
}
