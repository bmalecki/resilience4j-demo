package io.github.bmalecki.resilience4j.demo

import io.github.bmalecki.resilience4j.demo.client.ExampleRestClient
import io.github.bmalecki.resilience4j.demo.client.ResilientBulkheadExampleRestClient
import io.github.bmalecki.resilience4j.demo.client.ResilientExampleRestClient
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Future


@RestController
@RequestMapping(produces = ["application/json"])
class ExampleController(
    private val exampleRestClient: ExampleRestClient,
    private val resilientExampleRestClient: ResilientExampleRestClient,
    private val resilientBulkheadExampleRestClient: ResilientBulkheadExampleRestClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/test1")
    fun test1(): ExampleResponse {
        logger.info("Thread: ${Thread.currentThread().name}")
        val res = exampleRestClient.call()
        return ExampleResponse(res)
    }

    @GetMapping("/test2")
    fun test2(): ExampleResponse {
        logger.info("Thread: ${Thread.currentThread().name}")
        val res = resilientExampleRestClient.call()
        return ExampleResponse(res)
    }

    @GetMapping("/test3")
    fun test3(): Future<ExampleResponse> {
        logger.info("Thread: ${Thread.currentThread().name}")
        val res = resilientBulkheadExampleRestClient.call()
        return res.thenApply { ExampleResponse(it) }
    }

}

