package io.github.bmalecki.resilience4j.demo

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@WireMockTest(httpPort = 8070)
class DemoApplicationTests {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @BeforeEach
    fun setup() {
        circuitBreakerRegistry.allCircuitBreakers.forEach { it.reset() }
    }

    @Test
    fun `test1 - external service works`() {
        // given
        serviceWorks()

        // when
        val response = restTemplate.getForEntity("/test1", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("ok", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(1, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test1 - external service does not work`() {
        // given
        serviceDoesNotWork()

        // when
        val response = restTemplate.getForEntity("/test1", String::class.java)

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        verify(1, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test2 - external service works`() {
        // given
        serviceWorks()

        // when
        val response = restTemplate.getForEntity("/test2", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("ok", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(1, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test2 - external service does not work and retry 4 times`() {
        // given
        serviceDoesNotWork()

        // when
        val response = restTemplate.getForEntity("/test2", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("RETRY", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(4, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test2 - external service does not work and circuit breaker triggered`() {
        // given
        serviceDoesNotWork()

        // when
        restTemplate.getForEntity("/test2", ExampleResponse::class.java)
        val response = restTemplate.getForEntity("/test2", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("CB", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(5, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test2 - external service starts working`() {
        // given
        serviceDoesNotWork()

        // when
        restTemplate.getForEntity("/test2", ExampleResponse::class.java)
        restTemplate.getForEntity("/test2", ExampleResponse::class.java)

        // and
        serviceWorks()
        Thread.sleep(1000)
        val response = restTemplate.getForEntity("/test2", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("ok", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(6, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test3 - external service works`() {
        // given
        serviceWorks()

        // when
        val response = restTemplate.getForEntity("/test3", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("ok", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(1, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test3 - external service does not work and retry 4 times`() {
        // given
        serviceDoesNotWork()

        // when
        val response = restTemplate.getForEntity("/test3", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("RETRY", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)

        Thread.sleep(300)
        verify(4, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test3 - external service does not work and circuit breaker triggered`() {
        // given
        serviceDoesNotWork()

        // when
        restTemplate.getForEntity("/test3", ExampleResponse::class.java)
        val response = restTemplate.getForEntity("/test3", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("CB", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(5, getRequestedFor(urlEqualTo("/example")))
    }

    @Test
    fun `test3 - external service works slowly`() {
        // given
        serviceWorksSlowly()

        // when
        val response = restTemplate.getForEntity("/test3", ExampleResponse::class.java)

        // then
        val body = response.body!!
        assertEquals("RETRY", body.test)
        assertEquals(HttpStatus.OK, response.statusCode)

        Thread.sleep(300)
        verify(4, getRequestedFor(urlEqualTo("/example")))
    }

    fun serviceWorks() {
        stubFor(
            get("/example")
                .willReturn(
                    aResponse()
                        .withBody("ok")
                )
        )
    }

    fun serviceWorksSlowly() {
        stubFor(
            get("/example")
                .willReturn(
                    aResponse()
                        .withBody("ok")
                        .withFixedDelay(300)
                )
        )
    }

    fun serviceDoesNotWork() {
        stubFor(
            get("/example")
                .willReturn(
                    aResponse()
                        .withStatus(500)
                )
        )
    }

}
