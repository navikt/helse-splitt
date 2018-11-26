val kafkaVersion = "2.0.1"

val streamsVersion = 10

val junitJupiterVersion = "5.3.1"
val spekVersion = "1.2.1"
val kluentVersion = "1.41"

val mainClass = "no.nav.helse.AppKt"

plugins {
   application
   kotlin("jvm") version "1.3.10"
   id("com.github.johnrengelman.shadow") version "4.0.3"
}

buildscript {
   dependencies {
      classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
   }
}

application {
   mainClassName = "$mainClass"
}

dependencies {
   compile(kotlin("stdlib"))
   compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0")

   compile("no.nav.helse:streams:$streamsVersion")

   testCompile("org.apache.kafka:kafka-streams-test-utils:$kafkaVersion")
   testCompile("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testCompile("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
   testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
   testCompile("org.amshove.kluent:kluent:$kluentVersion")
   testCompile("org.jetbrains.spek:spek-api:$spekVersion") {
      exclude(group = "org.jetbrains.kotlin")
   }
   testRuntime("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion") {
      exclude(group = "org.junit.platform")
      exclude(group = "org.jetbrains.kotlin")
   }
}

repositories {
   jcenter()
   mavenCentral()
   maven("http://packages.confluent.io/maven/")
   maven("https://dl.bintray.com/kotlin/ktor")
}

java {
   sourceCompatibility = JavaVersion.VERSION_11
   targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test> {
   useJUnitPlatform()
   testLogging {
      events("passed", "skipped", "failed")
   }
}

tasks.withType<Wrapper> {
   gradleVersion = "5.0"
}


