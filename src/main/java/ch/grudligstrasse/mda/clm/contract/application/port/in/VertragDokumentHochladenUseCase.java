package ch.grudligstrasse.mda.clm.contract.application.port.in;

import ch.grudligstrasse.mda.clm.contract.domain.DokumentReferenz;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.util.UUID;

public interface VertragDokumentHochladenUseCase {

    void execute(VertragDokumentHochladenCommand cmd);

    record VertragDokumentHochladenCommand(VertragId vertragId, UUID erstelltVon, DokumentReferenz dokument) {}
}
