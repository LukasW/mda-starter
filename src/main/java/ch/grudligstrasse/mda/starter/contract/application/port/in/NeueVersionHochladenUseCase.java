package ch.grudligstrasse.mda.starter.contract.application.port.in;

import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersionId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

public interface NeueVersionHochladenUseCase {

    DokumentVersionId hochladen(Command cmd);

    record Command(
            VertragId vertragId,
            String blobReferenz,
            String pruefsummeSha256,
            String dateiname,
            String mimeType,
            long groesseBytes,
            String aenderungskommentar,
            UserId hochladender
    ) {}
}
