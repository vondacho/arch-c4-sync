package edu.obya.c4.appl.usecase

import edu.obya.c4.appl.config.*
import edu.obya.c4.appl.config.NamingProvider.readNaming
import edu.obya.c4.appl.util.AppOption
import edu.obya.c4.appl.util.AppOptionId
import edu.obya.c4.appl.util.AppParameter
import edu.obya.c4.appl.util.at
import edu.obya.c4.appl.workflow.Filter
import edu.obya.c4.appl.workflow.Workflow
import edu.obya.c4.appl.workflow.at
import edu.obya.c4.appl.workflow.enricher.*
import edu.obya.c4.appl.workflow.filter.*
import edu.obya.c4.appl.workflow.writer.C4DocumentWriter
import edu.obya.c4.appl.workflow.writer.StructurizrPrinter
import edu.obya.c4.domain.*
import edu.obya.c4.domain.strategy.*
import edu.obya.c4.domain.structurizr.StructurizrModel
import edu.obya.c4.domain.structurizr.enricher.C4AsyncRelationshipsInferrer
import edu.obya.c4.domain.structurizr.enricher.C4SyncRelationshipsTagger
import edu.obya.c4.external.broker.RabbitMQImporter
import edu.obya.c4.external.metadata.MetadataFileReader
import edu.obya.c4.external.structurizr.*
import edu.obya.c4.external.tracing.JaegerImporter
import edu.obya.c4.external.tracing.TraefikImporter
import edu.obya.c4.util.logger
import edu.obya.c4.util.toFilePaths
import edu.obya.c4.util.toFileStream
import com.structurizr.Workspace
import java.io.OutputStreamWriter
import java.nio.file.Path
import java.nio.file.Paths

