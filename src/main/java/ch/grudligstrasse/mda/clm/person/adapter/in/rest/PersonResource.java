package ch.grudligstrasse.mda.clm.person.adapter.in.rest;

import ch.grudligstrasse.mda.clm.person.application.port.in.PersonErfassenUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonSuchenQuery;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;
import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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

@Path("/api/v1/personen")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    private final PersonErfassenUseCase erfassenUc;
    private final PersonSuchenQuery suche;

    public PersonResource(PersonErfassenUseCase erfassenUc, PersonSuchenQuery suche) {
        this.erfassenUc = erfassenUc;
        this.suche = suche;
    }

    @POST
    public Response erfassen(@Valid PersonErfassenRequest req) {
        PersonId id = erfassenUc.execute(new PersonErfassenUseCase.PersonErfassenCommand(
                req.vorname(), req.nachname(), req.email(),
                req.organisation(), req.funktion(), req.tenantId()));
        return Response.created(URI.create("/api/v1/personen/" + id.asString()))
                .entity(new PersonErfassenResponse(id.asString()))
                .build();
    }

    @GET
    public List<PersonDto> suchen(@QueryParam("query") @DefaultValue("") String query,
                                  @QueryParam("limit") @DefaultValue("20") int limit) {
        return suche.suchen(query, limit).stream()
                .map(PersonDto::of)
                .toList();
    }

    @GET
    @Path("/{id}")
    public PersonDto byId(@PathParam("id") String id) {
        return suche.byId(PersonId.parse(id))
                .map(PersonDto::of)
                .orElseThrow(() -> DomainException.notFound("MDA-PER-404",
                        "Person nicht gefunden: " + id));
    }

    public record PersonErfassenRequest(
            @NotBlank String vorname,
            @NotBlank String nachname,
            @NotBlank @Email String email,
            String organisation,
            String funktion,
            UUID tenantId) {}

    public record PersonErfassenResponse(String id) {}
}
