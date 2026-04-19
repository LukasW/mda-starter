package ch.grudligstrasse.mda.starter.obligation.adapter.in.rest;

import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristErfassenUseCase;
import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristLadenQuery;
import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristSichtenUseCase;
import ch.grudligstrasse.mda.starter.obligation.domain.FristArt;
import ch.grudligstrasse.mda.starter.obligation.domain.FristId;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/fristen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FristResource {

    @Inject FristErfassenUseCase erfassen;
    @Inject FristSichtenUseCase sichten;
    @Inject FristLadenQuery laden;

    public record ErfassenRequest(
            @NotNull UUID vertragId,
            @NotNull FristArt art,
            @NotNull LocalDate faelligkeitsDatum,
            @PositiveOrZero int vorlaufTage,
            @NotNull UUID verantwortlicherId
    ) {}

    public record SichtenRequest(@NotNull UUID sichtenderUserId) {}

    @POST
    public Response erfassen(@Valid ErfassenRequest req) {
        FristId id = erfassen.erfassen(new FristErfassenUseCase.Command(
                req.vertragId(), req.art(), req.faelligkeitsDatum(),
                req.vorlaufTage(), req.verantwortlicherId()));
        FristDto body = FristDto.from(laden.laden(id).orElseThrow());
        return Response.created(URI.create("/api/v1/fristen/" + id.value())).entity(body).build();
    }

    @POST
    @Path("/{id}/sichten")
    public FristDto sichten(@PathParam("id") UUID id, @Valid SichtenRequest req) {
        FristId fid = FristId.of(id);
        sichten.sichten(new FristSichtenUseCase.Command(fid, req.sichtenderUserId()));
        return FristDto.from(laden.laden(fid).orElseThrow());
    }

    @GET
    @Path("/{id}")
    public FristDto einzeln(@PathParam("id") UUID id) {
        return laden.laden(FristId.of(id))
                .map(FristDto::from)
                .orElseThrow(() -> new DomainException("MDA-OBL-NOT_FOUND", "Frist nicht gefunden: " + id));
    }

    @GET
    public List<FristDto> alle(@QueryParam("vertragId") UUID vertragId) {
        if (vertragId != null) {
            return laden.fuerVertrag(vertragId).stream().map(FristDto::from).toList();
        }
        return laden.alle().stream().map(FristDto::from).toList();
    }
}
