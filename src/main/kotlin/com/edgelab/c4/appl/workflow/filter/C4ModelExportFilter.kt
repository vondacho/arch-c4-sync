package com.edgelab.c4.appl.workflow.filter

import com.edgelab.c4.appl.workflow.Context
import com.edgelab.c4.appl.workflow.Filter
import com.edgelab.c4.appl.workflow.atFail
import com.edgelab.c4.domain.C4Writer
import com.structurizr.Workspace

class C4ModelExportFilter<T>(
    override val name: String,
    private val destination: T,
    private val writer: C4Writer<Long, Workspace, T>
) : Filter {

    override fun execute(context: Context) {
        writer.write(context.atFail("model.master"), destination)
    }
}

