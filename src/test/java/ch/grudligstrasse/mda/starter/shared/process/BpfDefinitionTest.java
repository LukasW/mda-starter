package ch.grudligstrasse.mda.starter.shared.process;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BpfDefinitionTest {

    enum S implements BpfStage { A, B, C }
    enum T { GO, SKIP }

    @Test
    void erlaubter_uebergang_wird_geliefert() {
        BpfDefinition<S, T> def = BpfDefinition.<S, T>builder("demo", "Aggr", S.class, T.class)
                .initial(S.A)
                .terminal(S.C)
                .allow(S.A, T.GO, S.B)
                .allow(S.B, T.GO, S.C)
                .build();
        assertEquals(S.B, def.nextStage(S.A, T.GO));
        assertTrue(def.isTerminal(S.C));
    }

    @Test
    void unbekannte_transition_wirft_MDA_BPF_001() {
        BpfDefinition<S, T> def = BpfDefinition.<S, T>builder("demo", "Aggr", S.class, T.class)
                .initial(S.A)
                .allow(S.A, T.GO, S.B)
                .build();
        DomainException ex = assertThrows(DomainException.class, () -> def.nextStage(S.A, T.SKIP));
        assertEquals("MDA-BPF-001", ex.code());
    }
}
