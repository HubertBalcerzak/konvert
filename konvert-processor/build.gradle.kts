import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.5.10"

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("kapt") version "1.5.10"
}

group = "me.hubertus248"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":konvert-api"))
    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.squareup:kotlinpoet-metadata:1.8.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.8.0")
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
