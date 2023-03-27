package edu.obya.c4.appl.usecase

import edu.obya.c4.appl.workflow.Workflow
import edu.obya.c4.appl.workflow.filter.C4ModelExportFilter
import edu.obya.c4.appl.workflow.filter.C4ModelImportFilter
import edu.obya.c4.appl.workflow.writer.StructurizrPrinter
import edu.obya.c4.external.structurizr.CloudId
import edu.obya.c4.external.structurizr.StructurizrCloudWriter
import edu.obya.c4.external.structurizr.StructurizrFileReader
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
