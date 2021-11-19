package io.github.bmalecki.resilience4j.demo.client

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

class ExampleRestClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    val restTemplate: RestTemplate = RestTemplate()

    fun call(): String {
        logger.info("Thread: ${Thread.currentThread().name}")
        val url = "http://localhost:8070/example";
        val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)

        return response.body ?: throw Exception()
    }
}