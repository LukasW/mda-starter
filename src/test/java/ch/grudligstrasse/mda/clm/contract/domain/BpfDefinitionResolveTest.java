package ch.grudligstrasse.mda.clm.contract.domain;

import ch.grudligstrasse.mda.clm.contract.domain.process.VertragLifecycle;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpfDefinitionResolveTest {

    private final VertragLifecycle lc = VertragLifecycle.instance();

    @Test
    void transitionsMapEnthaeltKeineFinalStages() {
        for (VertragStage s : lc.finalStages()) {
            assertTrue(!lc.transitions().containsKey(s));
        }
    }

    @Test
    void nameUndTypSindKonstant() {
        assertEquals("contract", lc.processName());
        assertEquals("Vertrag", lc.aggregateType());
        assertEquals(VertragStage.ENTWURF, lc.initial());
    }

    @Test
    void resolveMitUnbekanntemTriggerLiefertNull() {
        assertEquals(null, lc.resolve(VertragStage.ENTWURF, "blabla"));
    }
}
