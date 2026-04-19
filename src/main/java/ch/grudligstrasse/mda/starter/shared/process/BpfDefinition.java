package ch.grudligstrasse.mda.starter.shared.process;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BpfDefinition<S extends Enum<S> & BpfStage, T extends Enum<T>> {

    private final String processName;
    private final String aggregateType;
    private final S initialStage;
    private final Set<S> terminalStages;
    private final Map<S, Map<T, S>> transitions;

    private BpfDefinition(String processName, String aggregateType, S initialStage,
                          Set<S> terminalStages, Map<S, Map<T, S>> transitions) {
        this.processName = processName;
        this.aggregateType = aggregateType;
        this.initialStage = initialStage;
        this.terminalStages = Set.copyOf(terminalStages);
        this.transitions = Collections.unmodifiableMap(transitions);
    }

    public String processName() { return processName; }
    public String aggregateType() { return aggregateType; }
    public S initialStage() { return initialStage; }
    public boolean isTerminal(S stage) { return terminalStages.contains(stage); }

    public S nextStage(S from, T trigger) {
        Map<T, S> byTrigger = transitions.get(from);
        if (byTrigger == null || !byTrigger.containsKey(trigger)) {
            throw new DomainException(
                    "MDA-BPF-001",
                    "Ungueltige Transition: " + processName + " from=" + from + " trigger=" + trigger);
        }
        return byTrigger.get(trigger);
    }

    public static <S extends Enum<S> & BpfStage, T extends Enum<T>> Builder<S, T> builder(
            String processName, String aggregateType, Class<S> stageClass, Class<T> triggerClass) {
        return new Builder<>(processName, aggregateType, stageClass, triggerClass);
    }

    public static final class Builder<S extends Enum<S> & BpfStage, T extends Enum<T>> {
        private final String processName;
        private final String aggregateType;
        private S initial;
        private final Set<S> terminals = new java.util.HashSet<>();
        private final Map<S, Map<T, S>> transitions = new HashMap<>();
        private final Class<S> stageClass;
        private final Class<T> triggerClass;

        private Builder(String processName, String aggregateType, Class<S> stageClass, Class<T> triggerClass) {
            this.processName = processName;
            this.aggregateType = aggregateType;
            this.stageClass = stageClass;
            this.triggerClass = triggerClass;
        }

        public Builder<S, T> initial(S stage) { this.initial = stage; return this; }
        public Builder<S, T> terminal(S stage) { this.terminals.add(stage); return this; }

        public Builder<S, T> allow(S from, T trigger, S to) {
            transitions.computeIfAbsent(from, k -> new HashMap<>()).put(trigger, to);
            return this;
        }

        public BpfDefinition<S, T> build() {
            if (initial == null) {
                throw new IllegalStateException("BPF-Definition ohne initialen Zustand: " + processName);
            }
            return new BpfDefinition<>(processName, aggregateType, initial, terminals, transitions);
        }

        public Class<S> stageClass() { return stageClass; }
        public Class<T> triggerClass() { return triggerClass; }
    }
}
