package com.edgelab.c4.external.structurizr

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4ModelId
import com.edgelab.c4.domain.C4Reader
import com.structurizr.Workspace

class StructurizrCloudReader : C4Reader<CloudId, Long, Workspace> {

    override fun read(source: CloudId): C4Model<Long, Workspace>? =
        StructurizrRepositoryAdapter(source.credential).findOne(C4ModelId(source.id))
}
