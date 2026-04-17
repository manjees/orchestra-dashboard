package com.orchestradashboard.server.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Suppress("LongMethod")
class OrchestratorProxyServiceTest {
    private lateinit var webClient: WebClient
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var requestBodySpec: WebClient.RequestBodySpec
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var service: OrchestratorProxyService

    @BeforeEach
    fun setUp() {
        webClient = mock()
        requestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        requestHeadersSpec = mock<WebClient.RequestHeadersSpec<*>>()
        requestBodyUriSpec = mock()
        requestBodySpec = mock()
        responseSpec = mock()
        service = OrchestratorProxyService(webClient)
    }

    private fun stubGet(responseBody: String) {
        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(any<String>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Any>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Any>(), any<Any>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Any>(), any<Any>(), any<Any>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<String>>()))
            .thenReturn(Mono.just(responseBody))
    }

    private fun stubPost(responseBody: String) {
        whenever(webClient.post()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri(any<String>())).thenReturn(requestBodySpec)
        whenever(requestBodyUriSpec.uri(any<String>(), any<Any>())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<String>>()))
            .thenReturn(Mono.just(responseBody))
    }

    @Test
    fun `getProjects returns json`() {
        stubGet("""[{"name":"proj1"}]""")
        assertEquals("""[{"name":"proj1"}]""", service.getProjects())
    }

    @Test
    fun `getProject returns json`() {
        stubGet("""{"name":"proj1"}""")
        assertEquals("""{"name":"proj1"}""", service.getProject("proj1"))
    }

    @Test
    fun `getProjectIssues returns json`() {
        stubGet("""[{"number":1}]""")
        assertEquals("""[{"number":1}]""", service.getProjectIssues("proj1", 0, 20))
    }

    @Test
    fun `getSystemStatus returns json`() {
        stubGet("""{"status":"ok"}""")
        assertEquals("""{"status":"ok"}""", service.getSystemStatus())
    }

    @Test
    fun `getPipelines returns json`() {
        stubGet("""[{"id":"p1"}]""")
        assertEquals("""[{"id":"p1"}]""", service.getPipelines())
    }

    @Test
    fun `getPipeline returns json`() {
        stubGet("""{"id":"p1"}""")
        assertEquals("""{"id":"p1"}""", service.getPipeline("p1"))
    }

    @Test
    fun `getPipelineHistory returns json`() {
        stubGet("""[{"id":"h1"}]""")
        assertEquals("""[{"id":"h1"}]""", service.getPipelineHistory())
    }

    @Test
    fun `getParallelPipelines returns json`() {
        stubGet("""{"children":[]}""")
        assertEquals("""{"children":[]}""", service.getParallelPipelines("p1"))
    }

    @Test
    fun `postSolve returns json`() {
        stubPost("""{"id":"cmd-1"}""")
        assertEquals("""{"id":"cmd-1"}""", service.postSolve("""{"issue":1}"""))
    }

    @Test
    fun `postInit returns json`() {
        stubPost("""{"ok":true}""")
        assertEquals("""{"ok":true}""", service.postInit("""{"project":"test"}"""))
    }

    @Test
    fun `postPlan returns json`() {
        stubPost("""{"ok":true}""")
        assertEquals("""{"ok":true}""", service.postPlan("""{"project":"test"}"""))
    }

    @Test
    fun `postDiscuss returns json`() {
        stubPost("""{"reply":"hello"}""")
        assertEquals("""{"reply":"hello"}""", service.postDiscuss("""{"msg":"hi"}"""))
    }

    @Test
    fun `postDesign returns json`() {
        stubPost("""{"design":"done"}""")
        assertEquals("""{"design":"done"}""", service.postDesign("""{"spec":"v1"}"""))
    }

    @Test
    fun `postShell returns json`() {
        stubPost("""{"output":"files"}""")
        assertEquals("""{"output":"files"}""", service.postShell("""{"cmd":"ls"}"""))
    }

    @Test
    fun `getCheckpoints returns json`() {
        stubGet("""[{"id":"cp-1"}]""")
        assertEquals("""[{"id":"cp-1"}]""", service.getCheckpoints())
    }

    @Test
    fun `retryCheckpoint returns json`() {
        stubPost("""{"id":"cp-1","status":"RETRYING"}""")
        assertEquals("""{"id":"cp-1","status":"RETRYING"}""", service.retryCheckpoint("cp-1"))
    }

    @Test
    fun `respondToApproval returns json`() {
        stubPost("""{"status":"approved"}""")
        assertEquals("""{"status":"approved"}""", service.respondToApproval("appr-1", """{"approved":true}"""))
    }
}
