package ch.grudligstrasse.mda.starter.approval.adapter.in.rest;

import ch.grudligstrasse.mda.starter.approval.application.port.in.FreigabeAnfordernUseCase;
import ch.grudligstrasse.mda.starter.approval.application.port.in.FreigabeEntscheidenUseCase;
import ch.grudligstrasse.mda.starter.approval.application.port.in.FreigabeLadenQuery;
import ch.grudligstrasse.mda.starter.approval.domain.Entscheidung;
import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/freigaben")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FreigabeResource {

    @Inject FreigabeAnfordernUseCase anfordern;
    @Inject FreigabeEntscheidenUseCase entscheiden;
    @Inject FreigabeLadenQuery laden;

    public record AnfordernRequest(@NotNull UUID vertragId, @NotNull UUID versionId, @NotNull UUID reviewerId) {}
    public record EntscheidenRequest(@NotNull Entscheidung entscheidung, String begruendung) {}

    @POST
    public Response anfordern(@Valid AnfordernRequest req) {
        FreigabeId id = anfordern.anfordern(new FreigabeAnfordernUseCase.Command(
                req.vertragId(), req.versionId(), req.reviewerId()));
        FreigabeDto body = FreigabeDto.from(laden.laden(id).orElseThrow());
        return Response.created(URI.create("/api/v1/freigaben/" + id.value())).entity(body).build();
    }

    @POST
    @Path("/{id}/entscheiden")
    public FreigabeDto entscheiden(@PathParam("id") UUID id, @Valid EntscheidenRequest req) {
        FreigabeId fid = FreigabeId.of(id);
        entscheiden.entscheiden(new FreigabeEntscheidenUseCase.Command(fid, req.entscheidung(), req.begruendung()));
        return FreigabeDto.from(laden.laden(fid).orElseThrow());
    }

    @GET
    @Path("/{id}")
    public FreigabeDto einzeln(@PathParam("id") UUID id) {
        return laden.laden(FreigabeId.of(id))
                .map(FreigabeDto::from)
                .orElseThrow(() -> new DomainException("MDA-APV-NOT_FOUND", "Freigabe nicht gefunden: " + id));
    }

    @GET
    public List<FreigabeDto> listeFuerVertrag(@QueryParam("vertragId") UUID vertragId) {
        if (vertragId == null) {
            throw new DomainException("MDA-APV-003", "Query-Parameter vertragId ist erforderlich");
        }
        return laden.fuerVertrag(vertragId).stream().map(FreigabeDto::from).toList();
    }
}
