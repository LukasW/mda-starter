package ch.grudligstrasse.mda.clm.bdd;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;

@CucumberOptions(
        features = "classpath:features/service",
        glue = "ch.grudligstrasse.mda.clm.bdd.service",
        tags = "@service and not @wip",
        plugin = "pretty")
public class ServiceBddIT extends CucumberQuarkusTest {
}
