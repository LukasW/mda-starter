package ch.grudligstrasse.mda.starter.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "ch.grudligstrasse.mda.starter",
        importOptions = { ImportOption.DoNotIncludeTests.class }
)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_enthaelt_keine_framework_imports = noClasses()
            .that().resideInAPackage("..contract.domain..")
            .or().resideInAPackage("..approval.domain..")
            .or().resideInAPackage("..obligation.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..",
                    "jakarta.ws.rs..",
                    "io.quarkus..",
                    "jakarta.inject..",
                    "jakarta.enterprise..")
            .because("Domain-Schicht muss framework-frei bleiben (Hexagonal / DDD).");

    @ArchTest
    static final ArchRule application_enthaelt_keine_persistenz_framework = noClasses()
            .that().resideInAPackage("..contract.application..")
            .or().resideInAPackage("..approval.application..")
            .or().resideInAPackage("..obligation.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..",
                    "io.quarkus.hibernate..")
            .because("Application-Schicht kennt keine Persistenz-Details, nur Ports.");

    @ArchTest
    static final ArchRule rest_adapter_nutzt_nur_port_in = noClasses()
            .that().resideInAPackage("..adapter.in.rest..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application.service..",
                    "..adapter.out..")
            .because("REST-Adapter ruft ausschliesslich Eingangs-Ports (port.in).");

    @ArchTest
    static final ArchRule persistence_adapter_greift_nicht_in_domain_service = noClasses()
            .that().resideInAPackage("..adapter.out.persistence..")
            .should().dependOnClassesThat().resideInAPackage("..application.service..")
            .because("Persistenz-Adapter darf keine Anwendungsservices aufrufen (nur Port-Out implementieren).");

    @ArchTest
    static final ArchRule bc_kopplung_nur_via_shared_oder_id = noClasses()
            .that().resideInAPackage("..approval.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..contract.application..",
                    "..contract.adapter..",
                    "..obligation.application..",
                    "..obligation.adapter..")
            .because("Bounded-Context-uebergreifende Kopplung nur via IDs oder Events.");
}
