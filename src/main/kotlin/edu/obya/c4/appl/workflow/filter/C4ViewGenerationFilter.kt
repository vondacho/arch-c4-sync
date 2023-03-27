package edu.obya.c4.appl.workflow.filter

import edu.obya.c4.appl.workflow.Context
import edu.obya.c4.appl.workflow.Filter
import edu.obya.c4.appl.workflow.atFail
import edu.obya.c4.domain.enricher.C4ViewGenerator
import com.structurizr.Workspace

class C4ViewGenerationFilter(
    override val name: String,
    private val generator: C4ViewGenerator<Long, Workspace>
) : Filter {

    override fun execute(context: Context) {
        generator.generate(context.atFail("model.master"))
    }
}

