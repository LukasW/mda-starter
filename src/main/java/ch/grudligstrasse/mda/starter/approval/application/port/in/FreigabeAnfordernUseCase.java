package ch.grudligstrasse.mda.starter.approval.application.port.in;

import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;

import java.util.UUID;

public interface FreigabeAnfordernUseCase {
    FreigabeId anfordern(Command cmd);

    record Command(UUID vertragId, UUID versionId, UUID reviewerId) {}
}
