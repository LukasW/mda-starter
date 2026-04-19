package ch.grudligstrasse.mda.clm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String ROOT = "ch.grudligstrasse.mda.clm";

    private static JavaClasses classes() {
        return new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
    }

    @Test
    void domain_mustNotDependOnFrameworks() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta..", "io.quarkus..", "io.smallrye..",
                        "org.hibernate..", "com.fasterxml..",
                        ROOT + ".(*)..adapter..",
                        ROOT + ".(*)..application..")
                .check(classes());
    }

    @Test
    void applicationPorts_mustNotDependOnFrameworks() {
        noClasses().that().resideInAPackage("..application.port..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta..", "io.quarkus..", "io.smallrye..",
                        ROOT + ".(*)..adapter..")
                .check(classes());
    }

    @Test
    void adapterIn_mustNotDependOn_adapterOut() {
        noClasses().that().resideInAPackage("..adapter.in..")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter.out..")
                .check(classes());
    }

    @Test
    void jaxrs_onlyIn_adapterIn_rest_or_shared_problem_or_adapterOut_rest() {
        // Bestehende Whitelist (..adapter.in.rest.., ..shared.problem..) bleibt;
        // ..adapter.out.rest.. wird zusaetzlich erlaubt fuer ausgehende REST-Clients
        // (z. B. externe Personenverwaltung). Erweiterung, keine Aufweichung.
        noClasses().that()
                .resideOutsideOfPackages("..adapter.in.rest..", "..adapter.out.rest..", "..shared.problem..")
                .should().dependOnClassesThat()
                .resideInAPackage("jakarta.ws.rs..")
                .check(classes());
    }

    @Test
    void jpa_onlyIn_adapterOut_persistence_or_shared_process() {
        noClasses().that()
                .resideOutsideOfPackages("..adapter.out.persistence..", "..shared.process..")
                .should().dependOnClassesThat()
                .resideInAPackage("jakarta.persistence..")
                .check(classes());
    }

    @Test
    void contract_mustNotDependDirectlyOnPerson() {
        noClasses().that().resideInAPackage(ROOT + ".contract..")
                .should().dependOnClassesThat()
                .resideInAPackage(ROOT + ".person..")
                .check(classes());
    }
}
