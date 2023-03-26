package com.edgelab.c4.external.atlassian

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Writer
import com.structurizr.Workspace

object ConfluenceExporter : C4Writer<Long, Workspace, Any> {

    override fun write(model: C4Model<Long, Workspace>, destination: Any) {
        TODO("Not yet implemented")
    }
}
