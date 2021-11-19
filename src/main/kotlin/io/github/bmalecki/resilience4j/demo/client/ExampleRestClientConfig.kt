package io.github.bmalecki.resilience4j.demo.client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExampleRestClientConfig {

    @Bean
    fun exampleRestClient(): ExampleRestClient {
        return ExampleRestClient()
    }

    @Bean
    fun resilientExampleRestClient(exampleRestClient: ExampleRestClient): ResilientExampleRestClient {
        return ResilientExampleRestClientImpl(exampleRestClient = exampleRestClient)
    }

    @Bean
    fun resilientBulkheadExampleRestClient(exampleRestClient: ExampleRestClient): ResilientBulkheadExampleRestClient {
        return ResilientBulkheadExampleRestClientImpl(exampleRestClient = exampleRestClient)
    }
}

