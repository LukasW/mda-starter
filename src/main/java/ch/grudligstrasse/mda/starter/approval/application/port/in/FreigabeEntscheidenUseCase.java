package ch.grudligstrasse.mda.starter.approval.application.port.in;

import ch.grudligstrasse.mda.starter.approval.domain.Entscheidung;
import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;

public interface FreigabeEntscheidenUseCase {
    void entscheiden(Command cmd);

    record Command(FreigabeId freigabeId, Entscheidung entscheidung, String begruendung) {}
}
