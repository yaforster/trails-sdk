package io.github.yaforster.trails;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class SdkArchitectureTest {
    @Test
    void shouldKeepCoreAndApplicationIndependentOfSdkAndTransport() {
        noClasses().that()
                   .resideInAnyPackage("..core..", "..app..")
                   .should()
                   .dependOnClassesThat()
                   .resideInAnyPackage("..sdk..", "..adapter..")
                   .check(new ClassFileImporter().importPackages("io.github.yaforster.trails.core",
                           "io.github.yaforster.trails.app"));
    }

    @Test
    void shouldKeepCoreIndependentOfApplicationPorts() {
        noClasses().that()
                   .resideInAPackage("..core..")
                   .should()
                   .dependOnClassesThat()
                   .resideInAPackage("..app..")
                   .check(new ClassFileImporter().importPackages("io.github.yaforster.trails.core"));
    }

    @Test
    void shouldKeepMigratedCoreAndApplicationFreeOfTechnicalAdapters() {
        noClasses().that()
                   .resideInAnyPackage("..core..", "..app..")
                   .should()
                   .dependOnClassesThat()
                   .resideInAnyPackage("org.springframework..", "org.openqa.selenium..", "jakarta.persistence..",
                           "org.hibernate..", "org.springframework.mail..")
                   .check(new ClassFileImporter().importPackages("io.github.yaforster.trails.core",
                           "io.github.yaforster.trails.app"));
    }

    @Test
    void shouldNotRetainTheRemovedPluginOrRuntimeArchitecture() {
        noClasses().should()
                   .dependOnClassesThat()
                   .resideInAnyPackage("..extension..", "..runtime..", "..loader..", "..descriptor..")
                   .check(new ClassFileImporter().importPackages("io.github.yaforster.trails"));
    }

    @Test
    void shouldKeepSdkHostFreeOfAdapterTechnologies() {
        noClasses().that()
                   .resideInAPackage("..sdk..")
                   .should()
                   .dependOnClassesThat()
                   .resideInAnyPackage("org.openqa.selenium..", "jakarta.persistence..", "org.hibernate..",
                           "com.mysql..", "org.springframework.mail..", "org.thymeleaf..", "com.openhtmltopdf..",
                           "org.apache.pdfbox..")
                   .check(new ClassFileImporter().importPackages("io.github.yaforster.trails.sdk"));
    }

}
