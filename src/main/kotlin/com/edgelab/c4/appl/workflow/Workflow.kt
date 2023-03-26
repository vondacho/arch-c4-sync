package com.edgelab.c4.appl.workflow

import com.edgelab.c4.util.logger

class Workflow(private val filters: List<Filter>) {
    private val logger = this.javaClass.logger()

    fun run() = run(mutableMapOf())

    fun run(context: Context): Context {
        filters.onEach {
            logger.info("filter ${it.name} - executing")
            it.execute(context)
            logger.info("filter ${it.name} - end")
        }
        return context
    }
}

interface Filter {
    val name: String
    fun execute(context: Context)
}

typealias Context = MutableMap<String, Any>

fun <T> Context.at(key: String): T? = this[key]?.let { it as T }
fun <T> Context.atFail(key: String): T = at(key) ?: throw IllegalArgumentException("key $key is absent from context")
fun <T> Context.atDefault(key: String, default: T): T = at(key) ?: default
