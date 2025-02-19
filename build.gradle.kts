import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

	plugins {
	id("org.springframework.boot") version "2.2.0.M6"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.50"
	kotlin("plugin.spring") version "1.3.50"
}

group = "com.example.bukbot"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.telegram:telegrambots-spring-boot-starter:4.1.2")
	implementation("org.springframework.boot:spring-boot-devtools:2.1.8.RELEASE")
	implementation("org.mongodb:mongodb-driver-async:3.10.1")
	implementation("ch.rasc:sse-eventbus:1.1.7")
	implementation("joda-time:joda-time:2.10.1")
	implementation("com.google.guava:guava:25.+")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
