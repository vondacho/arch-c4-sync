package com.edgelab.c4.appl.config

import com.edgelab.c4.domain.strategy.Naming
import com.edgelab.c4.domain.strategy.NodeMapping
import com.edgelab.c4.domain.strategy.PropertyWeights
import com.edgelab.c4.util.toResourceStream
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.InputStream
import java.io.InputStreamReader

object WeightsProvider {

    fun read(scope: String): PropertyWeights = "config/c4-$scope-weights.properties"
        .toResourceStream()
        .readLines({ it.read() }, emptyMap())

    private fun List<String>.read(): PropertyWeights? = this
        .filter { it.isNotEmpty() }
        .map { it.split(":") }
        .map { it[0] to it[1].trim().toInt() }
        .toMap()
}

object NamingProvider {

    fun read(): Naming = "config/c4-naming.properties".toResourceStream().readNaming()

    fun InputStream?.readNaming() = this.readLines({ it.read() }, emptyMap())

    private fun List<String>.read(): Naming? = this
        .filter { it.isNotEmpty() }
        .map { it.split(":") }
        .map { it[0] to it[1].split(",") }
        .flatMap { (canonical, candidates) -> candidates.map { it.trim() to canonical } }
        .toMap()
}

object NodeMappingProvider {

    fun read(): NodeMapping = "config/c4-node-mapping.properties"
        .toResourceStream()
        .readLines({ it.read() }, emptyMap())

    private fun List<String>.read(): NodeMapping? = this
        .filter { it.isNotEmpty() }
        .map { it.split(":") }
        .map { it[0] to it[1].trim() }
        .toMap()
}

typealias ViewNaming = Map<String, Template>

object ViewNamingProvider {

    private val config = TemplateConfigurationFactory.create()

    fun read(): ViewNaming = "config/c4-view-naming.properties"
        .toResourceStream()
        .readLines({ it.read(config) }, emptyMap())

    private fun List<String>.read(cfg: Configuration): ViewNaming? = this
        .filter { it.isNotEmpty() }
        .map { it.split(":") }
        .map { it[0] to Template(it[0], it[1].trim(), cfg) }
        .toMap()
}

object TemplateConfigurationFactory {
    fun create(): Configuration {
        val cfg = Configuration(Configuration.VERSION_2_3_30)
        cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "")
        cfg.defaultEncoding = "UTF-8"
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        cfg.logTemplateExceptions = false
        cfg.wrapUncheckedExceptions = true
        cfg.fallbackOnNullLoopVariable = false
        return cfg
    }
}

private fun <T> InputStream?.readLines(reader: (List<String>) -> T?, default: T): T =
    this?.let { stream ->
        runCatching { reader(InputStreamReader(stream).readLines()) }
            .onFailure { println("Could not load configuration file, use defaults: $it") }
            .getOrNull()
    } ?: default
