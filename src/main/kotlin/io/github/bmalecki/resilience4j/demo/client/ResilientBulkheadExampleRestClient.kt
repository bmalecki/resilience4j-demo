package io.github.bmalecki.resilience4j.demo.client

import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

private const val SERVICE_NAME = "example-service"

interface ResilientBulkheadExampleRestClient {
    fun call(): CompletableFuture<String>
}

open class ResilientBulkheadExampleRestClientImpl(
    private val exampleRestClient: ExampleRestClient
) : ResilientBulkheadExampleRestClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "fallbackCB")
    @Retry(name = SERVICE_NAME, fallbackMethod = "fallbackRetry")
    @Bulkhead(name = SERVICE_NAME, type = Bulkhead.Type.THREADPOOL)
    @TimeLimiter(name = SERVICE_NAME)
    override fun call(): CompletableFuture<String> {
        val res = exampleRestClient.call()
        return CompletableFuture.completedFuture(res)
    }

    private fun fallbackRetry(ex: Throwable): CompletableFuture<String> {
        logger.debug("Failed due to Retry policy", ex)
        return CompletableFuture.completedFuture("RETRY")
    }

    private fun fallbackCB(ex: CallNotPermittedException): CompletableFuture<String> {
        logger.debug("Failed to Circuit Breaker policy", ex)
        return CompletableFuture.completedFuture("CB")
    }
}