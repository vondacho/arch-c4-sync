package com.edgelab.c4.external.structurizr

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4ModelId
import com.edgelab.c4.domain.C4Repository
import com.edgelab.c4.domain.structurizr.StructurizrModel
import com.edgelab.c4.util.logger
import com.structurizr.Workspace
import com.structurizr.api.StructurizrClient
import java.io.File

class StructurizrRepositoryAdapter(account: Credential) : C4Repository<Long, Workspace> {

    private val logger = this.javaClass.logger()

    private val client = StructurizrClient(account.apiKey, account.apiSecret)

    init {
        client.workspaceArchiveLocation = File("backup")
    }

    override fun findOne(id: C4ModelId<Long>): C4Model<Long, Workspace>? =
        runCatching { StructurizrModel(id, client.getWorkspace(id.id)) }
            .onFailure { logger.warn("C4 model $id could not be pulled from Structurizr cloud, cause is $it") }
            .onSuccess { logger.info("C4 model $id has been successfully pulled from Structurizr cloud.") }
            .getOrNull()

    override fun save(model: C4Model<Long, Workspace>): C4Model<Long, Workspace>? =
        runCatching { client.putWorkspace(model.id.id, model.state) }
            .onFailure { logger.warn("C4 model ${model.id.id} could not be pushed to Structurizr cloud, cause is $it") }
            .onSuccess { logger.info("C4 model ${model.id.id} has been successfully pushed to Structurizr cloud.") }
            .let { model }
}
