package ch.grudligstrasse.mda.clm.person.adapter.out.rest;

import ch.grudligstrasse.mda.clm.person.application.port.out.ExternePersonenverwaltungClient;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST-basierter Adapter fuer die externe Personenverwaltung.
 * Wird nur bei Build-Property {@code clm.person.externe-verwaltung.enabled=true}
 * aktiviert; ueberschreibt damit den {@code DisabledExternePersonenverwaltungClient}.
 */
@ApplicationScoped
@IfBuildProperty(name = "clm.person.externe-verwaltung.enabled", stringValue = "true")
public class RestExternePersonenverwaltungClient implements ExternePersonenverwaltungClient {

    private static final Logger LOG = Logger.getLogger(RestExternePersonenverwaltungClient.class);

    private final ExternePersonenverwaltungRestApi api;

    public RestExternePersonenverwaltungClient(@RestClient ExternePersonenverwaltungRestApi api) {
        this.api = api;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<ExternePerson> suchen(String query, int limit) {
        try {
            List<ExternePersonPayload> raw = api.suchen(query, limit);
            return raw.stream()
                    .map(p -> new ExternePerson(p.externeId(), p.vorname(), p.nachname(),
                            p.email(), p.organisation(), p.funktion()))
                    .toList();
        } catch (RuntimeException e) {
            LOG.warnf("Externe Personenverwaltung nicht erreichbar: %s", e.getMessage());
            return List.of();
        }
    }
}
