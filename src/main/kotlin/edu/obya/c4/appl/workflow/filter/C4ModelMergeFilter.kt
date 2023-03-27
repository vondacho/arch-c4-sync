package edu.obya.c4.appl.workflow.filter

import edu.obya.c4.appl.workflow.Context
import edu.obya.c4.appl.workflow.Filter
import edu.obya.c4.appl.workflow.atFail
import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.structurizr.StructurizrModel

class C4ModelMergeFilter(
    override val name: String,
    private val mergeContext: C4Model.MergeContext
) : Filter {

    override fun execute(context: Context) {
        if (context.containsKey("model.master") && context.containsKey("model.other")) {
            val masterModel: StructurizrModel = context.atFail("model.master")
            val otherModel: StructurizrModel = context.atFail("model.other")
            context["model.master"] = masterModel.merge(otherModel, mergeContext)
            context.remove("model.other")
        }
    }
}
