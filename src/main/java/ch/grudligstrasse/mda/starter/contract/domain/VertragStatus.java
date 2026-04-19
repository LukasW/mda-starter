package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.process.BpfStage;

public enum VertragStatus implements BpfStage {
    ENTWURF,
    IN_PRUEFUNG,
    UEBERARBEITUNG,
    FREIGEGEBEN,
    AKTIV,
    IN_KUENDIGUNG,
    BEENDET,
    ABGELAUFEN,
    ARCHIVIERT;

    public boolean isEditableByAntragsteller() {
        return this == ENTWURF || this == UEBERARBEITUNG;
    }

    public boolean isActive() {
        return this == FREIGEGEBEN || this == AKTIV || this == IN_KUENDIGUNG;
    }
}
