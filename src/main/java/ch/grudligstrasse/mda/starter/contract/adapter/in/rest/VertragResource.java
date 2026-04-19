package ch.grudligstrasse.mda.starter.contract.adapter.in.rest;

import ch.grudligstrasse.mda.starter.contract.application.port.in.NeueVersionHochladenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragEinreichenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragEntscheidenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragErfassenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragLadenQuery;
import ch.grudligstrasse.mda.starter.contract.domain.MandantId;
import ch.grudligstrasse.mda.starter.contract.domain.PartnerId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/vertraege")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VertragResource {

    @Inject VertragErfassenUseCase erfassen;
    @Inject VertragEinreichenUseCase einreichen;
    @Inject VertragEntscheidenUseCase entscheiden;
    @Inject NeueVersionHochladenUseCase hochladen;
    @Inject VertragLadenQuery laden;

    @POST
    public Response erfassen(@Valid VertragRequests.Erfassen req) {
        VertragId id = erfassen.erfassen(new VertragErfassenUseCase.Command(
                MandantId.of(req.mandantId()),
                req.titel(),
                req.vertragsart(),
                PartnerId.of(req.partnerId()),
                req.startDatum(),
                req.endDatum(),
                req.kuendigungsfristTage(),
                UserId.of(req.antragstellerId())));
        VertragDto body = VertragDto.from(laden.laden(id).orElseThrow());
        return Response.created(URI.create("/api/v1/vertraege/" + id.value())).entity(body).build();
    }

    @GET
    public List<VertragDto> alle() {
        return laden.alle().stream().map(VertragDto::from).toList();
    }

    @GET
    @Path("/{id}")
    public VertragDto einzeln(@PathParam("id") UUID id) {
        return laden.laden(VertragId.of(id))
                .map(VertragDto::from)
                .orElseThrow(() -> new DomainException("MDA-CON-NOT_FOUND", "Vertrag nicht gefunden: " + id));
    }

    @POST
    @Path("/{id}/einreichen")
    public VertragDto einreichen(@PathParam("id") UUID id, @Valid VertragRequests.Einreichen req) {
        einreichen.einreichen(new VertragEinreichenUseCase.Command(
                VertragId.of(id), UserId.of(req.antragstellerId())));
        return VertragDto.from(laden.laden(VertragId.of(id)).orElseThrow());
    }

    @POST
    @Path("/{id}/genehmigen")
    public VertragDto genehmigen(@PathParam("id") UUID id, @Valid VertragRequests.Genehmigen req) {
        entscheiden.genehmigen(new VertragEntscheidenUseCase.GenehmigenCommand(
                VertragId.of(id), UserId.of(req.reviewerId())));
        return VertragDto.from(laden.laden(VertragId.of(id)).orElseThrow());
    }

    @POST
    @Path("/{id}/ablehnen")
    public VertragDto ablehnen(@PathParam("id") UUID id, @Valid VertragRequests.Ablehnen req) {
        entscheiden.ablehnen(new VertragEntscheidenUseCase.AblehnenCommand(
                VertragId.of(id), UserId.of(req.reviewerId()), req.begruendung()));
        return VertragDto.from(laden.laden(VertragId.of(id)).orElseThrow());
    }

    @POST
    @Path("/{id}/versionen")
    public VertragDto versionHochladen(@PathParam("id") UUID id, @Valid VertragRequests.NeueVersion req) {
        hochladen.hochladen(new NeueVersionHochladenUseCase.Command(
                VertragId.of(id),
                req.blobReferenz(),
                req.pruefsummeSha256(),
                req.dateiname(),
                req.mimeType(),
                req.groesseBytes(),
                req.aenderungskommentar(),
                UserId.of(req.hochladender())));
        return VertragDto.from(laden.laden(VertragId.of(id)).orElseThrow());
    }
}
