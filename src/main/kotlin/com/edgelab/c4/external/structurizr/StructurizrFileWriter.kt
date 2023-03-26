package com.edgelab.c4.external.structurizr

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Writer
import com.edgelab.c4.util.logger
import com.structurizr.Workspace
import com.structurizr.util.WorkspaceUtils
import java.nio.file.Path

class StructurizrFileWriter : C4Writer<Long, Workspace, Path> {

    private val logger = this.javaClass.logger()

    override fun write(model: C4Model<Long, Workspace>, destination: Path) {
        runCatching { WorkspaceUtils.saveWorkspaceToJson(model.state, destination.toFile()) }
            .onFailure { logger.warn("C4 model ${model.id.id} could not be written to ${destination}.") }
            .onSuccess { logger.info("C4 model ${model.id.id} has been successfully written to ${destination}.") }
            .let { model }
    }
}
