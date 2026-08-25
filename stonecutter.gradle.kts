@file:OptIn(dev.kikugie.stonecutter.StonecutterExperimentalAPI::class)

plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.143" apply false
    id("io.freefair.lombok") version "9.2.0" apply false
}

stonecutter active "1.21.1"

stonecutter parameters {
    replacements {
        // Minecraft 26.x renamed ResourceLocation to Identifier
        string(current.parsed >= "26") {
            replace("ResourceLocation", "Identifier")
            replace(".location()", ".identifier()")
        }
    }
}
