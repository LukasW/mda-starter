package ch.grudligstrasse.mda.clm.bdd;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;

@CucumberOptions(
        features = "classpath:features/ui",
        glue = "ch.grudligstrasse.mda.clm.bdd.ui",
        tags = "@ui and not @wip",
        plugin = "pretty")
public class UiBddIT extends CucumberQuarkusTest {
}
