package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VertragLifecycleTest {

    @Test
    void golden_path_bis_aktiv() {
        var def = VertragLifecycle.DEFINITION;
        assertEquals(VertragStatus.IN_PRUEFUNG,    def.nextStage(VertragStatus.ENTWURF,      VertragTrigger.EINREICHEN));
        assertEquals(VertragStatus.FREIGEGEBEN,    def.nextStage(VertragStatus.IN_PRUEFUNG,  VertragTrigger.GENEHMIGEN));
        assertEquals(VertragStatus.AKTIV,          def.nextStage(VertragStatus.FREIGEGEBEN,  VertragTrigger.START_DATUM_ERREICHT));
    }

    @Test
    void ueberarbeitung_schleife() {
        var def = VertragLifecycle.DEFINITION;
        assertEquals(VertragStatus.UEBERARBEITUNG, def.nextStage(VertragStatus.IN_PRUEFUNG,   VertragTrigger.ABLEHNEN));
        assertEquals(VertragStatus.IN_PRUEFUNG,    def.nextStage(VertragStatus.UEBERARBEITUNG, VertragTrigger.NEUE_VERSION_HOCHLADEN));
    }

    @Test
    void retention_uebergang() {
        var def = VertragLifecycle.DEFINITION;
        assertEquals(VertragStatus.ARCHIVIERT, def.nextStage(VertragStatus.ABGELAUFEN, VertragTrigger.RETENTION_FRIST_LAEUFT));
        assertEquals(VertragStatus.ARCHIVIERT, def.nextStage(VertragStatus.BEENDET,    VertragTrigger.RETENTION_FRIST_LAEUFT));
        assertTrue(def.isTerminal(VertragStatus.ARCHIVIERT));
    }

    @Test
    void verbotener_uebergang_wirft_MDA_BPF_001() {
        var def = VertragLifecycle.DEFINITION;
        DomainException ex = assertThrows(DomainException.class, () ->
                def.nextStage(VertragStatus.ENTWURF, VertragTrigger.GENEHMIGEN));
        assertEquals("MDA-BPF-001", ex.code());
    }
}
