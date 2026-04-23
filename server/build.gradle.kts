plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.websocket)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.serialization.json)
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // Database
    runtimeOnly(libs.h2)
    runtimeOnly(libs.postgresql)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit5)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// JaCoCo coverage configuration
tasks.named<JacocoReport>("jacocoTestReport") {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/model/*Entity*",
                        "**/model/*Response*",
                        "**/model/*Request*",
                        "**/model/EventType*",
                        "**/model/*Mapper*",
                        "**/model/notification/DeviceTokenRecord*",
                        "**/model/notification/NotificationDispatchResult*",
                        "**/model/notification/PipelineNotificationPayload*",
                        "**/config/*",
                        "**/service/PipelineEventConsumerService*",
                        "**/service/notification/FcmSenderImpl*",
                        "**/service/notification/ApnsSenderImpl*",
                        "**/*Application*",
                    )
                }
            },
        ),
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(provider { tasks.named<JacocoReport>("jacocoTestReport").get().classDirectories })
}

tasks.named("check") {
    dependsOn("jacocoTestCoverageVerification")
}
