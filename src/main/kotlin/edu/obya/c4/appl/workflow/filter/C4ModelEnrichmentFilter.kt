package edu.obya.c4.appl.workflow.filter

import edu.obya.c4.appl.workflow.Context
import edu.obya.c4.appl.workflow.Filter
import edu.obya.c4.appl.workflow.atFail
import edu.obya.c4.domain.enricher.C4ModelEnricher
import com.structurizr.Workspace

class C4ModelEnrichmentFilter(
    override val name: String,
    private val enricher: C4ModelEnricher<Long, Workspace>
) : Filter {

    override fun execute(context: Context) {
        enricher.enrich(context.atFail("model.master"))
    }
}

