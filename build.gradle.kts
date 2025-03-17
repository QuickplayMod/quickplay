import org.apache.commons.lang3.SystemUtils
import java.net.URL

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.gradleup.shadow") version "8.3.6"
    id("org.jsonschema2pojo") version "1.2.2"
}

//Constants:
val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")

// Downloading and generating Quickplay JSON schema
val generatedFolder = "src/main/resources/assets/${project.property("modid")}/generated"
val downloadSchema = tasks.register("downloadSchema") {
    doLast {
        file(generatedFolder).mkdirs()
        val schema = URL("http://0.0.0.0:4343/api/v1/gamesSchema").readText()
        File(generatedFolder, "schema.json").writeText(schema)
    }
}

tasks.generateJsonSchema2Pojo {
    dependsOn(downloadSchema)
}

jsonSchema2Pojo {
    setSource(project.files(project.file("${generatedFolder}/schema.json")))
    targetDirectory = layout.buildDirectory.dir("generated/sources/schemas").get().asFile
    targetPackage = "dev.ecr.quickplay"
    includeAdditionalProperties = true
    removeOldOutput = true
    includeSetters = false
}

// Generate constants class containing mod ID and mod version
val generateConstants = tasks.register("generateConstants") {
    val outputDir = layout.buildDirectory.dir("generated/sources/constants").get().asFile

    doLast {
        val constantsFile = File(outputDir, "dev/ecr/quickplay/QuickplayConstants.java")
        constantsFile.parentFile.mkdirs()
        constantsFile.writeText(
            """
            package dev.ecr.quickplay;

            public class QuickplayConstants {
                public static final String MOD_ID = "${project.property("modid")}";
                public static final String MOD_VERSION = "${project.property("version")}";
                public static final String MC_VERSION = "${project.property("mcVersion")}";
                public static final String MC_PLATFORM = "${project.property("loom.platform")}";
            }
            """.trimIndent()
        )
    }
}

tasks.compileJava {
    dependsOn(tasks.generateJsonSchema2Pojo)
    dependsOn(generateConstants)
}

sourceSets.main {
    java.srcDirs("build/generated/sources/constants")
    java.srcDirs("build/generated/sources/schemas")
}

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration:
loom {
    log4jConfigs.from(file("log4j2.xml"))
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                // This argument causes a crash on macOS
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies:

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    // If you don't want to log in with your real minecraft account, remove this line
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    maven(url = "https://repo.essential.gg/repository/maven-public")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}
val modShadowImpl: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

val elementaVersion = 676
val ucVersion = 373
dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    // If you don't want to log in with your real minecraft account, remove this line
    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")

    shadowImpl("gg.essential:elementa:$elementaVersion")
    modShadowImpl("gg.essential:universalcraft-1.8.9-forge:$ucVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching(listOf("mcmod.info")) {
        expand(inputs.properties)
    }

    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}


val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))

    configurations = listOf(shadowImpl, modShadowImpl)

    doLast {
        configurations.forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }

    // If you want to include other dependencies and shadow them, you can relocate them in here
    fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
    relocate("gg.essential.elementa")
    relocate("gg.essential.universal")

    from("LICENSE")
}

tasks.assemble.get().dependsOn(tasks.remapJar)
tasks.build {
    dependsOn(tasks.shadowJar)
}
