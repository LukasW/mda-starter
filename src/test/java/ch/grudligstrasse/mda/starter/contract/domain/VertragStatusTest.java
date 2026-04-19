package ch.grudligstrasse.mda.starter.contract.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VertragStatusTest {

    @Test
    void entwurf_und_ueberarbeitung_sind_editierbar() {
        assertTrue(VertragStatus.ENTWURF.isEditableByAntragsteller());
        assertTrue(VertragStatus.UEBERARBEITUNG.isEditableByAntragsteller());
    }

    @Test
    void andere_status_sind_nicht_editierbar() {
        assertFalse(VertragStatus.IN_PRUEFUNG.isEditableByAntragsteller());
        assertFalse(VertragStatus.FREIGEGEBEN.isEditableByAntragsteller());
        assertFalse(VertragStatus.ARCHIVIERT.isEditableByAntragsteller());
    }

    @Test
    void active_kennzeichnet_produktive_stati() {
        assertTrue(VertragStatus.FREIGEGEBEN.isActive());
        assertTrue(VertragStatus.AKTIV.isActive());
        assertTrue(VertragStatus.IN_KUENDIGUNG.isActive());
        assertFalse(VertragStatus.ENTWURF.isActive());
        assertFalse(VertragStatus.ARCHIVIERT.isActive());
    }
}
