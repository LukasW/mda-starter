package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.process.BpfDefinition;

public final class VertragLifecycle {

    public static final String PROCESS_NAME = "VertragLifecycle";
    public static final String AGGREGATE_TYPE = "Vertrag";

    public static final BpfDefinition<VertragStatus, VertragTrigger> DEFINITION =
            BpfDefinition.<VertragStatus, VertragTrigger>builder(
                            PROCESS_NAME, AGGREGATE_TYPE, VertragStatus.class, VertragTrigger.class)
                    .initial(VertragStatus.ENTWURF)
                    .terminal(VertragStatus.ARCHIVIERT)
                    .allow(VertragStatus.ENTWURF,        VertragTrigger.EINREICHEN,             VertragStatus.IN_PRUEFUNG)
                    .allow(VertragStatus.IN_PRUEFUNG,    VertragTrigger.GENEHMIGEN,             VertragStatus.FREIGEGEBEN)
                    .allow(VertragStatus.IN_PRUEFUNG,    VertragTrigger.ABLEHNEN,               VertragStatus.UEBERARBEITUNG)
                    .allow(VertragStatus.UEBERARBEITUNG, VertragTrigger.NEUE_VERSION_HOCHLADEN, VertragStatus.IN_PRUEFUNG)
                    .allow(VertragStatus.FREIGEGEBEN,    VertragTrigger.START_DATUM_ERREICHT,   VertragStatus.AKTIV)
                    .allow(VertragStatus.AKTIV,          VertragTrigger.KUENDIGUNG_AUSLOESEN,   VertragStatus.IN_KUENDIGUNG)
                    .allow(VertragStatus.AKTIV,          VertragTrigger.END_DATUM_ERREICHT,     VertragStatus.ABGELAUFEN)
                    .allow(VertragStatus.IN_KUENDIGUNG,  VertragTrigger.KUENDIGUNG_WIRKSAM,     VertragStatus.BEENDET)
                    .allow(VertragStatus.ABGELAUFEN,     VertragTrigger.RETENTION_FRIST_LAEUFT, VertragStatus.ARCHIVIERT)
                    .allow(VertragStatus.BEENDET,        VertragTrigger.RETENTION_FRIST_LAEUFT, VertragStatus.ARCHIVIERT)
                    .build();

    private VertragLifecycle() { }
}
