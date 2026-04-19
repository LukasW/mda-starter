package ch.grudligstrasse.mda.starter.contract.adapter.in.rest;

import ch.grudligstrasse.mda.starter.contract.domain.Vertragsart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public final class VertragRequests {

    public record Erfassen(
            @NotNull UUID mandantId,
            @NotBlank String titel,
            @NotNull Vertragsart vertragsart,
            @NotNull UUID partnerId,
            LocalDate startDatum,
            LocalDate endDatum,
            Integer kuendigungsfristTage,
            @NotNull UUID antragstellerId
    ) {}

    public record Einreichen(@NotNull UUID antragstellerId) {}

    public record Genehmigen(@NotNull UUID reviewerId) {}

    public record Ablehnen(@NotNull UUID reviewerId, @NotBlank String begruendung) {}

    public record NeueVersion(
            @NotBlank String blobReferenz,
            @NotBlank String pruefsummeSha256,
            @NotBlank String dateiname,
            @NotBlank String mimeType,
            long groesseBytes,
            @NotBlank String aenderungskommentar,
            @NotNull UUID hochladender
    ) {}

    private VertragRequests() {}
}
