package com.edgelab.c4.domain.structurizr.strategy

import com.edgelab.c4.domain.C4ViewFilter
import com.edgelab.c4.domain.strategy.*
import com.edgelab.c4.domain.structurizr.util.*
import com.edgelab.c4.util.logger
import com.edgelab.c4.util.notEmpty
import com.edgelab.c4.util.notNull
import com.structurizr.model.*
import com.structurizr.view.*

class MergeViewSetStrategy(
    val nameComparator: NameComparator,
    val nameCanonizer: NameCanonizer,
    val viewFilter: C4ViewFilter,
    elements: Set<Element>,
    nodes: Set<DeploymentNode>
) {
    private val elementComparator = elementComparator(nameComparator)
    private val viewComparator = { a: View, b: View -> nameComparator(a.key, b.key) }
    private val filteredViewComparator = { a: FilteredView, b: FilteredView -> nameComparator(a.key, b.key) }
    private val elementViewComparator = elementViewComparator(elementComparator)
    private val relationshipViewComparator = relationshipViewComparator(relationshipComparator(elementComparator))

    private val elementsMap = elements.allStatic().byName()
    private val allNodes = nodes.allNodes()
    private val leafNodes = allNodes.leafNodes()
    private val leafToRootNodePaths = leafNodes.pathToRoot()
    private val nodesMap = allNodes.byNameWithEnvironment()
    private val viewsMap = mutableMapOf<String, View>()

    private val elementFinder = { name: String -> elementsMap[name] }

    private val logger = this.javaClass.logger()

    fun execute(
        master: ViewSet,
        other: ViewSet,
        scores: PropertyScores,
        weights: PropertyWeights
    ): List<(ViewSet) -> Unit> {

        fun <T : View> limitedAdd(view: T, promotion: (ViewSet) -> Unit): (ViewSet) -> Unit = {
            if (viewFilter.filter(view.key)) promotion(it)
        }

        val landscapeV = "viewSet.systemLandscapeViews".promoteValues<SystemLandscapeView, ViewSet>(
            master.systemLandscapeViews.toSet(), scores, other.systemLandscapeViews.toSet(), weights, viewComparator,
            { limitedAdd(it, it.add(scores, weights)) },
            { a, b -> listOf(limitedAdd(a, a.merge(b, scores, weights))) }
        )
        val systemV = "viewSet.systemContextViews".promoteValues<SystemContextView, ViewSet>(
            master.systemContextViews.toSet(), scores, other.systemContextViews.toSet(), weights, viewComparator,
            { limitedAdd(it, it.add(scores, weights).promote(it.softwareSystem.name.canonize(nameCanonizer))) },
            { a, b ->
                listOf(
                    limitedAdd(
                        a,
                        a.merge(b, scores, weights).promote(a.softwareSystem.name.canonize(nameCanonizer))
                    )
                )
            }
        )
        val containerV = "viewSet.containerViews".promoteValues<ContainerView, ViewSet>(
            master.containerViews.toSet(), scores, other.containerViews.toSet(), weights, viewComparator,
            { limitedAdd(it, it.add(scores, weights).promote(it.softwareSystem.name.canonize(nameCanonizer))) },
            { a, b ->
                listOf(
                    limitedAdd(
                        a,
                        a.merge(b, scores, weights).promote(a.softwareSystem.name.canonize(nameCanonizer))
                    )
                )
            }
        )
        val componentV = "viewSet.componentViews".promoteValues<ComponentView, ViewSet>(
            master.componentViews.toSet(), scores, other.componentViews.toSet(), weights, viewComparator,
            { limitedAdd(it, it.add(scores, weights).promote(it.container.name.canonize(nameCanonizer))) },
            { a, b ->
                listOf(
                    limitedAdd(
                        a,
                        a.merge(b, scores, weights).promote(a.container.name.canonize(nameCanonizer))
                    )
                )
            }
        )
        val dynamicV = "viewSet.dynamicViews".promoteValues<DynamicView, ViewSet>(
            master.dynamicViews.toSet(), scores, other.dynamicViews.toSet(), weights, viewComparator,
            { limitedAdd(it, it.add(scores, weights).promote(it.element.name.canonize(nameCanonizer))) },
            { a, b ->
                listOf(
                    limitedAdd(
                        a,
                        a.merge(b, scores, weights).promote(a.element.name.canonize(nameCanonizer))
                    )
                )
            }
        )
        val filteredV = "viewSet.filteredViews".promoteValues<FilteredView, ViewSet>(
            master.filteredViews.toSet(), scores, other.filteredViews.toSet(), weights, filteredViewComparator,
            { it.add(scores, weights) },
            { a, b -> listOf(a.merge(b, scores, weights)) }
        )
        val deploymentV = "viewSet.deploymentViews".promoteValues<DeploymentView, ViewSet>(
            master.deploymentViews.toSet(), scores, other.deploymentViews.toSet(), weights, viewComparator,
            { limitedAdd(it, it.add(scores, weights).promote(it.softwareSystem.name.canonize(nameCanonizer))) },
            { a, b ->
                listOf(
                    limitedAdd(
                        a,
                        a.merge(b, scores, weights).promote(a.softwareSystem.name.canonize(nameCanonizer))
                    )
                )
            }
        )

        return landscapeV + systemV + containerV + componentV + dynamicV + filteredV + deploymentV
    }

    private fun SystemLandscapeView.merge(
        other: SystemLandscapeView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet) -> Unit =
        { viewSet ->
            val view = viewSet.createSystemLandscapeView(
                this.key,
                "view.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                ).notNull()
            )
            view.paperSize = "view.paperSize".electValue(this.paperSize, scores, other.paperSize, weights)
            view.title =
                "view.title".electValue(this.title?.notEmpty(), scores, other.title?.notEmpty(), weights).notNull()
            view.isEnterpriseBoundaryVisible = true
            view.disableAutomaticLayout()

            "view.elements".electValue(this.elements, scores, other.elements, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".electValue(
                this.animations.notEmpty()?.toAnimatedElementSet(this.model),
                scores,
                other.animations.notEmpty()?.toAnimatedElementSet(other.model),
                weights
            )
                ?.let { view.completeAnimation(it) }

            "view.relationships".electValue(this.relationships, scores, other.relationships, weights)
                ?.let { removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun SystemLandscapeView.add(
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet) -> Unit =
        { viewSet ->
            val view = viewSet.createSystemLandscapeView(
                this.key,
                "view.description".takeValue(this.description, scores, weights)
            )
            view.paperSize = "view.paperSize".takeValue(this.paperSize, scores, weights)
            view.title = "view.title".takeValue(this.title, scores, weights)
            view.isEnterpriseBoundaryVisible = true
            view.disableAutomaticLayout()

            "view.elements".takeValue(this.elements, scores, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".takeValue(this.animations.toAnimatedElementSet(this.model), scores, weights)
                ?.let { view.completeAnimation(it) }

            "view.relationships".takeValue(this.relationships, scores, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun SystemContextView.merge(
        other: SystemContextView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, SoftwareSystem) -> Unit =
        { viewSet, softwareSystem ->
            val view = viewSet.createSystemContextView(
                softwareSystem, this.key,
                "view.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                ).notNull()
            )
            view.paperSize = "view.paperSize".electValue(this.paperSize, scores, other.paperSize, weights)
            view.title =
                "view.title".electValue(this.title?.notEmpty(), scores, other.title?.notEmpty(), weights).notNull()
            view.isEnterpriseBoundaryVisible = true
            view.disableAutomaticLayout()

            "view.elements".electValue(this.elements, scores, other.elements, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".electValue(
                this.animations.notEmpty()?.toAnimatedElementSet(this.model),
                scores,
                other.animations.notEmpty()?.toAnimatedElementSet(other.model),
                weights
            )
                ?.let { view.completeAnimation(it) }

            "view.relationships".electValue(this.relationships, scores, other.relationships, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun StaticView.completeAnimation(electedAnimation: List<Set<Element>>) {
        val viewElementNames = this.elements.map { it.element.name }
        electedAnimation.onEach { electedElements ->
            val viewAnimatedElements = electedElements
                .filter { viewElementNames.contains(it.name) }
                .mapNotNull { elementsMap[it.name] }
            if (viewAnimatedElements.isNotEmpty()) {
                this.addAnimation(*(viewAnimatedElements.toTypedArray()))
            }
        }
    }

    private fun SystemContextView.add(
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, SoftwareSystem) -> Unit =
        { viewSet, softwareSystem ->
            val view = viewSet.createSystemContextView(
                softwareSystem, this.key,
                "view.description".takeValue(this.description, scores, weights)
            )
            view.paperSize = "view.paperSize".takeValue(this.paperSize, scores, weights)
            view.title = "view.title".takeValue(this.title, scores, weights)
            view.isEnterpriseBoundaryVisible = true
            view.disableAutomaticLayout()

            "view.elements".takeValue(this.elements, scores, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".takeValue(this.animations.toAnimatedElementSet(this.model), scores, weights)
                ?.let { view.completeAnimation(it) }

            "view.relationships".takeValue(this.relationships, scores, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun ContainerView.merge(
        other: ContainerView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, SoftwareSystem) -> Unit =
        { viewSet, softwareSystem ->
            val view = viewSet.createContainerView(
                softwareSystem, this.key,
                "view.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                ).notNull()
            )
            view.paperSize = "view.paperSize".electValue(this.paperSize, scores, other.paperSize, weights)
            view.title =
                "view.title".electValue(this.title?.notEmpty(), scores, other.title?.notEmpty(), weights).notNull()
            view.externalSoftwareSystemBoundariesVisible = true
            view.disableAutomaticLayout()

            "view.elements".electValue(this.elements, scores, other.elements, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".electValue(
                this.animations.notEmpty()?.toAnimatedElementSet(this.model),
                scores,
                other.animations.notEmpty()?.toAnimatedElementSet(other.model),
                weights
            )
                ?.let { view.completeAnimation(it) }

            "view.relationships".electValue(this.relationships, scores, other.relationships, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun ContainerView.add(scores: PropertyScores, weights: PropertyWeights): (ViewSet, SoftwareSystem) -> Unit =
        { viewSet, softwareSystem ->
            val view = viewSet.createContainerView(
                softwareSystem, this.key,
                "view.description".takeValue(this.description, scores, weights)
            )
            view.paperSize = "view.paperSize".takeValue(this.paperSize, scores, weights)
            view.title = "view.title".takeValue(this.title, scores, weights)
            view.externalSoftwareSystemBoundariesVisible = true
            view.disableAutomaticLayout()

            "view.elements".takeValue(this.elements, scores, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".takeValue(this.animations.toAnimatedElementSet(this.model), scores, weights)
                ?.let { view.completeAnimation(it) }

            "view.relationships".takeValue(this.relationships, scores, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun ComponentView.merge(
        other: ComponentView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, Container) -> Unit =
        { viewSet, container ->
            val view = viewSet.createComponentView(
                container, this.key,
                "view.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                )
            )
            view.paperSize = "view.paperSize".electValue(this.paperSize, scores, other.paperSize, weights)
            view.title = "view.title".electValue(this.title, scores, other.title, weights)
            view.setExternalSoftwareSystemBoundariesVisible(true)
            view.disableAutomaticLayout()

            "view.elements".electValue(this.elements, scores, other.elements, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".electValue(
                this.animations.notEmpty()?.toAnimatedElementSet(this.model),
                scores,
                other.animations.notEmpty()?.toAnimatedElementSet(other.model),
                weights
            )
                ?.let { view.completeAnimation(it) }

            "view.relationships".electValue(this.relationships, scores, other.relationships, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun ComponentView.add(scores: PropertyScores, weights: PropertyWeights): (ViewSet, Container) -> Unit =
        { viewSet, container ->
            val view = viewSet.createComponentView(
                container, this.key,
                "view.description".takeValue(this.description, scores, weights)
            )
            view.paperSize = "view.paperSize".takeValue(this.paperSize, scores, weights)
            view.setExternalSoftwareSystemBoundariesVisible(true)
            view.disableAutomaticLayout()

            "view.elements".takeValue(this.elements, scores, weights)
                ?.onEach { it.addToStaticView(scores, weights)(view) }

            "view.animation".takeValue(this.animations.toAnimatedElementSet(this.model), scores, weights)
                ?.let { view.completeAnimation(it) }

            "view.relationships".takeValue(this.relationships, scores, weights)
                ?.let { view.removeNotElectedRelationships(it) }

            viewsMap[view.key] = view
        }

    private fun StaticView.removeNotElectedRelationships(electedRelationships: Collection<RelationshipView>) {
        val defaultRelationships = this.relationships

        if (electedRelationships.size < defaultRelationships.size) {
            val absentRelationships = defaultRelationships.filter { it.notElected(electedRelationships) }
            absentRelationships.onEach {
                println("view:${this.key}, remove relationship view:${it.relationship.description}")
                //this.remove(it.relationship)
            }
        }
    }

    private fun RelationshipView.notElected(election: Collection<RelationshipView>): Boolean {
        return election.none {
            it.relationship.source.name == this.relationship.source.name &&
                    it.relationship.destination.name == this.relationship.destination.name
        } || election.none {
            it.description != null &&
                    it.description == this.description &&
                    it.relationship.source.name == this.relationship.source.name &&
                    it.relationship.destination.name == this.relationship.destination.name
        } || election.none {
            it.relationship.description != null &&
                    it.relationship.description == this.relationship.description &&
                    it.relationship.source.name == this.relationship.source.name &&
                    it.relationship.destination.name == this.relationship.destination.name
        }
    }

    private fun StaticView.add(container: Container, addRelationships: Boolean) =
        when (this) {
            is ContainerView -> this.add(container, addRelationships)
            is ComponentView -> this.add(container, addRelationships)
            else -> Unit
        }

    private fun StaticView.add(component: Component, addRelationships: Boolean) =
        when (this) {
            is ComponentView -> this.add(component, addRelationships)
            else -> Unit
        }

    private fun DynamicView.merge(
        other: DynamicView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, StaticStructureElement) -> Unit =
        { viewSet, scope ->
            val view = when (scope) {
                is Container ->
                    viewSet.createDynamicView(
                        scope,
                        this.key,
                        "view.description".electValue(this.description, scores, other.description, weights)
                    )
                is SoftwareSystem ->
                    viewSet.createDynamicView(
                        scope,
                        this.key,
                        "view.description".electValue(this.description, scores, other.description, weights)
                    )
                else ->
                    viewSet.createDynamicView(
                        this.key,
                        "view.description".electValue(this.description, scores, other.description, weights)
                    )
            }
            view.paperSize = "view.paperSize".electValue(this.paperSize, scores, other.paperSize, weights)
            view.title = "view.title".electValue(this.title, scores, other.title, weights)
            view.disableAutomaticLayout()
            view.externalBoundariesVisible = true

            "view.dynamic.relationships".electValue(this.relationships, scores, other.relationships, weights)
                ?.onEach { it.add(scores, weights)(view) }

            viewsMap[view.key] = view
        }

    private fun DynamicView.add(
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, StaticStructureElement) -> Unit =
        { viewSet, scope ->
            val view = when (scope) {
                is Container ->
                    viewSet.createDynamicView(
                        scope,
                        this.key,
                        "view.description".takeValue(this.description, scores, weights)
                    )
                is SoftwareSystem ->
                    viewSet.createDynamicView(
                        scope,
                        this.key,
                        "view.description".takeValue(this.description, scores, weights)
                    )
                else ->
                    viewSet.createDynamicView(
                        this.key,
                        "view.description".takeValue(this.description, scores, weights)
                    )
            }
            view.paperSize = "view.paperSize".takeValue(this.paperSize, scores, weights)
            view.title = "view.title".takeValue(this.title, scores, weights)
            view.disableAutomaticLayout()
            view.externalBoundariesVisible = true

            "view.dynamic.relationships".takeValue(this.relationships, scores, weights)
                ?.onEach { it.add(scores, weights)(view) }

            viewsMap[view.key] = view
        }

    private fun DeploymentView.merge(
        other: DeploymentView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, SoftwareSystem) -> Unit =
        { viewSet, softwareSystem ->
            val view = viewSet.createDeploymentView(
                softwareSystem, this.key,
                "view.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                )
            )
            view.paperSize = "view.paperSize".electValue(this.paperSize, scores, other.paperSize, weights)
            view.title = "view.title".electValue(this.title, scores, other.title, weights)
            view.environment = "view.environment".electValue(this.environment, scores, other.environment, weights)
            view.disableAutomaticLayout()

            val masterLeafElements = this.elements
                .filter { it.element is DeploymentNode && (it.element as DeploymentNode).hasChildren().not() }
                .toSet()

            val otherLeafElements = other.elements
                .filter { it.element is DeploymentNode && (it.element as DeploymentNode).hasChildren().not() }
                .toSet()

            "view.elements".electValue(this.elements, scores, other.elements, weights)
                ?.onEach { it.addToDeploymentView(scores, weights)(view) }

            viewsMap[view.key] = view
        }

    private fun DeploymentView.add(
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet, SoftwareSystem) -> Unit =
        { viewSet, softwareSystem ->
            val view = viewSet.createDeploymentView(
                softwareSystem, this.key,
                "view.description".takeValue(this.description, scores, weights)
            )
            view.paperSize = "view.paperSize".takeValue(this.paperSize, scores, weights)
            view.title = "view.title".takeValue(this.title, scores, weights)
            view.environment = "view.environment".takeValue(this.environment, scores, weights)
            view.disableAutomaticLayout()

            val leafElements = this.elements
                .filter { it.element is DeploymentNode && (it.element as DeploymentNode).hasChildren().not() }

            val leafElementNodes = leafElements.map { it.element as DeploymentNode }

            val elementsWithInfrastructureNode = this.elements
                .filter { it.element is DeploymentNode && (it.element as DeploymentNode).infrastructureNodes.isNotEmpty() }

            "view.elements".takeValue(leafElements, scores, weights)
                ?.onEach { it.addToDeploymentView(scores, weights)(view) }

            elementsWithInfrastructureNode.onEach { it.addToDeploymentView(scores, weights)(view) }

            val allExpectedViewNodeNames = leafElementNodes
                .flatMap { leafToRootNodePaths[it.name] ?: listOf() }
                .map { "${view.environment}-${this.name}" }

            val allViewNodeNames = view.elements
                .map { "${view.environment}-${it.element.name}" }

            (allViewNodeNames - allExpectedViewNodeNames)
                .mapNotNull { nodesMap[it] }
                .onEach { view.remove(it) }

            viewsMap[view.key] = view
        }

    private fun FilteredView.merge(
        other: FilteredView,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (ViewSet) -> Unit =
        { viewSet ->
            viewSet.createFilteredView(
                viewsMap[this.baseViewKey] as StaticView,
                this.key,
                "view.description".electValue(this.description, scores, other.description, weights),
                this.mode,
                *"view.tags".electSet(this.tags, scores, other.tags, weights)!!.toTypedArray()
            )
        }

    private fun FilteredView.add(scores: PropertyScores, weights: PropertyWeights): (ViewSet) -> Unit =
        { viewSet ->
            viewSet.createFilteredView(
                viewsMap[this.baseViewKey] as StaticView,
                this.key,
                "view.description".takeValue(this.description, scores, weights),
                this.mode,
                *"view.tags".takeValue(this.tags, scores, weights)!!.toTypedArray()
            )
        }

    private fun ElementView.addToStaticView(scores: PropertyScores, weights: PropertyWeights): Promotion<StaticView> =
        { view ->
            elementsMap[this.element.name]?.let { elt ->
                runCatching {
                    when (elt) {
                        is Person -> view.add(elt, true)
                        is SoftwareSystem -> view.add(elt, true)
                        is Container -> view.add(elt, true)
                        is Component -> view.add(elt, true)
                        else -> Unit
                    }
                }.onFailure {
                    logger.info(it.message)
                }
                view.elements.firstOrNull { eltView -> elementViewComparator(eltView, this) }
                    ?.let { eltView ->
                        eltView.x = "view.element.position".takeValue(this.x, scores, weights) ?: this.x
                        eltView.y = "view.element.position".takeValue(this.y, scores, weights) ?: this.y
                    }
            }
        }

private fun ElementView.addToDeploymentView(
    scores: PropertyScores,
    weights: PropertyWeights
): Promotion<DeploymentView> =
    { view ->
        nodesMap["${view.environment}-${this.element.name}"]
            ?.let { node ->
                view.add(node, true)
                view.elements.firstOrNull { elementViewComparator(it, this) }
                    ?.let {
                        it.x = "view.element.position".takeValue(this.x, scores, weights) ?: this.x
                        it.y = "view.element.position".takeValue(this.y, scores, weights) ?: this.y
                    }
            }
    }

private fun RelationshipView.add(scores: PropertyScores, weights: PropertyWeights): (DynamicView) -> Unit =
    {
        elementFinder(this.relationship.source.name)?.let { source ->
            elementFinder(this.relationship.destination.name)?.let { destination ->
                val relationShipView = it.add(
                    source,
                    "view.relationship.description".takeValue(this.description, scores, weights),
                    destination
                )
                relationShipView.routing = "view.relationship.routing".takeValue(this.routing, scores, weights)
                relationShipView.vertices = "view.relationship.vertices".takeValue(this.vertices, scores, weights)
            }
        }
    }

private fun RelationshipView.merge(
    other: RelationshipView,
    scores: PropertyScores,
    weights: PropertyWeights
): (DynamicView) -> Unit =
    {
        elementFinder(this.relationship.source.name)?.let { source ->
            elementFinder(this.relationship.destination.name)?.let { destination ->
                val relationShipView = it.add(
                    source,
                    "view.relationship.description".electValue(
                        this.description,
                        scores,
                        other.description,
                        weights
                    ),
                    destination
                )
                relationShipView.routing =
                    "view.relationship.routing".electValue(this.routing, scores, other.routing, weights)
                relationShipView.vertices =
                    "view.relationship.vertices".electValue(this.vertices, scores, other.vertices, weights)
            }
        }
    }

private fun <C : Element> ((ViewSet, C) -> Unit).promote(elementName: String): Promotion<ViewSet> =
    { viewSet: ViewSet -> this(viewSet, elementsMap.getValue(elementName) as C) }

private fun Collection<Animation>.toAnimatedElementSet(model: Model): List<Set<Element>> =
    this.map { a -> a.elements.mapNotNull { model.getElement(it) }.toSet() }
}
