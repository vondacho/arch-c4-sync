package com.edgelab.c4.appl.usecase

import com.edgelab.c4.appl.config.ViewNamingProvider
import com.edgelab.c4.appl.workflow.Workflow
import com.edgelab.c4.appl.workflow.enricher.*
import com.edgelab.c4.appl.workflow.filter.C4ModelExportFilter
import com.edgelab.c4.appl.workflow.filter.C4ModelImportFilter
import com.edgelab.c4.appl.workflow.filter.C4ViewGenerationFilter
import com.edgelab.c4.external.structurizr.CloudId
import com.edgelab.c4.external.structurizr.StructurizrCloudWriter
import com.edgelab.c4.external.structurizr.StructurizrFileReader
import java.nio.file.Path

object LandscapeViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate landscape view",
                    C4LandscapeViewGenerator(viewNamer = C4ViewNamer(ViewNamingProvider.read()))
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}

object SystemContextViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate system context views",
                    C4SystemContextViewGenerator(viewNamer = C4ViewNamer(ViewNamingProvider.read()))
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}

object SystemServicesViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate system services views",
                    C4MicroservicesContainerViewGenerator(viewNamer = C4ViewNamer(ViewNamingProvider.read()))
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}

object SystemOwnershipViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate system ownerships views",
                    C4OwnershipContainerViewGenerator(viewNamer = C4ViewNamer(ViewNamingProvider.read()))
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}

object ServiceContextViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate service context views",
                    C4ServiceContextContainerViewGenerator(viewNamer = C4ViewNamer(ViewNamingProvider.read()))
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}

object ServiceInternalViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate service component views",
                    C4ServiceComponentViewGenerator(viewNamer = C4ViewNamer(ViewNamingProvider.read()))
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}

object ServiceDeploymentViewGenUseCase {

    fun run(source: Path, destination: CloudId) {
        Workflow(
            listOf(
                C4ModelImportFilter("load", source, mapOf(), StructurizrFileReader()),
                C4ViewGenerationFilter(
                    "generate service deployment views",
                    C4ServiceDeploymentViewGenerator(
                        viewNamer = C4ViewNamer(ViewNamingProvider.read()),
                        targetEnvironment = "production"
                    )
                ),
                C4ModelExportFilter("upload", destination, StructurizrCloudWriter())
            )
        ).run()
    }
}
