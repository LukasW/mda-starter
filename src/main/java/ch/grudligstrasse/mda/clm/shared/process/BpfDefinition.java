package ch.grudligstrasse.mda.clm.shared.process;

import java.util.List;
import java.util.Map;

/**
 * Process-Definition. Pure Java — keine Framework-Imports.
 */
public interface BpfDefinition<S extends Enum<S>> {

    String processName();

    String aggregateType();

    S initial();

    Map<S, List<Transition<S>>> transitions();

    List<S> finalStages();

    default S resolve(S from, String trigger) {
        List<Transition<S>> outgoing = transitions().getOrDefault(from, List.of());
        for (Transition<S> t : outgoing) {
            if (t.trigger().equals(trigger)) {
                return t.to();
            }
        }
        return null;
    }

    record Transition<S extends Enum<S>>(String trigger, S to) {}
}
