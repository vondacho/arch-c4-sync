package com.edgelab.c4.appl.usecase

import com.edgelab.c4.appl.workflow.Workflow
import com.edgelab.c4.appl.workflow.filter.C4ModelExportFilter
import com.edgelab.c4.appl.workflow.filter.C4ModelImportFilter
import com.edgelab.c4.appl.workflow.writer.StructurizrPrinter
import com.edgelab.c4.external.structurizr.CloudId
import com.edgelab.c4.external.structurizr.StructurizrCloudWriter
import com.edgelab.c4.external.structurizr.StructurizrFileReader
import java.nio.file.Path

object UploadUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ModelExportFilter("print", System.out, StructurizrPrinter()),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}