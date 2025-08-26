plugins {
    application
}

dependencies {
    implementation(project(":modules:core"))
    implementation(project(":modules:data-http"))
    implementation(project(":modules:data-sqlite"))
    implementation(project(":modules:render-xchart"))
    implementation(project(":modules:report-pdfbox"))
    implementation("info.picocli:picocli:4.7.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.13")
}

application {
    mainClass.set("com.trend.app.Cli")
}
