package io.github.bmalecki.resilience4j.demo.client

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory

private const val SERVICE_NAME = "example-service"

interface ResilientExampleRestClient {
    fun call(): String
}

open class ResilientExampleRestClientImpl (
    private val exampleRestClient: ExampleRestClient
) : ResilientExampleRestClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "fallbackCB")
    @Retry(name = SERVICE_NAME, fallbackMethod = "fallbackRetry")
    override fun call(): String {
        return exampleRestClient.call()
    }

    private fun fallbackRetry(ex: Throwable): String {
        logger.debug("Failed due to Retry policy", ex)
        return "RETRY"
    }

    private fun fallbackCB(ex: CallNotPermittedException): String {
        logger.debug("Failed to Circuit Breaker policy", ex)
        return "CB"
    }
}