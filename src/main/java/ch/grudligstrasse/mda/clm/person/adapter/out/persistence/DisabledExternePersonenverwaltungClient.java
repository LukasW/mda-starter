package ch.grudligstrasse.mda.clm.person.adapter.out.persistence;

import ch.grudligstrasse.mda.clm.person.application.port.out.ExternePersonenverwaltungClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

/**
 * Default-Adapter: externe Personenverwaltung ist deaktiviert (Standalone-Modus).
 * Kann per Config {@code clm.person.externe-verwaltung.enabled=true} aktiviert werden;
 * die konkrete Anbieter-Implementierung kommt in einem spaeteren Feature.
 */
@ApplicationScoped
@DefaultBean
public class DisabledExternePersonenverwaltungClient implements ExternePersonenverwaltungClient {

    @ConfigProperty(name = "clm.person.externe-verwaltung.enabled", defaultValue = "false")
    boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<ExternePerson> suchen(String query, int limit) {
        return List.of();
    }
}
