package ch.grudligstrasse.mda.starter.obligation.application.port.in;

import ch.grudligstrasse.mda.starter.obligation.domain.FristId;

import java.util.UUID;

public interface FristSichtenUseCase {
    void sichten(Command cmd);

    record Command(FristId fristId, UUID sichtenderUserId) {}
}
