package ch.grudligstrasse.mda.clm.bdd;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;

@CucumberOptions(
        features = "classpath:features/process",
        glue = "ch.grudligstrasse.mda.clm.bdd.process",
        tags = "@process and not @wip",
        plugin = "pretty")
public class ProcessBddIT extends CucumberQuarkusTest {
}
