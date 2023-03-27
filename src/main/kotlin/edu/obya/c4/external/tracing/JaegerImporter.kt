package edu.obya.c4.external.tracing

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.enricher.C4ModelEnricher
import com.structurizr.Workspace

object JaegerImporter: C4ModelEnricher<Long, Workspace> {

    override fun enrich(model: C4Model<Long, Workspace>) {
        TODO("Not yet implemented")
    }
}
