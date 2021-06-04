import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "service.rest"
version = "0.0.1"
application {
    mainClass.set("service.rest.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation ("io.ktor:ktor-serialization:$ktor_version")
    implementation ("com.beust:klaxon:5.5")
    implementation ("org.jsoup:jsoup:1.13.1")
    implementation ("org.json:json:20210307")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    implementation ("io.github.microutils:kotlin-logging:1.12.5")
}

//tasks.withType<Jar> {
//    manifest {
//        attributes["Main-Class"] = "service.rest.ApplicationKt"
//    }
//    configurations["compileClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
//}



tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "service.rest.ApplicationKt"
    }
}