package edu.obya.c4.appl.workflow.filter

import edu.obya.c4.appl.workflow.Context
import edu.obya.c4.appl.workflow.Filter
import edu.obya.c4.domain.C4Reader
import edu.obya.c4.domain.strategy.PropertyWeights
import edu.obya.c4.domain.structurizr.StructurizrModel
import com.structurizr.Workspace

class C4ModelImportFilter<T>(
    override val name: String,
    private val source: T,
    private val sourceWeights: PropertyWeights,
    private val reader: C4Reader<T, Long, Workspace>
) : Filter {

    override fun execute(context: Context) {
        reader.read(source)?.let {
            val model = StructurizrModel(it.id, it.state, sourceWeights)

            if (context.containsKey("model.master")) {
                context["model.other"] = model
            } else {
                context["model.master"] = model
                context.remove("model.other")
            }
        }
    }
}
