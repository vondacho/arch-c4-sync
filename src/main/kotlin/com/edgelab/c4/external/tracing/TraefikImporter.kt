package com.edgelab.c4.external.tracing

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.enricher.C4ModelEnricher
import com.structurizr.Workspace

object TraefikImporter: C4ModelEnricher<Long, Workspace> {

    override fun enrich(model: C4Model<Long, Workspace>) {
        TODO("Not yet implemented")
    }
}
