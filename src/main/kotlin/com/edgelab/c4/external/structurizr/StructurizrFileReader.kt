package com.edgelab.c4.external.structurizr

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4ModelId
import com.edgelab.c4.domain.C4Reader
import com.edgelab.c4.domain.structurizr.StructurizrModel
import com.edgelab.c4.util.logger
import com.structurizr.Workspace
import com.structurizr.dsl.StructurizrDslParser
import com.structurizr.util.WorkspaceUtils
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

class StructurizrFileReader : C4Reader<Path, Long, Workspace> {

    private val logger = this.javaClass.logger()

    private val jsonMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json")
    private val dslMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.dsl")

    override fun read(source: Path): C4Model<Long, Workspace>? =
        when {
            jsonMatcher.matches(source) -> fromJson(source)
            dslMatcher.matches(source) -> fromDsl(source)
            else -> null.also {
                logger.warn("C4 model could not be read from ${source}, cause is unsupported format.")
            }
        }

    private fun fromJson(source: Path): C4Model<Long, Workspace>? =
        runCatching { WorkspaceUtils.loadWorkspaceFromJson(source.toFile()) }
            .onFailure { logger.warn("C4 model could not be read, cause is $it") }
            .onSuccess { logger.info("C4 model has been successfully loaded from $source.") }
            .getOrNull()
            ?.let { StructurizrModel(C4ModelId(it.id), it) }

    private fun fromDsl(source: Path): C4Model<Long, Workspace>? =
        runCatching {
            val parser = StructurizrDslParser()
            parser.parse(source.toFile())
            parser.workspace
        }
            .onFailure { logger.warn("C4 model could not be read from ${source}, cause is $it") }
            .onSuccess { logger.info("C4 model has been successfully loaded from $source.") }
            .getOrNull()
            ?.let { StructurizrModel(C4ModelId(it.id), it) }
}

fun Path.toModel(): C4Model<Long, Workspace>? = StructurizrFileReader().read(this)
