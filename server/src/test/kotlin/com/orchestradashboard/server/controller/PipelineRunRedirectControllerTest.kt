package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.SecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(PipelineRunRedirectController::class)
@Import(SecurityConfig::class)
class PipelineRunRedirectControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `GET api-v1-pipelines redirects 308 to pipeline-runs`() {
        mockMvc.get("/api/v1/pipelines")
            .andExpect {
                status { isPermanentRedirect() }
                header { string("Location", "/api/v1/pipeline-runs") }
            }
    }

    @Test
    fun `GET api-v1-pipelines-id redirects 308 to pipeline-runs-id`() {
        mockMvc.get("/api/v1/pipelines/pipe-1")
            .andExpect {
                status { isPermanentRedirect() }
                header { string("Location", "/api/v1/pipeline-runs/pipe-1") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with query string preserves it`() {
        mockMvc.get("/api/v1/pipelines?agentId=agent-1&status=RUNNING")
            .andExpect {
                status { isPermanentRedirect() }
                header { string("Location", "/api/v1/pipeline-runs?agentId=agent-1&status=RUNNING") }
            }
    }

    @Test
    fun `POST api-v1-pipelines redirects 308 to pipeline-runs`() {
        mockMvc.post("/api/v1/pipelines")
            .andExpect {
                status { isPermanentRedirect() }
                header { string("Location", "/api/v1/pipeline-runs") }
            }
    }
}
