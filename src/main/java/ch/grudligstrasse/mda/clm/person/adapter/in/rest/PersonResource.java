package ch.grudligstrasse.mda.clm.person.adapter.in.rest;

import ch.grudligstrasse.mda.clm.person.application.port.in.PersonAendernUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonErfassenUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonLoeschenUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonSuchenQuery;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;
import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
    private final PersonAendernUseCase aendernUc;
    private final PersonLoeschenUseCase loeschenUc;

    public PersonResource(PersonErfassenUseCase erfassenUc, PersonSuchenQuery suche,
                          PersonAendernUseCase aendernUc, PersonLoeschenUseCase loeschenUc) {
        this.erfassenUc = erfassenUc;
        this.suche = suche;
        this.aendernUc = aendernUc;
        this.loeschenUc = loeschenUc;
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

    // mda-generator: manual-edits-below

    @PUT
    @Path("/{id}")
    public Response aendern(@PathParam("id") String id, @Valid PersonAendernRequest req) {
        aendernUc.execute(new PersonAendernUseCase.PersonAendernCommand(
                PersonId.parse(id), req.vorname(), req.nachname(), req.email(),
                req.organisation(), req.funktion(), req.expectedVersion()));
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response loeschen(@PathParam("id") String id,
                             @QueryParam("expectedVersion") @DefaultValue("0") long expectedVersion) {
        loeschenUc.execute(new PersonLoeschenUseCase.PersonLoeschenCommand(
                PersonId.parse(id), expectedVersion));
        return Response.noContent().build();
    }

    public record PersonAendernRequest(
            @NotBlank String vorname,
            @NotBlank String nachname,
            @NotBlank @Email String email,
            String organisation,
            String funktion,
            long expectedVersion) {}
}
