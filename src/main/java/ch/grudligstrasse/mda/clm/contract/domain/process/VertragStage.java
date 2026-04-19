package ch.grudligstrasse.mda.clm.contract.domain.process;

import java.util.EnumSet;
import java.util.Set;

/**
 * Stages des Vertragslifecycles gemaess Fachspec Kap. 9 (Statusmodell).
 */
public enum VertragStage {
    ENTWURF,
    IN_PRUEFUNG,
    KORREKTURBEDARF,
    FREIGEGEBEN,
    ZUR_SIGNATUR,
    UNTERZEICHNET,
    ARCHIVIERT,
    ABGELAUFEN,
    GEKUENDIGT;

    private static final Set<VertragStage> FINAL_STAGES = EnumSet.of(ABGELAUFEN, GEKUENDIGT);

    public boolean isFinal() {
        return FINAL_STAGES.contains(this);
    }
}
