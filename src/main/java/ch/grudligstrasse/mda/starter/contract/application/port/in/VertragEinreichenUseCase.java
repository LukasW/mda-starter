package ch.grudligstrasse.mda.starter.contract.application.port.in;

import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

public interface VertragEinreichenUseCase {

    void einreichen(Command cmd);

    record Command(VertragId vertragId, UserId antragstellerId) {}
}
