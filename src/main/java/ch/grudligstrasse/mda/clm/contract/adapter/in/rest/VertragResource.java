package ch.grudligstrasse.mda.clm.contract.adapter.in.rest;

import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragAbrufenQuery;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragDokumentHochladenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragErstellenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragMetadatenSetzenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragPersonZuordnenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragTriggerUseCase;
import ch.grudligstrasse.mda.clm.contract.domain.DokumentReferenz;
import ch.grudligstrasse.mda.clm.contract.domain.ParteiRolle;
import ch.grudligstrasse.mda.clm.contract.domain.SpeicherTyp;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsTyp;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;
import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/vertraege")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VertragResource {

    private final VertragErstellenUseCase erstellenUc;
    private final VertragMetadatenSetzenUseCase metadatenUc;
    private final VertragDokumentHochladenUseCase dokumentUc;
    private final VertragPersonZuordnenUseCase personZuordnenUc;
    private final VertragTriggerUseCase triggerUc;
    private final VertragAbrufenQuery abfrage;

    public VertragResource(VertragErstellenUseCase erstellenUc,
                           VertragMetadatenSetzenUseCase metadatenUc,
                           VertragDokumentHochladenUseCase dokumentUc,
                           VertragPersonZuordnenUseCase personZuordnenUc,
                           VertragTriggerUseCase triggerUc,
                           VertragAbrufenQuery abfrage) {
        this.erstellenUc = erstellenUc;
        this.metadatenUc = metadatenUc;
        this.dokumentUc = dokumentUc;
        this.personZuordnenUc = personZuordnenUc;
        this.triggerUc = triggerUc;
        this.abfrage = abfrage;
    }

    @POST
    public Response erstellen(@Valid VertragErstellenRequest request) {
        VertragId id = erstellenUc.execute(new VertragErstellenUseCase.VertragErstellenCommand(
                request.titel(), VertragsTyp.parse(request.typ()),
                request.erstellerId(), request.tenantId()));
        return Response.created(URI.create("/api/v1/vertraege/" + id.asString()))
                .entity(new VertragErstellenResponse(id.asString()))
                .build();
    }

    @GET
    public List<VertragDto> listen(@QueryParam("$top") @DefaultValue("50") int top,
                                   @QueryParam("$skip") @DefaultValue("0") int skip,
                                   @QueryParam("tenantId") UUID tenantId) {
        return abfrage.byTenant(tenantId, top, skip).stream()
                .map(VertragDto::of)
                .toList();
    }

    @GET
    @Path("/{id}")
    public VertragDto abrufen(@PathParam("id") String id) {
        return abfrage.byId(VertragId.parse(id))
                .map(VertragDto::of)
                .orElseThrow(() -> DomainException.notFound("MDA-CON-404",
                        "Vertrag nicht gefunden: " + id));
    }

    @PUT
    @Path("/{id}/metadaten")
    public void metadaten(@PathParam("id") String id, @Valid VertragMetadatenRequest req) {
        metadatenUc.execute(new VertragMetadatenSetzenUseCase.VertragMetadatenSetzenCommand(
                VertragId.parse(id), req.titel(), req.gueltigVon(), req.gueltigBis()));
    }

    @POST
    @Path("/{id}/dokument")
    public Response dokumentHochladen(@PathParam("id") String id, @Valid DokumentHochladenRequest req) {
        DokumentReferenz ref = new DokumentReferenz(
                SpeicherTyp.valueOf(req.speicherTyp()),
                req.pfadLokal(),
                req.archivExternId(),
                req.mimeType(),
                req.groesseByte(),
                req.inhaltHash());
        dokumentUc.execute(new VertragDokumentHochladenUseCase.VertragDokumentHochladenCommand(
                VertragId.parse(id), req.erstelltVon(), ref));
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/parteien")
    public Response personZuordnen(@PathParam("id") String id, @Valid ParteiRequest req) {
        personZuordnenUc.execute(new VertragPersonZuordnenUseCase.VertragPersonZuordnenCommand(
                VertragId.parse(id), req.personId(), ParteiRolle.valueOf(req.rolle())));
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/process/contract/trigger/{trigger}")
    @Consumes(MediaType.WILDCARD)
    public TriggerResponse trigger(@PathParam("id") String id,
                                   @PathParam("trigger") String triggerName,
                                   @QueryParam("actor") @DefaultValue("system") String actor) {
        VertragStage neu = triggerUc.execute(new VertragTriggerUseCase.VertragTriggerCommand(
                VertragId.parse(id), triggerName, actor));
        return new TriggerResponse(neu.name());
    }

    public record VertragErstellenRequest(
            @NotBlank @Size(max = 200) String titel,
            @NotBlank String typ,
            @NotNull UUID erstellerId,
            UUID tenantId) {}

    public record VertragErstellenResponse(String id) {}

    public record VertragMetadatenRequest(
            @NotBlank @Size(max = 200) String titel,
            LocalDate gueltigVon,
            LocalDate gueltigBis) {}

    public record DokumentHochladenRequest(
            @NotNull UUID erstelltVon,
            @NotBlank String speicherTyp,
            String pfadLokal,
            String archivExternId,
            @NotBlank String mimeType,
            long groesseByte,
            String inhaltHash) {}

    public record ParteiRequest(
            @NotNull UUID personId,
            @NotBlank String rolle) {}

    public record TriggerResponse(String stage) {}
}
