package ch.grudligstrasse.mda.starter.obligation.domain;

import ch.grudligstrasse.mda.starter.shared.process.BpfStage;

public enum FristStatus implements BpfStage {
    OFFEN,
    ERINNERT,
    ERLEDIGT,
    ESKALIERT
}
