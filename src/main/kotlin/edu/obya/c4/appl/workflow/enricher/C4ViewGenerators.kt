package edu.obya.c4.appl.workflow.enricher

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.C4Tags
import edu.obya.c4.domain.C4ViewFilter
import edu.obya.c4.domain.NoViewFilter
import edu.obya.c4.domain.enricher.C4ViewGenerator
import edu.obya.c4.domain.structurizr.util.*
import edu.obya.c4.util.logger
import edu.obya.c4.util.normalize
import com.structurizr.Workspace
import com.structurizr.model.Container
import com.structurizr.model.DeploymentNode
import com.structurizr.model.Person
import com.structurizr.model.SoftwareSystem
import com.structurizr.view.DeploymentView
import com.structurizr.view.PaperSize


class C4LandscapeViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        val viewKey = viewNamer.name(C4ViewType.SYSTEM_LANDSCAPE, mapOf())

        with(model.state.views) {
            if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                with(this.createSystemLandscapeView(viewKey, "All the stakeholders")) {
                    addDefaultElements()

                    paperSize = PaperSize.A4_Landscape
                    disableAutomaticLayout()

                    logger.debug("view ${this.key} created")
                }
            }
        }
        return model
    }
}

class C4SystemContextViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        model.allSystems().onEach { system ->
            val viewKey = viewNamer.name(C4ViewType.SYSTEM_CONTEXT, mapOf())

            with(model.state.views) {
                if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                    with(this.createSystemContextView(system, viewKey, "The context of ${system.name}")) {
                        addDefaultElements()

                        paperSize = PaperSize.A4_Landscape
                        disableAutomaticLayout()

                        logger.debug("view ${this.key} created")
                    }
                }
            }
        }
        return model
    }
}

class C4MicroservicesContainerViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        model.allSystems().onEach { system ->
            val viewKey = viewNamer.name(C4ViewType.SYSTEM_SERVICES, mapOf())

            with(model.state.views) {
                if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                    with(
                        this.createContainerView(
                            system, viewKey,
                            "The services inside ${system.name}"
                        )
                    ) {
                        system.allMicroservices().onEach { add(it, true) }

                        paperSize = PaperSize.A4_Landscape
                        disableAutomaticLayout()

                        logger.debug("view ${this.key} created")
                    }
                }
            }
        }
        return model
    }
}

class C4OwnershipContainerViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        val teams = model.allDevelopmentTeams()

        model.allSystems().onEach { system ->
            system.allMicroservices()
                .flatMap { service -> teams.filter { service.ownedBy(it) }.map { it to service } }
                .groupBy({ (team, _) -> team }, { (_, service) -> service })
                .onEach { ownership ->
                    val viewKey = viewNamer.name(C4ViewType.OWNERSHIP, mapOf("owner" to ownership.key.name.normalize()))

                    with(model.state.views) {
                        if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                            with(
                                this.createContainerView(
                                    system, viewKey,
                                    "The services owned by ${ownership.key.name} team"
                                )
                            ) {
                                ownership.value.onEach { service ->
                                    add(service)
                                    addNearestNeighbours(service)
                                }
                                withoutPerson()
                                withoutCommonInfrastructure()
                                withoutStorageInfrastructure()
                                withoutRelationshipsNotConnectedToOwnership(ownership.key)

                                paperSize = PaperSize.A4_Landscape
                                disableAutomaticLayout()

                                logger.debug("view ${this.key} created")
                            }
                        }
                    }
                }
        }
        return model
    }
}

class C4ServiceContextContainerViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        model.allSystems().onEach { system ->
            system.allMicroservices().onEach { service ->
                val viewKey = viewNamer.name(C4ViewType.SERVICE_CONTEXT, mapOf("service" to service.name.normalize()))

                with(model.state.views) {
                    if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                        with(
                            this.createContainerView(
                                service.softwareSystem, viewKey,
                                "The context of ${service.name} service"
                            )
                        ) {
                            add(service)
                            addNearestNeighbours(service)
                            serviceContextOnly(service)

                            paperSize = PaperSize.A4_Landscape
                            disableAutomaticLayout()

                            logger.debug("view ${this.key} created")
                        }
                    }
                }
            }
        }
        return model
    }
}

class C4ServiceComponentViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        model.allSystems().onEach { system ->
            system.allMicroservices().onEach { service ->
                val viewKey = viewNamer.name(C4ViewType.COMPONENTS, mapOf("service" to service.name.normalize()))

                with(model.state.views) {
                    if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                        with(
                            this.createComponentView(
                                service, viewKey,
                                "The software components inside ${service.name} service"
                            )
                        ) {
                            addDefaultElements()
                            withQueueDependencies()
                            withDatabaseSchemaDependencies()
                            componentRelationshipsOnly()

                            paperSize = PaperSize.A4_Landscape
                            disableAutomaticLayout()

                            logger.debug("view ${this.key} created")
                        }
                    }
                }
            }
        }
        return model
    }
}

class C4ServiceDeploymentViewGenerator(
    override val viewFilter: C4ViewFilter = NoViewFilter,
    private val viewNamer: C4ViewNamer,
    private val targetEnvironment: String
) : C4ViewGenerator<Long, Workspace> {

    private val logger = this.javaClass.logger()

    override fun generate(model: C4Model<Long, Workspace>): C4Model<Long, Workspace> {
        model.allSystems().onEach { system ->
            val containerNodesMap = system.model.deploymentNodes.leafNodes().byContainerId()

            system.allMicroservices().onEach { service ->
                val viewKey = viewNamer.name(C4ViewType.DEPLOYMENT, mapOf("service" to service.name.normalize()))

                with(model.state.views) {
                    if (this.hasByKey(viewKey).not() && viewFilter.filter(viewKey)) {
                        with(
                            this.createDeploymentView(
                                system, viewKey,
                                "The deployment and infrastructure nodes for the operating of ${service.name} service",
                            )
                        ) {
                            deployContainer(service, containerNodesMap)

                            environment = targetEnvironment
                            paperSize = PaperSize.A4_Landscape
                            disableAutomaticLayout()

                            logger.debug("view ${this.key} created")
                        }
                    }
                }
            }
        }
        return model
    }

    private fun DeploymentView.deployContainer(container: Container, nodesMap: Map<String, DeploymentNode>) {
        nodesMap[container.id]?.let { this.add(it, true) }

        val neighbors = container.relationships
            .map { it.destination }
            .filterIsInstance<Container>()

        neighbors
            .filter { it.tagsAsSet.intersect(C4Tags.Infrastructure.common).isNotEmpty() }
            .mapNotNull { nodesMap[container.id] }
            .onEach { this.add(it, true) }
    }
}
