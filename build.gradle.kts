plugins {
    `java`
}

allprojects {
    group = "com.trend"
    version = "0.1"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
