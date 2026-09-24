package com.kodelabs.formflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enforces the layering and module-boundary rules from the backend CLAUDE.md as
 * compiler-adjacent checks instead of relying only on code review discipline.
 */
class ArchitectureTest {

    /**
     * Every top-level module under {@code modules.*}. A module added to the codebase without
     * being added here silently drops out of {@link #eachModuleMayNotReachIntoAnotherModulesOutputPorts()}
     * — that's exactly the gap that let the {@code reports} module (added after this test was
     * first written) go unchecked for a while. If this list and {@code modules/*} in the source
     * tree drift apart, {@link #moduleListStaysInSyncWithTheSourceTree()} fails the build.
     */
    private static final List<String> MODULES = List.of("auth", "forms", "notifications", "reports");

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.kodelabs.formflow");
    }

    @Test
    void moduleListStaysInSyncWithTheSourceTree() {
        List<String> actualModules = classes.stream()
                .map(c -> c.getPackageName())
                .filter(pkg -> pkg.contains(".modules."))
                .map(pkg -> pkg.substring(pkg.indexOf(".modules.") + ".modules.".length()))
                .map(rest -> rest.contains(".") ? rest.substring(0, rest.indexOf('.')) : rest)
                .distinct()
                .sorted()
                .toList();

        List<String> expected = MODULES.stream().sorted().toList();
        assertThat(actualModules).isEqualTo(expected);
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

    /**
     * One dynamic test per module, generated from {@link #MODULES} instead of hand-duplicated —
     * a module's application/domain layer must never depend on another module's domain.port.out
     * directly (that was #97's exact violation: a forms use case injecting auth's
     * TenantRepositoryPort). The producer module's *infrastructure* implementing a port the
     * *consumer* module owns (TenantInfoPort/TenantInfoAdapter) is the sanctioned way to cross
     * this boundary, and is deliberately untouched — infrastructure isn't in the "that()" scope.
     */
    @TestFactory
    Stream<DynamicTest> eachModuleMayNotReachIntoAnotherModulesOutputPorts() {
        return MODULES.stream().map(module -> DynamicTest.dynamicTest(module, () -> {
            List<String> otherModulesPortOut = MODULES.stream()
                    .filter(other -> !other.equals(module))
                    .map(other -> "..modules." + other + ".domain.port.out..")
                    .toList();

            ArchRule rule = noClasses()
                    .that().resideInAnyPackage(
                            "..modules." + module + ".application..", "..modules." + module + ".domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            otherModulesPortOut.toArray(new String[0]))
                    .because("cross-module reads/writes must go through a port owned by the "
                            + "consuming module (see TenantInfoPort/TenantInfoAdapter), never by "
                            + "reaching into another module's internal repository port (see #97).");

            rule.check(classes);
        }));
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
