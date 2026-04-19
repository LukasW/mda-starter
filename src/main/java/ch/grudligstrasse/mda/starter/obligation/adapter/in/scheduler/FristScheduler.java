package ch.grudligstrasse.mda.starter.obligation.adapter.in.scheduler;

import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristErinnernUseCase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDate;

@ApplicationScoped
public class FristScheduler {

    private static final Logger LOG = Logger.getLogger(FristScheduler.class);

    @Inject FristErinnernUseCase erinnern;

    @ConfigProperty(name = "clm.obligation.scheduler.enabled", defaultValue = "false")
    boolean enabled;

    @Scheduled(cron = "0 0 2 * * ?")
    void taeglicherErinnerungsLauf() {
        if (!enabled) {
            return;
        }
        int count = erinnern.erinnere(LocalDate.now());
        LOG.infof("FristScheduler: %d Erinnerungen ausgeloest", count);
    }
}
