import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.loom)
    id("maven-publish")
}

val modName = project.property("mod_name").toString()
val modId = modName.lowercase()
val modVersion = project.property("mod_version").toString()
val mavenGroup = project.property("maven_group").toString()

base.archivesName.set(modId)
version = modVersion
group = mavenGroup

repositories {
    mavenCentral()
    mavenLocal()
}

val includeImplementation: Configuration by configurations.creating {
    configurations.implementation.configure { extendsFrom(this@creating) }
}

fun DependencyHandlerScope.modImplementationAndInclude(dep: Any) {
    modImplementation(dep)
    include(dep)
}

dependencies {
    // To change the versions, see at `libs.versions.toml` file
    // Fabric
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    // Fabric API + Kotlin
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    // Fabric Permissions
    modImplementationAndInclude(libs.fabric.permissions)

    // ktoml
    includeImplementation(libs.ktoml.core)
    includeImplementation(libs.ktoml.file)

    // JDA
    includeImplementation(libs.jda) {
        exclude(group = "net.dv8tion", module = "opus-java")
        exclude(group = "net.dv8tion", module = "tink")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        // ensure that the encoding is set to UTF-8, no matter what the system default is
        // this fixes some edge cases with special characters not displaying correctly
        // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
        // If Javadoc is generated, this must be specified in that task too.
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        inputs.property("id", modId)
        inputs.property("name", modName)
        inputs.property("version", modVersion)

        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "id" to modId,
                "name" to name,
                "version" to version,

                "fabric_loader_version" to libs.versions.fabric.loader.get(),
                "fabric_kotlin_version" to libs.versions.fabric.kotlin.get(),
                "minecraft_version" to libs.versions.minecraft.get()
            ))
        }
    }

    jar {
        from("LICENSE")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // select the repositories you want to publish to
    repositories {}
}
