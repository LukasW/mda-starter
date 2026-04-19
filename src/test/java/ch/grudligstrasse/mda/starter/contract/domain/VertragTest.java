package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VertragTest {

    private static Vertrag erzeugeEntwurf() {
        return Vertrag.erfassen(
                MandantId.of(UUID.randomUUID()),
                "Rahmenvertrag IT",
                Vertragsart.DIENSTLEISTUNG,
                PartnerId.of(UUID.randomUUID()),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                90,
                UserId.of(UUID.randomUUID()));
    }

    @Test
    void erfassen_ist_im_status_entwurf() {
        Vertrag v = erzeugeEntwurf();
        assertEquals(VertragStatus.ENTWURF, v.status());
        assertNotNull(v.id());
    }

    @Test
    void leerer_titel_wird_abgelehnt() {
        DomainException ex = assertThrows(DomainException.class, () ->
                Vertrag.erfassen(
                        MandantId.of(UUID.randomUUID()),
                        "   ",
                        Vertragsart.MIETE,
                        PartnerId.of(UUID.randomUUID()),
                        null, null, null,
                        UserId.of(UUID.randomUUID())));
        assertEquals("MDA-CON-001", ex.code());
    }

    @Test
    void endDatum_vor_startDatum_wird_abgelehnt() {
        DomainException ex = assertThrows(DomainException.class, () ->
                Vertrag.erfassen(
                        MandantId.of(UUID.randomUUID()),
                        "X",
                        Vertragsart.MIETE,
                        PartnerId.of(UUID.randomUUID()),
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 1, 1),
                        null,
                        UserId.of(UUID.randomUUID())));
        assertEquals("MDA-CON-010", ex.code());
    }

    @Test
    void version_hochladen_setzt_status_auf_pruefung_aus_freigegeben() {
        Vertrag v = erzeugeEntwurf();
        v.neueVersionHochladen("blob://a", "sha", "vertrag.pdf", "application/pdf", 42, "initial", UserId.of(UUID.randomUUID()));
        v.markiereEingereicht();
        v.markiereFreigegeben();
        assertEquals(VertragStatus.FREIGEGEBEN, v.status());
        v.neueVersionHochladen("blob://b", "sha2", "vertrag.pdf", "application/pdf", 42, "update", UserId.of(UUID.randomUUID()));
        assertEquals(VertragStatus.IN_PRUEFUNG, v.status());
    }

    @Test
    void einreichen_ohne_version_wird_abgelehnt() {
        Vertrag v = erzeugeEntwurf();
        DomainException ex = assertThrows(DomainException.class, v::markiereEingereicht);
        assertEquals("MDA-CON-031", ex.code());
    }

    @Test
    void aenderungskommentar_ist_pflicht() {
        Vertrag v = erzeugeEntwurf();
        DomainException ex = assertThrows(DomainException.class, () ->
                v.neueVersionHochladen("blob://a", "sha", "v.pdf", "application/pdf", 1, "   ", UserId.of(UUID.randomUUID())));
        assertEquals("MDA-CON-021", ex.code());
    }

    @Test
    void aktive_version_ist_hoechste_versionNummer() {
        Vertrag v = erzeugeEntwurf();
        v.neueVersionHochladen("blob://a", "sha1", "v.pdf", "application/pdf", 1, "erste", UserId.of(UUID.randomUUID()));
        v.neueVersionHochladen("blob://b", "sha2", "v.pdf", "application/pdf", 1, "zweite", UserId.of(UUID.randomUUID()));
        assertEquals(2, v.aktiveVersion().orElseThrow().versionNummer());
    }
}
