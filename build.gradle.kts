plugins {
    `java-library`
    `maven-publish`
    idea
    id("net.neoforged.moddev")
    id("io.freefair.lombok")
    id("neoforge-mutex")
}

val modVersion = property("mod_version") as String
// Version scheme follows upstream convention: 1.0.0+<build|pr>.<N> (Minecraft target is conveyed by the jar name).
// The mc_index property makes N unique per Minecraft version when a CI run builds all of them at once.
val ciRun = System.getenv("CI_BUILD") != "false"
val buildType = if (ciRun && System.getenv("PR_BUILD") != "false") "pr" else "build"
val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
val mcIndex = providers.gradleProperty("mc_index").orNull?.toIntOrNull()
val buildNumber = when {
    !ciRun -> null
    mcIndex != null && runNumber != null -> (runNumber * 100 + mcIndex).toString()
    else -> System.getenv("GITHUB_RUN_NUMBER")
}
version = modVersion + (buildNumber?.let { "+$buildType.$it" } ?: "")
group = property("mod_group_id") as String

base {
    archivesName = "${property("mod_name")}-neoforge-${sc.current.version}"
}

// Minecraft 26.x requires Java 25, older versions run on Java 21
val requiredJava = when {
    sc.current.parsed >= "26" -> 25
    else -> 21
}

java.toolchain.languageVersion = JavaLanguageVersion.of(requiredJava)

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.ryanhcode.dev/releases") { name = "RyanHCode Maven Releases" }
    maven("https://maven.ryanhcode.dev/snapshots") { name = "RyanHCode Maven Snapshots" }
    exclusiveContent {
        forRepository { maven("https://cursemaven.com") { name = "CurseForge Maven" } }
        filter { includeGroup("curse.maven") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth Maven" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC Maven" }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel Maven" }
    maven("https://maven.ithundxr.dev/snapshots") { name = "Ithundxr Maven" }
    maven("https://modmaven.dev") { name = "ModMaven" }
    maven("https://maven.blamejared.com/") { name = "Jared's maven" }
    maven("https://maven.saps.dev/minecraft") {
        name = "Saps Maven"
        content { includeGroup("dev.latvian.mods"); includeGroup("dev.latvian.apps") }
    }
    maven("https://maven.k-4u.nl") { name = "k-4u Maven" }
    maven("https://maven.octo-studios.com/releases") { name = "OctoStudios Maven" }
    maven("https://maven.su5ed.dev/releases") { name = "Su5eD Maven" }
    maven("https://server.cjsah.net:1002/maven/") { name = "Cjsah Maven" }
    exclusiveContent {
        forRepository { maven("https://girafi.dk/maven/") { name = "Girafi Maven" } }
        filter { includeGroup("com.teammetallurgy.aquaculture") }
    }
}

// Extra per-version mixin config registrations:
//  - 1.21.1: Sable physics compat + legacy compat mixins (Spectrum/Create)
//  - 1.21.8: PlayerAccessor is still present
val extraMixins: String =
    when (sc.current.version) {
        "1.21.1" -> """
            [[mixins]]
            config = "${property("mod_id")}.mixins.compat.sable.json"

            [[mixins]]
            config = "${property("mod_id")}.mixins.compat121.json"
        """.trimIndent()
        "1.21.8" -> """
            [[mixins]]
            config = "${property("mod_id")}.mixins.compat128.json"
        """.trimIndent()
        else -> ""
    }

