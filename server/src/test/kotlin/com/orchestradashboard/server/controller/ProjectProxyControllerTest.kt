package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.service.OrchestratorProxyService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(ProjectProxyController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class ProjectProxyControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var proxyService: OrchestratorProxyService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `GET api-v1-projects returns 200 with project list`() {
        whenever(proxyService.getProjects()).thenReturn("""[{"name":"my-project"}]""")

        mockMvc.get("/api/v1/projects")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""[{"name":"my-project"}]""") }
            }
    }

    @Test
    fun `GET api-v1-projects-name returns 200 with project detail`() {
        whenever(proxyService.getProject("my-project")).thenReturn("""{"name":"my-project","desc":"test"}""")

        mockMvc.get("/api/v1/projects/my-project")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""{"name":"my-project","desc":"test"}""") }
            }
    }

    @Test
    fun `GET api-v1-projects-name-issues returns 200 with issues`() {
        whenever(proxyService.getProjectIssues("my-project", 0, 20))
            .thenReturn("""[{"number":1,"title":"Bug"}]""")

        mockMvc.get("/api/v1/projects/my-project/issues")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""[{"number":1,"title":"Bug"}]""") }
            }
    }

    @Test
    fun `GET api-v1-projects-name-issues with custom pagination`() {
        whenever(proxyService.getProjectIssues("my-project", 2, 10))
            .thenReturn("""[{"number":5}]""")

        mockMvc.get("/api/v1/projects/my-project/issues?page=2&pageSize=10")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""[{"number":5}]""") }
            }
    }
}
