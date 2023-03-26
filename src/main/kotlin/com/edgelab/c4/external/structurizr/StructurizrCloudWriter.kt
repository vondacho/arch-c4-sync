package com.edgelab.c4.external.structurizr

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4ModelId
import com.edgelab.c4.domain.C4Writer
import com.edgelab.c4.domain.structurizr.StructurizrModel
import com.structurizr.Workspace

class StructurizrCloudWriter : C4Writer<Long, Workspace, CloudId> {

    override fun write(model: C4Model<Long, Workspace>, destination: CloudId) {
        StructurizrRepositoryAdapter(destination.credential)
            .save(StructurizrModel(C4ModelId(destination.id), model.state))
    }
}
