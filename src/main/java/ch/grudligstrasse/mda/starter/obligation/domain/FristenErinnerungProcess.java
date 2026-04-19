package ch.grudligstrasse.mda.starter.obligation.domain;

import ch.grudligstrasse.mda.starter.shared.process.BpfDefinition;

public final class FristenErinnerungProcess {

    public static final String PROCESS_NAME = "FristenErinnerung";
    public static final String AGGREGATE_TYPE = "Frist";

    public static final BpfDefinition<FristStatus, FristTrigger> DEFINITION =
            BpfDefinition.<FristStatus, FristTrigger>builder(
                            PROCESS_NAME, AGGREGATE_TYPE, FristStatus.class, FristTrigger.class)
                    .initial(FristStatus.OFFEN)
                    .terminal(FristStatus.ERLEDIGT)
                    .allow(FristStatus.OFFEN,     FristTrigger.ERINNERUNG_AUSLOESEN, FristStatus.ERINNERT)
                    .allow(FristStatus.ERINNERT,  FristTrigger.SICHTEN,              FristStatus.ERLEDIGT)
                    .allow(FristStatus.ERINNERT,  FristTrigger.ESKALIEREN,           FristStatus.ESKALIERT)
                    .allow(FristStatus.ESKALIERT, FristTrigger.SICHTEN,              FristStatus.ERLEDIGT)
                    .build();

    private FristenErinnerungProcess() { }
}