class SynchronizeUseCase(
    val localReaderProvider: () -> C4Reader<Path, Long, Workspace> = { StructurizrFileReader() },
    val remoteReaderProvider: () -> C4Reader<CloudId, Long, Workspace> = { StructurizrCloudReader() },
    val remoteWriterProvider: () -> C4Writer<Long, Workspace, CloudId> = { StructurizrCloudWriter() },
    val metadataReaderProvider: (Naming) -> C4Reader<Path, Long, Workspace> = { MetadataFileReader(it.toCanonizer()) },
    val localWeightsProvider: () -> PropertyWeights = { WeightsProvider.read("local") },
    val remoteWeightsProvider: () -> PropertyWeights = { WeightsProvider.read("remote") },
    val metadataWeightsProvider: () -> PropertyWeights = { WeightsProvider.read("metadata") },
    val namingProvider: () -> Naming = { NamingProvider.read() },
    val nodeMappingProvider: () -> NodeMapping = { NodeMappingProvider.read() },
    val viewNamingProvider: () -> ViewNaming = { ViewNamingProvider.read() }
) {
    private val logger = this.javaClass.logger()

    fun run(options: Set<AppOption<Option>>): StructurizrModel? {
        return Workflow(options.toFilters()).run().at("model")
    }

    private fun Set<AppOption<Option>>.toFilters(): List<Filter> {
        val filters = mutableListOf<Filter>()
        var naming: Naming = emptyMap()
        var viewFilter: C4ViewFilter = NoViewFilter
        val nodeMapping: Naming = nodeMappingProvider()
        val viewNamer = C4ViewNamer(viewNamingProvider())

        withDefaultOptions().sortedBy { it.id.ordinal }.onEach { option ->
            logger.info("Handling option ${option.id}")
            when (option.id) {
                Option.WithNaming -> naming = c4Naming(option)
                Option.WithDownload -> filters.add(c4Download(option))
                Option.WithUpload -> filters.add(c4Upload(option, this.at(Option.WithDownload)))
                Option.WithViewCount -> viewFilter = c4ViewCount(option)
                Option.WithViewFilter -> viewFilter = c4ViewFilter(option)
                Option.WithDsl -> filters.addAll(c4FromDsl(option, naming, viewFilter, nodeMapping))
                Option.WithMetadata -> filters.addAll(c4FromMetadata(option, naming, viewFilter, nodeMapping))
                Option.WithBroker -> filters.add(c4FromBroker(naming))
                Option.WithOpenTracing -> filters.add(c4FromOpenTracing())
                Option.WithHttpTracing -> filters.add(c4FromHttpTracing())
                Option.WithJar -> filters.addAll(c4FromJar(option))
                Option.WithRelationshipInfer -> filters.addAll(c4RelationshipInference(naming))
                Option.WithRelationshipEnrich -> filters.addAll(c4RelationshipEnrichment())
                Option.WithViewGen -> filters.addAll(c4ViewGeneration(option, viewFilter, viewNamer))
                Option.WithViewEnrich -> filters.addAll(c4ViewEnrichment())
                Option.WithViewClean -> filters.addAll(c4ViewCleanup())
                Option.WithComponentEnrich -> filters.addAll(c4ComponentEnrichment())
                Option.WithSiteGen -> filters.add(c4SiteGeneration(option))
                Option.WithPrint -> filters.add(c4Print())
            }
        }
        return filters
    }

    private fun Set<AppOption<Option>>.withDefaultOptions() =
        this + AppOption(Option.WithNaming)

    private fun c4Naming(option: AppOption<Option>): Naming {
        return option.oneParameter()
            ?.let { Paths.get(it) }
            ?.toFileStream(logger)
            ?.readNaming()
            ?: namingProvider()
    }

    private fun c4Download(option: AppOption<Option>): Filter {
        return option.takeParameters(3)
            .toCloudId()
            .let { C4ModelImportFilter("download ${it.id}", it, remoteWeightsProvider(), remoteReaderProvider()) }
    }

    private fun c4FromDsl(
        option: AppOption<Option>,
        naming: Naming,
        viewFilter: C4ViewFilter,
        nodeMapping: NodeMapping
    ): List<Filter> {

        val weights = localWeightsProvider()
        val reader = localReaderProvider()

        return option.allParameters()
            .mapNotNull { runCatching { Paths.get(it) }.getOrNull() }
            .toSet()
            .toFilePaths("*.{dsl,json}")
            .flatMap {
                listOf(
                    C4ModelImportFilter("load $it", it, weights, reader),
                    C4ModelMergeFilter(
                        "merge $it", C4Model.MergeContext(
                            nameComparator = naming.toComparator(),
                            nameCanonizer = naming.toCanonizer(),
                            viewFilter = viewFilter,
                            nodeMapper = nodeMapping.toMapper()
                        )
                    )
                )
            }
    }

    private fun c4FromMetadata(
        option: AppOption<Option>,
        naming: Naming,
        viewFilter: C4ViewFilter,
        nodeMapping: NodeMapping
    ): List<Filter> {

        val weights = metadataWeightsProvider()
        val reader = metadataReaderProvider(naming)

        return option.allParameters()
            .mapNotNull { runCatching { Paths.get(it) }.getOrNull() }
            .toSet()
            .toFilePaths("*.{yaml}")
            .flatMap {
                listOf(
                    C4ModelImportFilter("load $it", it, weights, reader),
                    C4ModelMergeFilter(
                        "merge $it", C4Model.MergeContext(
                            nameComparator = naming.toComparator(),
                            nameCanonizer = naming.toCanonizer(),
                            viewFilter = viewFilter,
                            nodeMapper = nodeMapping.toMapper()
                        )
                    )
                )
            }
    }

    private fun c4FromJar(option: AppOption<Option>): List<Filter> {
        return listOf()
    }

    private fun c4FromBroker(naming: Naming): Filter {
        return C4ModelEnrichmentFilter("enrich with broker", RabbitMQImporter(naming.toCanonizer()))
    }

    private fun c4FromOpenTracing(): Filter {
        return C4ModelEnrichmentFilter("enrich with jaeger", JaegerImporter)
    }

    private fun c4FromHttpTracing(): Filter {
        return C4ModelEnrichmentFilter("enrich with traefik", TraefikImporter)
    }

    private fun c4ViewCount(option: AppOption<Option>): C4ViewFilter {
        return ViewCountFilter(option.oneParameterOrFail().toInt())
    }

    private fun c4ViewFilter(option: AppOption<Option>): C4ViewFilter {
        return option.allParameters()
            .takeIf { it.isNotEmpty() }
            ?.let { ViewKeyFilter(it.toSet()) }
            ?: NoViewFilter
    }

    private class ViewCountFilter(val threshold: Int) : C4ViewFilter {
        private val logger = this.javaClass.logger()
        private var counter = 0
        override fun filter(key: String) = (counter++ < threshold).also { logger.info("Filter $key C4 view: $it") }
    }

    private class ViewKeyFilter(val keys: Set<String>) : C4ViewFilter {
        private val logger = this.javaClass.logger()
        override fun filter(key: String) = keys.contains(key).also { logger.info("Filter $key C4 view: $it") }
    }

    private fun c4RelationshipInference(naming: Naming): List<Filter> {
        return listOf(
            C4ModelEnrichmentFilter(
                "infer asynchronous relationships", C4AsyncRelationshipsInferrer(naming.toCanonizer())
            )
        )
    }

    private fun c4RelationshipEnrichment(): List<Filter> {
        return listOf(
            C4ModelEnrichmentFilter("tag synchronous relationships", C4SyncRelationshipsTagger),
            C4ModelEnrichmentFilter("document asynchronous relationships", C4SyncRelationshipsDocumenter)
        )
    }

    private fun c4ViewEnrichment(): List<Filter> {
        return listOf(
            C4ModelEnrichmentFilter("add queues in component views", C4ServiceComponentViewEnricher)
        )
    }

    private fun c4ComponentEnrichment(): List<Filter> {
        return listOf(
            C4ModelEnrichmentFilter("document queue components", C4QueueComponentsDocumenter)
        )
    }

    private fun c4ViewCleanup(): List<Filter> {
        return listOf(
            C4ModelEnrichmentFilter("clean relationships in context views", C4ServiceContextViewCleaner),
            C4ModelEnrichmentFilter("clean relationships in component views", C4ServiceComponentViewCleaner)
        )
    }

    private fun c4ViewGeneration(
        option: AppOption<Option>,
        viewFilter: C4ViewFilter,
        viewNamer: C4ViewNamer
    ): List<Filter> {
        val candidateFilters = mapOf(
            ViewCategory.Landscape to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.Landscape}",
                    C4LandscapeViewGenerator(viewFilter, viewNamer)
                )
            ),
            ViewCategory.System to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.System}",
                    C4SystemContextViewGenerator(viewFilter, viewNamer)
                )
            ),
            ViewCategory.Services to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.Services}",
                    C4MicroservicesContainerViewGenerator(viewFilter, viewNamer)
                )
            ),
            ViewCategory.Service to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.Service}",
                    C4ServiceContextContainerViewGenerator(viewFilter, viewNamer)
                )
            ),
            ViewCategory.Ownership to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.Ownership}",
                    C4OwnershipContainerViewGenerator(viewFilter, viewNamer)
                )
            ),
            ViewCategory.Components to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.Components}",
                    C4ServiceComponentViewGenerator(viewFilter, viewNamer)
                )
            ),
            ViewCategory.Deployment to listOf(
                C4ViewGenerationFilter(
                    "viewgen:${ViewCategory.Deployment}",
                    C4ServiceDeploymentViewGenerator(viewFilter, viewNamer, "production")
                )
            )
        ).mapKeys { it.key.name.lowercase() }

        val parameters = option.allParameters()
        return if (parameters.isNotEmpty()) parameters.flatMap { candidateFilters[it] ?: emptyList() }
        else candidateFilters.values.flatten()
    }

    private fun c4Upload(uploadOption: AppOption<Option>, downloadOption: AppOption<Option>?): Filter {
        val uploadId = uploadOption
            .allParameters()
            .takeIf { it.isNotEmpty() }
            ?.toCloudId()

        val downloadId = downloadOption
            ?.allParameters()
            ?.takeIf { it.isNotEmpty() }
            ?.toCloudId()

        return (uploadId ?: downloadId)
            ?.let { C4ModelExportFilter("upload ${it.id}", it, StructurizrCloudWriter()) }
            ?: throw IllegalArgumentException("No specified cloud id for upload operation")
    }

    private fun c4SiteGeneration(option: AppOption<Option>): Filter {
        return option.oneParameterOrFail()
            .let { Paths.get(it).toFile() }
            .let { C4ModelExportFilter("sitegen", OutputStreamWriter(it.outputStream()), C4DocumentWriter()) }
    }

    private fun c4Print(): Filter {
        return C4ModelExportFilter("print", System.out, StructurizrPrinter())
    }

    enum class ViewCategory {
        Landscape,
        System,
        Services,
        Service,
        Ownership,
        Components,
        Deployment
    }

    enum class Option(override val id: String) : AppOptionId {
        WithNaming("-naming"),
        WithDownload("-download"),
        WithMetadata("-metadata"),
        WithViewCount("-viewcount"),
        WithViewFilter("-viewfilter"),
        WithDsl("-dsl"),
        WithBroker("-broker"),
        WithOpenTracing("-opentracing"),
        WithHttpTracing("-httptracing"),
        WithJar("-jar"),
        WithComponentEnrich("-componentenrich"),
        WithRelationshipInfer("-relationshipinfer"),
        WithRelationshipEnrich("-relationshipenrich"),
        WithViewGen("-viewgen"),
        WithViewEnrich("-viewenrich"),
        WithViewClean("-viewclean"),
        WithUpload("-upload"),
        WithSiteGen("-sitegen"),
        WithPrint("-print");

        companion object {
            fun fromId(id: String): Option? = values().firstOrNull { it.id == id }
        }
    }

    private fun List<AppParameter>.toCloudId(): CloudId =
        CloudId(
            id = this[0].toLong(),
            credential = Credential(apiKey = this[1], apiSecret = this[2])
        )
}
