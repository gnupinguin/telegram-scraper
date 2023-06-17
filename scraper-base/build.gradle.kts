
plugins {
    id("java-library")
}

dependencies {
    api(project(":persistence-base"))
    implementation("org.jsoup:jsoup:${rootProject.extra["jsoupVersion"]}")

    implementation("org.apache.commons:commons-lang3:${rootProject.extra["commonLang3Version"]}")
    implementation("commons-io:commons-io:${rootProject.extra["commonsIoVersion"]}")

}
