package edu.obya.c4.domain.structurizr.util

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.C4Tags
import edu.obya.c4.domain.strategy.NameCanonizer
import edu.obya.c4.util.normalize
import com.structurizr.Workspace
import com.structurizr.model.*
import com.structurizr.view.ComponentView
import com.structurizr.view.ContainerView
import com.structurizr.view.View
import com.structurizr.view.ViewSet

fun C4Model<Long, Workspace>.allSystems(): Set<SoftwareSystem> = this.state.model.softwareSystems
fun C4Model<Long, Workspace>.allDevelopmentTeams() = this.state.model.people
    .filter { it.hasTag(C4Tags.Organization.DEVELOPMENT_TEAM) }

fun SoftwareSystem.allContainers(vararg tags: String) = this.containers
    .filter { container -> tags.any { container.hasTag(it) } }

fun SoftwareSystem.allMicroservices() = this.allContainers(C4Tags.Architecture.MICRO_SERVICE)
fun SoftwareSystem.allBrokers() = this.allContainers(C4Tags.Infrastructure.BROKER)

fun ViewSet.hasByKey(key: String): Boolean = this.views.any { it.key == key }

fun <T : Element> Collection<T>.byId() = this.associateBy { it.id }
fun <T : Element> Collection<T>.byName() = this.associateBy { it.name }
fun <T : Element> Collection<T>.byTag(tag: String) = this
    .flatMap { relationship -> relationship.tagsAsSet.map { tag -> tag to relationship } }
    .groupBy { it.first }

fun <T : Element> Collection<T>.atName(name: String) = this.firstOrNull { it.name == name }

fun Collection<Element>.allStatic() = this.filterIsInstance<StaticStructureElement>()
fun Collection<Element>.allContainers() = this.filterIsInstance<Container>()
fun Collection<Element>.allComponents() = this.filterIsInstance<Component>()
fun <T : Element> Collection<T>.havingTag(tag: String) = this.filter { it.hasTag(tag) }

fun DeploymentNode.leafNodes(): List<DeploymentNode> =
    if (hasChildren()) this.children.flatMap { it.leafNodes() }
    else listOf(this)

fun DeploymentNode.allNodes(): List<DeploymentNode> =
    if (hasChildren()) listOf(this) + this.children.flatMap { it.allNodes() }
    else listOf(this)

fun DeploymentNode.pathToRoot(): List<DeploymentNode> =
    if (this.parent != null) listOf(this) + (this.parent as DeploymentNode).pathToRoot()
    else listOf(this)

fun Collection<DeploymentNode>.allNodes() = this.flatMap { it.allNodes() }
fun Collection<DeploymentNode>.leafNodes() = this.flatMap { it.leafNodes() }
fun Collection<DeploymentNode>.byNameWithEnvironment() = this.associateBy { "${it.environment}-${it.name}" }
fun Collection<DeploymentNode>.pathToRoot() = this.associateBy({ it.name }, { it.pathToRoot() })
fun Collection<DeploymentNode>.byContainerId() = this
    .flatMap { node -> node.containerInstances.map { instance -> instance.containerId to node } }
    .toMap()

fun Component.isEventQueue() = name.isEvent()
fun Component.isCommandQueue() = name.isCommand()
fun Component.eventQueuePublisher() = name.substringBefore('.')
fun Component.eventQueueListener() = name.substringAfterLast('.')
fun Component.commandQueuePublisher() = name.substringAfterLast('.')
fun Component.commandQueueListener() = name.substringBefore('.')
fun Component.event() = name.substringBeforeLast('.').substringAfter("event.")
fun Component.command() = name.substringBeforeLast('.').substringAfter("command.")

fun Relationship.isEvent() = when {
    this.hasTag(C4Tags.Architecture.EVENT) -> true
    this.tags.isEvent() -> true
    this.destination is Component && (this.destination as Component).isEventQueue() -> true
    else -> false
}

fun Relationship.isCommand() = when {
    this.hasTag(C4Tags.Architecture.COMMAND) -> true
    this.tags.isCommand() -> true
    this.destination is Component && (this.destination as Component).isCommandQueue() -> true
    else -> false
}

fun Element.isEventPublisherTo(queue: Component, nameCanonizer: NameCanonizer) =
    this.name == nameCanonizer(queue.eventQueuePublisher())

fun Element.isEventListenerTo(queue: Component, nameCanonizer: NameCanonizer) =
    this.name == nameCanonizer(queue.eventQueueListener())

fun Element.isCommandPublisherTo(queue: Component, nameCanonizer: NameCanonizer) =
    this.name == nameCanonizer(queue.commandQueuePublisher())

fun Element.isCommandListenerTo(queue: Component, nameCanonizer: NameCanonizer) =
    this.name == nameCanonizer(queue.commandQueueListener())

fun Element.ownedBy(team: Person) = this.tags.contains(team.name.normalize())

fun String.isEvent() = contains(".event.")
fun String.isCommand() = contains(".command.")

fun Relationship.ownedBy(team: Person) = this.source.ownedBy(team) || this.destination.ownedBy(team)
fun Relationship.connects(sourceName: String, destinationName: String): Boolean =
    this.source.name == sourceName && this.destination.name == destinationName

fun Collection<Relationship>.between(sourceName: String, destinationName: String): List<Relationship> =
    this.filter { it.source.name == sourceName && it.destination.name == destinationName }

fun Collection<Relationship>.anyBetween(sourceName: String, destinationName: String): Boolean =
    this.any { it.connects(sourceName, destinationName) }

fun Collection<Relationship>.byTag() = this
    .flatMap { relationship -> relationship.tagsAsSet.map { tag -> tag to relationship } }
    .groupBy({ it.first }, { it.second })

fun Collection<Relationship>.byTagTo() = this
    .flatMap { relationship -> relationship.tagsAsSet.map { tag -> tag to relationship } }
    .groupByTo(mutableMapOf(), { it.first }, { it.second })

fun ViewSet.containerViewAt(key: String): ContainerView? = this.at(key)
fun ViewSet.componentViewAt(key: String): ComponentView? = this.at(key)
inline fun <reified T: View> ViewSet.at(key: String): T? =
    this.views.filterIsInstance<T>().firstOrNull { it.key == key }

fun ComponentView.hasElement(element: Element): Boolean
    = this.elements.any { it.element == element }
