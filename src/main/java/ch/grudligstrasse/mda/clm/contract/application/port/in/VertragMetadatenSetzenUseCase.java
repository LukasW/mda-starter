package ch.grudligstrasse.mda.clm.contract.application.port.in;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.time.LocalDate;

public interface VertragMetadatenSetzenUseCase {

    void execute(VertragMetadatenSetzenCommand cmd);

    record VertragMetadatenSetzenCommand(VertragId vertragId, String titel,
                                          LocalDate gueltigVon, LocalDate gueltigBis) {}
}
