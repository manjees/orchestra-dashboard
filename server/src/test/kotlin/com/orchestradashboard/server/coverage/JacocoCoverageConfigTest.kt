package com.orchestradashboard.server.coverage

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Verifies JaCoCo XML report generation and exclusion rules.
 *
 * These tests validate that:
 * 1. JaCoCo XML report is generated after running tests
 * 2. Generated/boilerplate classes are excluded from coverage
 *
 * Run with: ./gradlew :server:test :server:jacocoTestReport
 * Then: ./gradlew :server:test --tests "*.coverage.JacocoCoverageConfigTest" -Djacoco.report.verify=true
 */
class JacocoCoverageConfigTest {
    private val reportPath = "build/reports/jacoco/test/jacocoTestReport.xml"

    @Test
    fun `should generate valid JaCoCo XML report`() {
        val reportFile = File(reportPath)
        assumeTrue(reportFile.exists(), "JaCoCo report not found at $reportPath — run ./gradlew :server:jacocoTestReport first")

        val factory = DocumentBuilderFactory.newInstance()
        // Disable external entity resolution for security
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(reportFile)

        assertTrue(
            document.documentElement.tagName == "report",
            "JaCoCo XML report should have <report> root element",
        )

        val counters = document.getElementsByTagName("counter")
        assertTrue(
            counters.length > 0,
            "JaCoCo XML report should contain <counter> elements",
        )
    }

    @Test
    fun `should exclude generated code from coverage calculation`() {
        val reportFile = File(reportPath)
        assumeTrue(reportFile.exists(), "JaCoCo report not found at $reportPath — run ./gradlew :server:jacocoTestReport first")

        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(reportFile)

        val classes = document.getElementsByTagName("class")
        for (i in 0 until classes.length) {
            val className = classes.item(i).attributes.getNamedItem("name")?.nodeValue ?: continue

            // Entity classes in model package should be excluded
            if (className.contains("/model/") && className.endsWith("Entity")) {
                fail("Entity class '$className' should be excluded from coverage report")
            }

            // Application class should be excluded
            if (className.contains("Application")) {
                fail("Application class '$className' should be excluded from coverage report")
            }

            // Config classes should be excluded
            if (className.contains("/config/")) {
                fail("Config class '$className' should be excluded from coverage report")
            }
        }
    }
}
