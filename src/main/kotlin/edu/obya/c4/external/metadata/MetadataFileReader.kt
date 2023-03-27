package edu.obya.c4.external.metadata

import edu.obya.c4.domain.*
import edu.obya.c4.domain.strategy.NameCanonizer
import edu.obya.c4.domain.structurizr.StructurizrModel
import edu.obya.c4.domain.structurizr.util.isCommandQueue
import edu.obya.c4.domain.structurizr.util.isEventQueue
import edu.obya.c4.util.JOCKER
import edu.obya.c4.util.logger
import com.structurizr.Workspace
import com.structurizr.model.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

class MetadataFileReader(private val nameCanonizer: NameCanonizer) : C4Reader<Path, Long, Workspace> {
    private val logger = this.javaClass.logger()

    private val jsonMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json")
    private val yamlMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.yaml")

    override fun read(source: Path): C4Model<Long, Workspace>? =
        when {
            jsonMatcher.matches(source) -> fromJson(source)
            yamlMatcher.matches(source) -> fromYaml(source)
            else -> null.also {
                logger.warn("C4 model could not be read from ${source}, cause is unsupported format.")
            }
        }

    private fun fromJson(source: Path): C4Model<Long, Workspace>? =
        runCatching { MetadataDslParser().parseJson(source.toFile()).toWorkspace() }
            .onFailure { logger.warn("C4 model could not be read from ${source}, cause is $it") }
            .onSuccess { logger.info("C4 model has been successfully loaded from $source.") }
            .getOrNull()
            ?.let { StructurizrModel(C4ModelId(it.id), it) }

    private fun fromYaml(source: Path): C4Model<Long, Workspace>? =
        runCatching { MetadataDslParser().parseYaml(source.toFile()).toWorkspace() }
            .onFailure { logger.warn("C4 model could not be read from ${source}, cause is $it") }
            .onSuccess { logger.info("C4 model has been successfully loaded from $source.") }
            .getOrNull()
            ?.let { StructurizrModel(C4ModelId(it.id), it) }

    private fun MetadataDslParser.MetaRepository.toWorkspace(): Workspace {
        val workspace = Workspace(JOCKER, JOCKER)
        val softwareSystem = workspace.model.addSoftwareSystem("EL system")

        this.components.onEach {
            val componentContainer = it.toContainer(softwareSystem)
            componentContainer.ownedBy(it.value.owner.toPerson(workspace.model))
            it.value.dependencies?.containerize(softwareSystem, componentContainer)
        }
        return workspace
    }

    private fun MetaOwner.toPerson(model: Model) = model.getPersonWithName(this)
        ?: model.addPerson(this)

    private fun Map.Entry<MetaName, MetadataDslParser.MetaRepository.MetaComponent>.toContainer(
        softwareSystem: SoftwareSystem
    ): Container {
        val metaComponentContainerName = nameCanonizer(this.key)
        val metaComponentContainer = softwareSystem.getContainerWithName(metaComponentContainerName)
            ?: softwareSystem.addContainer(metaComponentContainerName, "", "")

        metaComponentContainer.addTags(
            nameCanonizer("${this.value.owner}-team"),
            nameCanonizer(this.value.classification.app_type),
            nameCanonizer(this.value.classification.service_type)
        )
        metaComponentContainer.url = this.value.documentation?.url
        return metaComponentContainer
    }

    private fun MetadataDslParser.MetaRepository.MetaComponent.MetaDependencies.containerize(
        softwareSystem: SoftwareSystem,
        metaComponentContainer: Container
    ) {
        this.toContainers(softwareSystem).onEach { metaComponentContainer.dependsOn(it) }

        this.data?.let { data ->
            data.onEach { dataStoreType ->
                when {
                    dataStoreType.key.isDatabase() -> {
                        dataStoreType.value.onEach { dataStore ->
                            val dataContainer =
                                "${dataStoreType.key}-${dataStore.key}".toDatabaseContainer(softwareSystem)
                            metaComponentContainer.persistenceWith(dataContainer)
                            dataStore.value.onEach { schema ->
                                metaComponentContainer.persistenceWith(
                                    dataContainer,
                                    schema.toDatabaseComponent(dataContainer)
                                )
                            }
                        }
                    }
                    dataStoreType.key.isCache() -> {
                        dataStoreType.value.onEach { dataStore ->
                            val dataContainer =
                                "${dataStoreType.key}-${dataStore.key}".toDatabaseContainer(softwareSystem)
                            metaComponentContainer.cachingWith(dataContainer)
                            dataStore.value.onEach { schema ->
                                metaComponentContainer.cachingWith(schema.toDatabaseComponent(dataContainer))
                            }
                        }
                    }
                    else -> {
                        dataStoreType.value.onEach { dataStore ->
                            val brokerContainer =
                                "${dataStoreType.key}-${dataStore.key}".toBrokerContainer(softwareSystem)
                            dataStore.value.onEach { queue -> queue.toBrokerComponent(brokerContainer) }
                        }
                    }
                }
            }
        }
    }

    private fun MetadataDslParser.MetaRepository.MetaComponent.MetaDependencies.toContainers(softwareSystem: SoftwareSystem): List<Container> =
        this.components.map { it.toContainer(softwareSystem) }

