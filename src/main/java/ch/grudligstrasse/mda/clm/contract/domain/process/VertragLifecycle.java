package ch.grudligstrasse.mda.clm.contract.domain.process;

import ch.grudligstrasse.mda.clm.shared.process.BpfDefinition;

import java.util.List;
import java.util.Map;

public final class VertragLifecycle implements BpfDefinition<VertragStage> {

    public static final String PROCESS_NAME = "contract";
    public static final String AGGREGATE_TYPE = "Vertrag";

    public static final String TRIGGER_EINREICHEN = "einreichen";
    public static final String TRIGGER_FREIGEBEN = "freigeben";
    public static final String TRIGGER_KORREKTUR = "korrekturbeantragen";
    public static final String TRIGGER_ZUR_SIGNATUR = "zurSignaturSenden";
    public static final String TRIGGER_UNTERZEICHNEN = "unterzeichnen";
    public static final String TRIGGER_ARCHIVIEREN = "archivieren";
    public static final String TRIGGER_ABLAUFEN = "ablaufen";
    public static final String TRIGGER_KUENDIGEN = "kuendigen";

    private static final VertragLifecycle INSTANCE = new VertragLifecycle();

    public static VertragLifecycle instance() {
        return INSTANCE;
    }

    private VertragLifecycle() {
    }

    @Override
    public String processName() {
        return PROCESS_NAME;
    }

    @Override
    public String aggregateType() {
        return AGGREGATE_TYPE;
    }

    @Override
    public VertragStage initial() {
        return VertragStage.ENTWURF;
    }

    @Override
    public Map<VertragStage, List<Transition<VertragStage>>> transitions() {
        return Map.of(
                VertragStage.ENTWURF, List.of(
                        new Transition<>(TRIGGER_EINREICHEN, VertragStage.IN_PRUEFUNG)),
                VertragStage.IN_PRUEFUNG, List.of(
                        new Transition<>(TRIGGER_FREIGEBEN, VertragStage.FREIGEGEBEN),
                        new Transition<>(TRIGGER_KORREKTUR, VertragStage.KORREKTURBEDARF)),
                VertragStage.KORREKTURBEDARF, List.of(
                        new Transition<>(TRIGGER_EINREICHEN, VertragStage.IN_PRUEFUNG)),
                VertragStage.FREIGEGEBEN, List.of(
                        new Transition<>(TRIGGER_ZUR_SIGNATUR, VertragStage.ZUR_SIGNATUR)),
                VertragStage.ZUR_SIGNATUR, List.of(
                        new Transition<>(TRIGGER_UNTERZEICHNEN, VertragStage.UNTERZEICHNET)),
                VertragStage.UNTERZEICHNET, List.of(
                        new Transition<>(TRIGGER_ARCHIVIEREN, VertragStage.ARCHIVIERT)),
                VertragStage.ARCHIVIERT, List.of(
                        new Transition<>(TRIGGER_ABLAUFEN, VertragStage.ABGELAUFEN),
                        new Transition<>(TRIGGER_KUENDIGEN, VertragStage.GEKUENDIGT)));
    }

    @Override
    public List<VertragStage> finalStages() {
        return List.of(VertragStage.ABGELAUFEN, VertragStage.GEKUENDIGT);
    }
}
