import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.6.0"
}

allprojects {

    group = "io.github.gnupinguin"
    version= "2.0"

    repositories {
        mavenLocal()
        mavenCentral()
    }
    ext {
        set("lombokVersion", "1.18.28")
        set("jsoupVersion", "1.16.1")
        set("commonLang3Version", "3.11")
        set("commonsIoVersion", "2.8.0")
    }
}

val jUnitJupiterVersion: String by rootProject.extra { "5.9.1" }
val slf4jVersion: String by rootProject.extra { "2.0.7" }


subprojects {
    apply {
        plugin("java")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")
        testCompileOnly("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")

        annotationProcessor("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")
        testAnnotationProcessor("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")

        compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")

        compileOnly("org.slf4j:slf4j-api:$slf4jVersion")
        testImplementation("org.slf4j:slf4j-api:$slf4jVersion")

        testImplementation("org.junit.jupiter:junit-jupiter:$jUnitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitJupiterVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")

    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
        include("**/*Test.*")
        testLogging.showStandardStreams = true
        testLogging.exceptionFormat = TestExceptionFormat.FULL
    }

}
