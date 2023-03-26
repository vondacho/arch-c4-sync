package com.edgelab.c4.appl.workflow.writer

import com.edgelab.c4.appl.config.TemplateConfigurationFactory
import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Writer
import com.edgelab.c4.domain.structurizr.util.allMicroservices
import com.edgelab.c4.domain.structurizr.util.allSystems
import com.edgelab.c4.util.logger
import com.edgelab.c4.util.normalize
import com.structurizr.Workspace
import com.structurizr.model.Container
import com.structurizr.model.SoftwareSystem
import freemarker.template.Template
import java.io.Writer

class C4DocumentWriter : C4Writer<Long, Workspace, Writer> {
    private val logger = this.javaClass.logger()

    override fun write(model: C4Model<Long, Workspace>, destination: Writer) {

        val cfg = TemplateConfigurationFactory.create()

        model.allSystems().onEach { system ->
            val root = mapOf("model" to DataModel(system, system.allMicroservices()))
            val template: Template? = runCatching { cfg.getTemplate("${system.name.normalize()}.md") }
                .onFailure { logger.warn("Template could not be read from ${system.name.normalize()}.md, cause is $it") }
                .onSuccess { logger.info("Template has been successfully loaded from ${system.name.normalize()}.md.") }
                .getOrNull()

            template?.process(root, destination)
        }
    }

    data class DataModel(val system: SoftwareSystem, val microservices: List<Container>)
}
