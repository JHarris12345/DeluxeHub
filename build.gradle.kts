plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
}


group = "fun.lewisdev"
version = "3.6.6"
description = "DeluxeHub"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation("com.github.cryptomorin:XSeries:13.0.0")
    implementation("javax.inject:javax.inject:1")
    implementation("javax.annotation:javax.annotation-api:1.2")
    implementation("com.github.BGMP.CommandFramework:command-framework-bukkit:master")
    implementation("de.tr7zw:item-nbt-api:2.15.3") // UPDATE THIS FOR EACH NEW MC VERSION
    implementation("org.bstats:bstats-bukkit-lite:1.7")
    implementation("com.github.shynixn.headdatabase:hdb-api:1.0")
    implementation("com.github.ItzSave:ZithiumLibrary:1f5182b77f")

    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.1")
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        inputs.properties(mapOf("version" to version))
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    shadowJar {
        minimize()
        archiveClassifier.set("") // Removes "-all" suffix

        relocate("org.bstats", "fun.lewisdev.deluxehub.libs.metrics")
        relocate("cl.bgmp", "fun.lewisdev.deluxehub.libs.command")
        relocate("de.tr7zw.changeme.nbtapi", "fun.lewisdev.deluxehub.libs.nbt")
        relocate("net.zithium.library", "fun.lewisdev.deluxehub.libs.library")

        // Additional relocation (if needed)
        relocate("com.cryptomorin.xseries", "fun.lewisdev.deluxehub.libs.xseries")
    }
}

