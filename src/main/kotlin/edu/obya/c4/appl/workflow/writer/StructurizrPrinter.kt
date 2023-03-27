package edu.obya.c4.appl.workflow.writer

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.C4Writer
import com.structurizr.Workspace
import com.structurizr.util.WorkspaceUtils

class StructurizrPrinter : C4Writer<Long, Workspace, Any> {

    fun write(model: C4Model<Long, Workspace>) {
        WorkspaceUtils.printWorkspaceAsJson(model.state)
    }

    override fun write(model: C4Model<Long, Workspace>, destination: Any) {
        write(model)
    }
}
