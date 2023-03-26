package com.edgelab.c4.appl.usecase

import com.edgelab.c4.appl.workflow.Workflow
import com.edgelab.c4.appl.workflow.filter.C4ModelExportFilter
import com.edgelab.c4.appl.workflow.filter.C4ModelImportFilter
import com.edgelab.c4.appl.workflow.writer.StructurizrPrinter
import com.edgelab.c4.external.structurizr.CloudId
import com.edgelab.c4.external.structurizr.StructurizrCloudReader

object DownloadUseCase {

    fun run(source: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrCloudReader()),
                C4ModelExportFilter("print", System.out, StructurizrPrinter())
            )
        ).run()
    }
}
