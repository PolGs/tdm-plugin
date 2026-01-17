plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
}

val pluginVersion: String = project.findProperty("version")?.toString() ?: "1.0.0"

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("TDMPlugin")
    archiveVersion.set(pluginVersion)

    from("src/main/resources")
}
