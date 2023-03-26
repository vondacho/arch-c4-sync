package com.edgelab.c4.domain

import com.structurizr.model.Tags

object C4Tags {
    val reserved = setOf(
        Tags.ELEMENT, Tags.CONTAINER, Tags.COMPONENT, Tags.PERSON, Tags.SOFTWARE_SYSTEM, Tags.RELATIONSHIP)

    object Infrastructure {
        const val INFRA = "infrastructure"
        const val BROKER = "broker"
        const val QUEUE = "queue"
        const val DATABASE = "database"
        const val KEYSPACE = "keyspace"
        const val SCHEMA = "schema"
        const val CACHE = "cache"
        const val REGISTRY = "registry"
        const val API_GATEWAY = "api-gateway"
        const val REVERSE_PROXY = "reverse-proxy"
        const val METRICS = "metrics"
        const val CALL_TRACES = "tracing"
        const val LOG_TRACES = "logging"

        val common = listOf(REGISTRY, API_GATEWAY, REVERSE_PROXY, METRICS, LOG_TRACES, CALL_TRACES)
        val storage = listOf(DATABASE, BROKER, CACHE)
    }

    object Architecture {
        const val MICRO_SERVICE = "microservice"
        const val EVENT = "event"
        const val COMMAND = "command"
    }

    object Organization {
        const val OWNERSHIP = "ownership"
        const val DEVELOPMENT_TEAM = "development-team"
    }

    fun Set<String>.purge() = this - reserved
}