val replaceProperties = mapOf(
    "minecraft_version" to property("minecraft_version"),
    "minecraft_version_range" to property("minecraft_version_range"),
    "neo_version" to property("neo_version"),
    "neo_version_range" to property("neo_version_range"),
    "loader_version_range" to property("loader_version_range"),
    "mod_id" to property("mod_id"),
    "mod_name" to property("mod_name"),
    "mod_license" to property("mod_license"),
    "mod_version" to version,
    "mod_authors" to property("mod_authors"),
    "mod_description" to property("mod_description"),
    "extra_mixins" to extraMixins,
)

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    // Always read the shared template from the root project; Stonecutter relocates
    // per-node generated trees, so a relative path would resolve to nothing.
    from(rootProject.file("src/main/templates"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets {
    main {
        resources.srcDir(generateModMetadata)
        resources.srcDir("src/generated/resources")
    }
}

neoForge {
    version = property("neo_version") as String

    if (hasProperty("parchment_mappings_version")) parchment {
        mappingsVersion = property("parchment_mappings_version") as String
        minecraftVersion = property("parchment_minecraft_version") as String
    }

    runs {
        register("client") {
            client()
            gameDirectory = file("../../run/")
            systemProperty("neoforge.enabledGameTestNamespaces", property("mod_id") as String)
        }

        register("server") {
            server()
            gameDirectory = file("../../run/")
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", property("mod_id") as String)
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", property("mod_id") as String)
        }

        register("data") {
            if (sc.current.parsed >= "26") clientData() else data()

            programArguments.addAll(
                "--mod", property("mod_id") as String,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        register(property("mod_id") as String) {
            sourceSet(sourceSets.main.get())
        }
    }

    ideSyncTask(generateModMetadata)
}

dependencies {
    // Rolling Gate
    implementation("dev.anvilcraft.rg:RollingGate:${property("rolling_gate_version")}")

    // Mod Compat - Aquaculture 2 (coordinates differ per Minecraft version)
    when {
        sc.current.version == "1.21.1" ->
            implementation("com.teammetallurgy.aquaculture:aquaculture2_1.21.1:1.21.1-${property("aquaculture_version")}")
        sc.current.parsed < "26" ->
            compileOnly("com.teammetallurgy.aquaculture:aquaculture2_1.21.5:1.21.5-${property("aquaculture_version")}")
        else ->
            compileOnly("maven.modrinth:aquaculture:2.9.2-neoforge,26.1.2")
    }

    if (hasProperty("spectrum_version")) {
        implementation("maven.modrinth:spectrum:${property("spectrum_version")}")
        implementation("maven.modrinth:revelationary:${property("revelationary_version")}")
        implementation("maven.modrinth:curios:${property("curios_version")}")
        implementation("maven.modrinth:modonomicon:${property("modonomicon_version")}")
    }

    if (hasProperty("sable_version")) {
        compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-${property("minecraft_version")}:${property("sable_companion_version")}")
        implementation("foundry.veil:veil-neoforge-${property("minecraft_version")}:${property("veil_version")}") {
            exclude(group = "maven.modrinth")
        }
        implementation("dev.ryanhcode.sable:sable-neoforge-${property("minecraft_version")}:${property("sable_version")}") {
            exclude(group = "foundry.veil")
            exclude(group = "com.tterrag.registrate")
            exclude(group = "com.simibubi.create")
            exclude(group = "net.createmod.ponder")
            exclude(group = "dev.engine-room.flywheel")
        }
    }
}

tasks {
    // Link ModDevGradle's Minecraft artifacts to Stonecutter's generated sources
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }
}

val delombokTask = tasks.named("delombok")

tasks.register<Jar>("sourcesJar") {
    archiveClassifier = "sources"
    dependsOn(delombokTask)
    from(project.files(delombokTask.map { it.outputs.files }))
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the mod jar and copies it to the root build/libs directory"
    dependsOn(tasks.named("build"))
    from(tasks.named("jar").map { (it as Jar).archiveFile }, tasks.named("sourcesJar").map { (it as Jar).archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs/${project.property("mod_version")}"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = property("mod_group_id") as String
            artifactId = property("mod_name") as String
            version = project.version as String
            from(components["java"])
            artifact(tasks.named("sourcesJar"))
        }
    }
    repositories {
        val mavenUrl = System.getenv("MAVEN_URL")
        if (mavenUrl != null) {
            maven {
                url = uri(mavenUrl)
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

lombok {
    // Lombok 1.18.44 is required for Java 25 support and works on Java 21 as well
    version.set("1.18.44")
}