    private fun MetaDataStoreInstance.toDatabaseContainer(softwareSystem: SoftwareSystem): Container {
        val containerName = nameCanonizer(this)
        val container = softwareSystem.getContainerWithName(containerName)
            ?: softwareSystem.addContainer(containerName, "", "")
        container.addTags(C4Tags.Infrastructure.INFRA, C4Tags.Infrastructure.DATABASE)
        return container
    }

    private fun MetaDataStoreInstance.toBrokerContainer(softwareSystem: SoftwareSystem): Container {
        val containerName = nameCanonizer(this)
        val container = softwareSystem.getContainerWithName(containerName)
            ?: softwareSystem.addContainer(containerName, "", C4Technologies.RABBITMQ)
        container.addTags(C4Tags.Infrastructure.INFRA, C4Tags.Infrastructure.BROKER)
        return container
    }

    private fun MetaDependency.toContainer(softwareSystem: SoftwareSystem): Container {
        val containerName = nameCanonizer(this)
        return softwareSystem.getContainerWithName(containerName)
            ?: softwareSystem.addContainer(containerName, "", "")
    }

    private fun MetaDataStore.components(): List<MetaDataStoreContent> = this.entries.flatMap { it.value }

    private fun MetaDataStoreType.isBroker() = this.equals(C4Technologies.RABBITMQ, ignoreCase = true)

    private fun MetaDataStoreType.isCache() = this.equals(C4Technologies.REDIS, ignoreCase = true)

    private fun MetaDataStoreType.isDatabase() = when (this.lowercase()) {
        C4Technologies.CASSANDRA.lowercase(),
        C4Technologies.MYSQL.lowercase(),
        C4Technologies.POSTGRESQL.lowercase() -> true
        else -> false
    }

    private fun MetaDataStoreContent.toDatabaseComponent(container: Container): Component {
        val metaComponentName = "${container.name}:$this"
        val metaComponent = container.getComponentWithName(metaComponentName)
            ?: container.addComponent(metaComponentName, "", container.technology)
        metaComponent.addTags(C4Tags.Infrastructure.INFRA, C4Tags.Infrastructure.DATABASE)
        metaComponent.addTags(
            *(when (container.technology.lowercase()) {
                C4Technologies.CASSANDRA.lowercase() -> arrayOf(C4Tags.Infrastructure.KEYSPACE)
                else -> arrayOf(C4Tags.Infrastructure.SCHEMA)
            })
        )
        return metaComponent
    }

    private fun MetaDataStoreContent.toBrokerComponent(container: Container): Component {
        val metaComponentName = this
        val metaComponent = container.getComponentWithName(metaComponentName)
            ?: container.addComponent(this, "", C4Technologies.AMQP)

        metaComponent.addTags(C4Tags.Infrastructure.INFRA, C4Tags.Infrastructure.QUEUE)
        if (metaComponent.isCommandQueue()) metaComponent.addTags(C4Tags.Architecture.COMMAND)
        else if (metaComponent.isEventQueue()) metaComponent.addTags(C4Tags.Architecture.EVENT)

        return metaComponent
    }

    private fun Container.ownedBy(person: Person) {
        person.uses(this, "owns")?.addTags(C4Tags.Organization.OWNERSHIP)
    }

    private fun Container.persistenceWith(databaseContainer: Container) {
        when (databaseContainer.technology.lowercase()) {
            C4Technologies.CASSANDRA.lowercase() -> this.cqlWith(databaseContainer)
            else -> this.sqlWith(databaseContainer)
        }
    }

    private fun Container.persistenceWith(databaseContainer: Container, databaseComponent: Component) {
        when (databaseContainer.technology.lowercase()) {
            C4Technologies.CASSANDRA.lowercase() -> this.cqlWith(databaseComponent)
            else -> this.sqlWith(databaseComponent)
        }
    }

    private fun Container.cachingWith(databaseContainer: Container) {
        this.uses(databaseContainer, "reads and writes")
    }

    private fun Container.cachingWith(databaseComponent: Component) {
        this.uses(databaseComponent, "reads and writes")
    }

    private fun Container.dependsOn(otherContainer: Container) {
        when (otherContainer.name.lowercase()) {
            C4Technologies.CONSUL.lowercase() -> this.uses(otherContainer, "sends beats to")
            C4Technologies.GRAPHITE.lowercase() -> this.uses(otherContainer, "sends metrics to")
            C4Technologies.LOGSTASH.lowercase() -> this.uses(otherContainer, "sends log events to")
            C4Technologies.JAEGER.lowercase() -> this.uses(otherContainer, "sends spans to")
            C4Technologies.STATSD.lowercase() -> this.uses(otherContainer, "sends metrics to")
            else -> this.uses(otherContainer, "depends on")
        }
    }

    private fun Container.cqlWith(databaseContainer: Container) {
        this.uses(databaseContainer, "reads and writes", C4Technologies.CQL)
    }

    private fun Container.cqlWith(databaseComponent: Component) {
        this.uses(databaseComponent, "reads and writes", C4Technologies.CQL)
    }

    private fun Container.sqlWith(databaseContainer: Container) {
        this.uses(databaseContainer, "reads and writes", C4Technologies.SQL)
    }

    private fun Container.sqlWith(databaseComponent: Component) {
        this.uses(databaseComponent, "reads and writes", C4Technologies.SQL)
    }
}
