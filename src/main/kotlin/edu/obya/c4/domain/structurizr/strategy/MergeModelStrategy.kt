package edu.obya.c4.domain.structurizr.strategy

import edu.obya.c4.domain.C4Tags.purge
import edu.obya.c4.domain.strategy.*
import edu.obya.c4.util.JOCKER
import edu.obya.c4.util.emptyIf
import edu.obya.c4.util.notEmpty
import edu.obya.c4.util.notNull
import com.structurizr.model.*

class MergeModelStrategy(
    val nameComparator: NameComparator,
    val nameCanonizer: NameCanonizer
) {
    private val elementComparator = elementComparator(nameComparator)

    fun execute(
        master: Model,
        other: Model,
        scores: PropertyScores,
        weights: PropertyWeights
    ): List<(Model) -> Unit> {

        val people = "model.people".promoteValues<Person, Model>(
            master.people, scores, other.people, weights, elementComparator,
            { it.add(scores, weights) },
            { a, b -> listOf(a.merge(b, scores, weights)) }
        )
        val systems = "model.systems".promoteValues<SoftwareSystem, Model>(
            master.softwareSystems, scores, other.softwareSystems, weights, elementComparator,
            { it.add(scores, weights) },
            { a, b -> listOf(a.merge(b, scores, weights)) }
        )
        val enterprise = { _: Model ->
            //master.enterprise?.name?.emptyIf(JOCKER)?.map { master.enterprise } ?: other.enterprise
        }
        val deploymentNodes = "model.deploymentNodes".promoteValues<DeploymentNode, Model>(
            master.deploymentNodes, scores, other.deploymentNodes, weights, elementComparator,
            { it.add(scores, weights) },
            { a, b -> listOf(a.merge(b, scores, weights)) }
        )
        return people + systems + enterprise + deploymentNodes
    }

    private fun Person.merge(other: Person, scores: PropertyScores, weights: PropertyWeights): Promotion<Model> =
        { model ->
            val person = model.addPerson(
                "person.name".electValue(this.name?.notEmpty(), scores, other.name?.notEmpty(), weights)!!
                    .canonize(nameCanonizer),
                "person.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                )
            )
            person.url = "person.url".electValue(this.url?.notEmpty(), scores, other.url?.notEmpty(), weights).notNull()
            person.addTags(
                *"person.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!.purge().toTypedArray()
            )
            "person.properties".electValue(this.properties, scores, other.properties, weights)
                ?.onEach { person.addProperty(it.key, it.value) }
        }

    private fun Person.add(scores: PropertyScores, weights: PropertyWeights): Promotion<Model> =
        { model ->
            val person = model.addPerson(
                "person.name".takeValue(this.name, scores, weights)!!.canonize(nameCanonizer),
                "person.description".takeValue(this.description, scores, weights)
            )
            person.url = "person.url".takeValue(this.url, scores, weights).notNull()
            person.addTags(*"person.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray())

            "person.properties".takeValue(this.properties, scores, weights)
                ?.onEach { person.addProperty(it.key, it.value) }
        }

    private fun SoftwareSystem.merge(
        other: SoftwareSystem,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<Model> =
        { model ->
            val system = model.addSoftwareSystem(
                "system.name".electValue(this.name?.notEmpty(), scores, other.name?.notEmpty(), weights)!!
                    .canonize(nameCanonizer),
                "system.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                )
            )
            system.url = "system.url".electValue(this.url?.notEmpty(), scores, other.url?.notEmpty(), weights).notNull()
            system.addTags(
                *"system.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!.purge().toTypedArray()
            )

            "system.properties".electValue(this.properties, scores, other.properties, weights)
                ?.onEach { system.addProperty(it.key, it.value) }
            "system.containers".promoteValues<Container, SoftwareSystem>(
                this.containers, scores, other.containers, weights, elementComparator,
                { it.add(scores, weights) },
                { a, b -> listOf(a.merge(b, scores, weights)) }
            ).onEach { it(system) }
        }

    private fun SoftwareSystem.add(scores: PropertyScores, weights: PropertyWeights): Promotion<Model> =
        { model ->
            val system = model.addSoftwareSystem(
                "system.name".takeValue(this.name, scores, weights)!!.canonize(nameCanonizer),
                "system.description".takeValue(this.description, scores, weights)
            )
            system.url = "system.url".takeValue(this.url, scores, weights).notNull()
            system.addTags(*"system.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray())

            "system.properties".takeValue(this.properties, scores, weights)
                ?.onEach { system.addProperty(it.key, it.value) }
            "system.containers".takeValue(this.containers, scores, weights)
                ?.map { it.add(scores, weights) }?.onEach { it(system) }
        }

    private fun Container.merge(
        other: Container,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<SoftwareSystem> =
        { system ->
            val container = system.addContainer(
                "container.name".electValue(this.name?.notEmpty(), scores, other.name?.notEmpty(), weights)!!
                    .canonize(nameCanonizer),
                "container.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                ),
                "container.technology".electValue(
                    this.technology?.notEmpty(),
                    scores,
                    other.technology?.notEmpty(),
                    weights
                )
            )
            container.url =
                "container.url".electValue(this.url?.notEmpty(), scores, other.url?.notEmpty(), weights).notNull()
            container.addTags(
                *"container.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!.purge().toTypedArray()
            )
            "container.properties".electValue(this.properties, scores, other.properties, weights)
                ?.onEach { container.addProperty(it.key, it.value) }
            "container.components".promoteValues<Component, Container>(
                this.components, scores, other.components, weights, elementComparator,
                { it.add(scores, weights) },
                { a, b -> listOf(a.merge(b, scores, weights)) }
            ).onEach { it(container) }
        }

    private fun Container.add(scores: PropertyScores, weights: PropertyWeights): Promotion<SoftwareSystem> =
        { system ->
            val container = system.addContainer(
                "container.name".takeValue(this.name, scores, weights)!!.canonize(nameCanonizer),
                "container.description".takeValue(this.description, scores, weights),
                "container.technology".takeValue(this.technology, scores, weights)
            )
            container.url = "container.url".takeValue(this.url, scores, weights).notNull()
            container.addTags(*"container.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray())

            "container.properties".takeValue(this.properties, scores, weights)
                ?.onEach { container.addProperty(it.key, it.value) }
            "container.components".takeValue(this.components, scores, weights)
                ?.map { it.add(scores, weights) }?.onEach { it(container) }
        }

    private fun Component.merge(
        other: Component,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<Container> =
        { container ->
            val component = container.addComponent(
                "component.name".electValue(this.name?.notEmpty(), scores, other.name?.notEmpty(), weights)!!
                    .canonize(nameCanonizer),
                "component.description".electValue(
                    this.description?.notEmpty(),
                    scores,
                    other.description?.notEmpty(),
                    weights
                ).notNull(),
                "component.technology".electValue(
                    this.technology?.notEmpty(),
                    scores,
                    other.technology?.notEmpty(),
                    weights
                ).notNull()
            )
            component.url =
                "component.url".electValue(this.url?.notEmpty(), scores, other.url?.notEmpty(), weights).notNull()
            component.addTags(
                *"component.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!.purge().toTypedArray()
            )
            "component.properties".electValue(this.properties, scores, other.properties, weights)
                ?.onEach { component.addProperty(it.key, it.value) }
        }

    private fun Component.add(scores: PropertyScores, weights: PropertyWeights): Promotion<Container> =
        { container ->
            val component = container.addComponent(
                "component.name".takeValue(this.name, scores, weights)!!.canonize(nameCanonizer),
                "component.description".takeValue(this.description, scores, weights),
                "component.technology".takeValue(this.technology, scores, weights)
            )
            component.url = "component.url".takeValue(this.url, scores, weights).notNull()
            component.addTags(*"component.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray())

            "component.properties".takeValue(this.properties, scores, weights)
                ?.onEach { component.addProperty(it.key, it.value) }
        }

    private fun DeploymentNode.merge(
        other: DeploymentNode,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<Model> =
        { model ->
            val node = model.addDeploymentNode(
                "node.environment".takeValue(this.environment, scores, weights),
                "node.name".electValue(this.name, scores, other.name, weights)!!.canonize(nameCanonizer),
                "node.description".electValue(this.description, scores, other.description, weights),
                "node.technology".electValue(this.technology, scores, other.technology, weights),
                "node.instances".electValue(this.instances, scores, other.instances, weights)?.toInt() ?: 1,
                "node.properties".electValue(this.properties, scores, other.properties, weights)
            )
            finalizeMerge(node, other, scores, weights)
        }

    private fun DeploymentNode.mergeToParent(
        other: DeploymentNode,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<DeploymentNode> =
        { parent ->
            val node = parent.addDeploymentNode(
                "node.name".electValue(this.name, scores, other.name, weights)!!.canonize(nameCanonizer),
                "node.description".electValue(this.description, scores, other.description, weights),
                "node.technology".electValue(this.technology, scores, other.technology, weights),
                "node.instances".electValue(this.instances, scores, other.instances, weights)?.toInt() ?: 1,
                "node.properties".electValue(this.properties, scores, other.properties, weights)
            )
            finalizeMerge(node, other, scores, weights)
        }

    private fun DeploymentNode.finalizeMerge(
        incubatedNode: DeploymentNode,
        other: DeploymentNode,
        scores: PropertyScores,
        weights: PropertyWeights
    ) {
        incubatedNode.url = "node.url".electValue(this.url, scores, other.url, weights).notNull()
        incubatedNode.addTags(
            *"node.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!.purge().toTypedArray()
        )

        "node.properties".electValue(this.properties, scores, other.properties, weights)
            ?.onEach { incubatedNode.addProperty(it.key, it.value) }

        "node.children".promoteValues<DeploymentNode, DeploymentNode>(
            this.children, scores, other.children, weights, elementComparator,
            { it.addToParent(scores, weights) },
            { a, b -> listOf(a.mergeToParent(b, scores, weights)) }
        ).onEach { it(incubatedNode) }

        "node.infrastructureNodes".promoteValues<InfrastructureNode, DeploymentNode>(
            this.infrastructureNodes, scores, other.infrastructureNodes, weights, elementComparator,
            { it.addToParent(scores, weights) },
            { a, b -> listOf(a.mergeToParent(b, scores, weights)) }
        ).onEach { it(incubatedNode) }
    }

    private fun DeploymentNode.add(scores: PropertyScores, weights: PropertyWeights): Promotion<Model> =
        { model ->
            val node = model.addDeploymentNode(
                "node.environment".takeValue(this.environment, scores, weights),
                "node.name".takeValue(this.name, scores, weights)!!.canonize(nameCanonizer),
                "node.description".takeValue(this.description, scores, weights),
                "node.technology".takeValue(this.technology, scores, weights),
                "node.instances".takeValue(this.instances, scores, weights)?.toInt() ?: 1,
                "node.properties".takeValue(this.properties, scores, weights)
            )
            this.finalizeAdd(node, scores, weights)
        }

    private fun DeploymentNode.addToParent(
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<DeploymentNode> =
        { parent ->
            val node = parent.addDeploymentNode(
                "node.name".takeValue(this.name, scores, weights)!!.canonize(nameCanonizer),
                "node.description".takeValue(this.description, scores, weights),
                "node.technology".takeValue(this.technology, scores, weights),
                "node.instances".takeValue(this.instances, scores, weights)?.toInt() ?: 1,
                "node.properties".takeValue(this.properties, scores, weights)
            )
            this.finalizeAdd(node, scores, weights)
        }

    private fun DeploymentNode.finalizeAdd(
        incubatedNode: DeploymentNode,
        scores: PropertyScores,
        weights: PropertyWeights
    ) {
        incubatedNode.url = "node.url".takeValue(this.url, scores, weights).notNull()
        incubatedNode.addTags(*"node.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray())

        "node.properties".takeValue(this.properties, scores, weights)
            ?.onEach { incubatedNode.addProperty(it.key, it.value) }
        "node.children".takeValue(this.children, scores, weights)
            ?.onEach { it.addToParent(scores, weights)(incubatedNode) }
        "node.infrastructureNodes".takeValue(this.infrastructureNodes, scores, weights)
            ?.onEach { it.addToParent(scores, weights)(incubatedNode) }
    }

    private fun InfrastructureNode.mergeToParent(
        other: InfrastructureNode,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<DeploymentNode> =
        { parent: DeploymentNode ->
            val node = parent.addInfrastructureNode(
                "node.name".electValue(this.name, scores, other.name, weights)!!.canonize(nameCanonizer),
                "node.description".electValue(this.description, scores, other.description, weights),
                "node.technology".electValue(this.technology, scores, other.technology, weights),
                "node.properties".electValue(this.properties, scores, other.properties, weights)
            )
            node.url = "node.url".electValue(this.url, scores, other.url, weights).notNull()
            node.addTags(
                *"node.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!.purge().toTypedArray()
            )
            "node.properties".electValue(this.properties, scores, other.properties, weights)
                ?.onEach { node.addProperty(it.key, it.value) }
        }

    private fun InfrastructureNode.addToParent(
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<DeploymentNode> =
        { parent: DeploymentNode ->
            val node = parent.addInfrastructureNode(
                "node.name".takeValue(this.name, scores, weights),
                "node.description".takeValue(this.description, scores, weights),
                "node.technology".takeValue(this.technology, scores, weights),
                "node.properties".takeValue(this.properties, scores, weights)
            )
            node.url = "node.url".takeValue(this.url, scores, weights).notNull()
            node.addTags(*"node.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray())

            "node.properties".takeValue(this.properties, scores, weights)
                ?.onEach { node.addProperty(it.key, it.value) }
        }
}
