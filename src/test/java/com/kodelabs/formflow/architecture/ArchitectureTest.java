package com.kodelabs.formflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the layering and module-boundary rules from the backend CLAUDE.md as
 * compiler-adjacent checks instead of relying only on code review discipline.
 */
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.kodelabs.formflow");
    }

    @Test
    void onlyPersistenceInfrastructureMayTouchJpaEntitiesOrRepositories() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..infrastructure.persistence..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.persistence.entity..",
                        "..infrastructure.persistence.repository..")
                .because("JPA entities and Spring Data repositories are persistence-only "
                        + "details; every other layer must go through domain ports (see #97).");

        rule.check(classes);
    }

    @Test
    void modulesMayNotReachIntoAnotherModulesOutputPorts() {
        ArchRule formsRule = noClasses()
                .that().resideInAnyPackage(
                        "..modules.forms.application..", "..modules.forms.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..modules.auth.domain.port.out..",
                        "..modules.notifications.domain.port.out..")
                .because("cross-module reads/writes must go through a port owned by the "
                        + "consuming module (see TenantInfoPort/TenantInfoAdapter), never by "
                        + "reaching into another module's internal repository port (see #97).");

        ArchRule authRule = noClasses()
                .that().resideInAnyPackage(
                        "..modules.auth.application..", "..modules.auth.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..modules.forms.domain.port.out..",
                        "..modules.notifications.domain.port.out..")
                .because("cross-module reads/writes must go through a port owned by the "
                        + "consuming module, never by reaching into another module's internal "
                        + "repository port.");

        ArchRule notificationsRule = noClasses()
                .that().resideInAnyPackage(
                        "..modules.notifications.application..", "..modules.notifications.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..modules.auth.domain.port.out..",
                        "..modules.forms.domain.port.out..")
                .because("cross-module reads/writes must go through a port owned by the "
                        + "consuming module, never by reaching into another module's internal "
                        + "repository port.");

        formsRule.check(classes);
        authRule.check(classes);
        notificationsRule.check(classes);
    }

    @Test
    void domainModelsMustNotDependOnPersistenceFrameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain.model..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
                .because("domain POJOs must stay framework-free so modules can be extracted "
                        + "into microservices later without rewriting the domain.");

        rule.check(classes);
    }
}
