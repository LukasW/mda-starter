package ch.grudligstrasse.mda.clm.person.adapter.out.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * MicroProfile-REST-Client fuer die optionale externe Personenverwaltung
 * gemaess Fachspec §7.2 (GET /persons?query=...&limit=...).
 *
 * Konfiguration via {@code clm.person.externe-verwaltung.base-url}.
 */
@RegisterRestClient(configKey = "externe-personenverwaltung")
@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
public interface ExternePersonenverwaltungRestApi {

    @GET
    List<ExternePersonPayload> suchen(@QueryParam("query") String query,
                                      @QueryParam("limit") int limit);
}
