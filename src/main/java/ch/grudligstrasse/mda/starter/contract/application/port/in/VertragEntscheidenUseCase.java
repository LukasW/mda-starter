package ch.grudligstrasse.mda.starter.contract.application.port.in;

import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

public interface VertragEntscheidenUseCase {

    void genehmigen(GenehmigenCommand cmd);
    void ablehnen(AblehnenCommand cmd);

    record GenehmigenCommand(VertragId vertragId, UserId reviewerId) {}
    record AblehnenCommand(VertragId vertragId, UserId reviewerId, String begruendung) {}
}
