package com.edgelab.c4.domain.structurizr.enricher

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Tags
import com.edgelab.c4.domain.enricher.C4ModelEnricher
import com.edgelab.c4.domain.strategy.NameCanonizer
import com.edgelab.c4.domain.structurizr.util.*
import com.edgelab.c4.util.logger
import com.structurizr.Workspace
import com.structurizr.model.*

object C4SyncRelationshipsTagger : C4ModelEnricher<Long, Workspace> {

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            val brokers = this.model.elements
                .havingTag(C4Tags.Infrastructure.BROKER)
                .map { it as Container }
            val relationships = this.model.relationships
            relationships.tagSynchronous(brokers)
            relationships.tagAsynchronous(brokers)
        }
    }

    private fun Collection<Relationship>.tagSynchronous(brokers: List<Container>) {
        this.filter { it.hasTag(Tags.ASYNCHRONOUS).not() && brokers.contains(it.destination).not() }
            .onEach { it.addTags(Tags.SYNCHRONOUS) }
    }

    private fun Collection<Relationship>.tagAsynchronous(brokers: List<Container>) {
        this.filter { brokers.contains(it.destination) }
            .onEach {
                if (it.hasTag(Tags.ASYNCHRONOUS).not()) it.addTags(Tags.ASYNCHRONOUS)
                if (it.isEvent()) {
                    if (it.hasTag(C4Tags.Architecture.EVENT).not()) it.addTags(C4Tags.Architecture.EVENT)
                } else if (it.isCommand()) {
                    if (it.hasTag(C4Tags.Architecture.COMMAND).not()) it.addTags(C4Tags.Architecture.COMMAND)
                }
            }
    }
}

class C4AsyncRelationshipsInferrer(private val nameCanonizer: NameCanonizer) : C4ModelEnricher<Long, Workspace> {
    private val logger = this.javaClass.logger()

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            val queues = this.model.elements.allComponents().havingTag(C4Tags.Infrastructure.QUEUE)
            val containersByName = this.model.elements.allContainers().byName()
            val relationshipsByTag = this.model.relationships.byTagTo()

            queues.onEach { queue ->
                if (queue.isEventQueue()) {
                    inferEventListeningRelationship(queue, containersByName, relationshipsByTag)
                    inferEventListeningRelationships(queue, containersByName, relationshipsByTag)
                } else if (queue.isCommandQueue()) {
                    inferCommandPublishingRelationship(queue, containersByName, relationshipsByTag)
                    inferCommandPublishingRelationships(queue, containersByName, relationshipsByTag)
                }
            }
        }
    }

    private fun inferEventListeningRelationship(
        queue: Component,
        containersByName: Map<String, Container>,
        relationshipsByTag: MutableMap<String, MutableList<Relationship>>
    ) {
        logger.debug("investigate event queue ${queue.name} - stage 1")

        containersByName[nameCanonizer(queue.eventQueuePublisher())]?.let { publisher ->
            logger.debug("identified related publisher ${publisher.name}")

            containersByName[nameCanonizer(queue.eventQueueListener())]?.let { listener ->
                logger.debug("identified related listener ${listener.name}")

                if (relationshipsByTag.allBy(listener.name, publisher.name, queue.name).isEmpty()) {
                    listener.uses(
                        publisher,
                        "listens to ${queue.event()} events",
                        queue.technology,
                        InteractionStyle.Asynchronous
                    )?.let {
                        it.addTags(queue.name, C4Tags.Architecture.EVENT)
                        relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                        logger.info("inferred relationship: $it")
                    }
                } else {
                    logger.debug("event relationship ${listener.name} -listens to-> ${publisher.name} already exists")
                }
            }
        }
        containersByName[nameCanonizer(queue.eventQueueListener())]?.let { listener ->
            logger.debug("identified related listener ${listener.name}")

            if (relationshipsByTag.allBy(listener.name, queue.name, queue.name).isEmpty()) {
                listener.uses(
                    queue,
                    "listens to ${queue.event()} events",
                    queue.technology,
                    InteractionStyle.Asynchronous
                )?.let {
                    it.addTags(queue.name, C4Tags.Architecture.EVENT)
                    relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                    logger.info("inferred relationship: $it")
                }
            } else {
                logger.debug("event relationship ${listener.name} -listens to-> ${queue.name} already exists")
            }
        }
    }

    private fun inferEventListeningRelationships(
        queue: Component,
        containersByName: Map<String, Container>,
        relationshipsByTag: MutableMap<String, MutableList<Relationship>>
    ) {
        logger.debug("investigate event queue ${queue.name} - stage 2")

        val eventListeners = mutableListOf<Pair<Element, Relationship>>()
        var eventPublisher: Element? = null

        relationshipsByTag.allByTag(queue.name).onEach {
            logger.debug("investigate event relationship $it")

            if (it.destination.hasTag(C4Tags.Infrastructure.BROKER)) {

                containersByName[nameCanonizer(queue.eventQueuePublisher())]?.let { publisher ->
                    eventPublisher = publisher
                    logger.debug("1. identified related publisher ${publisher.name}")
                }
                containersByName[nameCanonizer(queue.eventQueueListener())]?.let { listener ->
                    eventListeners.add(listener to it)
                    logger.debug("2. identified related listener ${listener.name}")
                }
            }
        }

        logger.debug("event publisher is ${eventPublisher?.name} and listeners are ${eventListeners.map { it.first.name }}")

        eventPublisher?.let { publisher ->
            eventListeners
                .distinctBy { it.first }
                .onEach { (listener, relationship) ->
                    if (listener is Container && publisher is Container) {
                        if (relationshipsByTag.allBy(listener.name, publisher.name, queue.name).isEmpty()) {
                            listener.uses(
                                publisher,
                                relationship.descriptionOrDefault("listens to ${queue.event()} events"),
                                queue.technology,
                                InteractionStyle.Asynchronous
                            )?.let {
                                it.addTags(queue.name, C4Tags.Architecture.EVENT)
                                relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                                logger.info("inferred relationship: $it")
                            }
                        } else {
                            logger.debug("event relationship ${listener.name} -listens to-> ${publisher.name} already exists")
                        }
                        if (relationshipsByTag.allBy(listener.name, queue.name, queue.name).isEmpty()) {
                            listener.uses(
                                queue,
                                relationship.descriptionOrDefault("listens to ${queue.event()} events"),
                                queue.technology,
                                InteractionStyle.Asynchronous
                            )?.let {
                                it.addTags(queue.name, C4Tags.Architecture.EVENT)
                                relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                                logger.info("inferred relationship: $it")
                            }
                        } else {
                            logger.debug("event relationship ${listener.name} -listens to-> ${queue.name} already exists")
                        }
                    }
                }
        }
    }

    private fun inferCommandPublishingRelationship(
        queue: Component,
        containersByName: Map<String, Container>,
        relationshipsByTag: MutableMap<String, MutableList<Relationship>>
    ) {
        logger.debug("investigate command queue ${queue.name} - stage 1")

        containersByName[nameCanonizer(queue.commandQueueListener())]?.let { listener ->
            logger.debug("identified related listener ${listener.name}")

            containersByName[nameCanonizer(queue.commandQueuePublisher())]?.let { publisher ->
                logger.debug("identified related publisher ${publisher.name}")

                if (relationshipsByTag.allBy(publisher.name, listener.name, queue.name).isEmpty()) {
                    publisher.uses(
                        listener,
                        "publishes ${queue.command()} commands",
                        queue.technology,
                        InteractionStyle.Asynchronous
                    )?.let {
                        it.addTags(queue.name, C4Tags.Architecture.COMMAND)
                        relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                        logger.info("inferred relationship: $it")
                    }
                } else {
                    logger.debug("command relationship ${publisher.name} -sends to-> ${listener.name} already exists")
                }
            }
        }
        containersByName[nameCanonizer(queue.commandQueuePublisher())]?.let { publisher ->
            logger.debug("identified related publisher ${publisher.name}")

            if (relationshipsByTag.allBy(publisher.name, queue.name, queue.name).isEmpty()) {
                publisher.uses(
                    queue,
                    "publishes ${queue.command()} commands",
                    queue.technology,
                    InteractionStyle.Asynchronous
                )?.let {
                    it.addTags(queue.name, C4Tags.Architecture.COMMAND)
                    relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                    logger.info("inferred relationship: $it")
                }
            } else {
                logger.debug("event relationship ${publisher.name} -sends to-> ${queue.name} already exists")
            }
        }
    }

    private fun inferCommandPublishingRelationships(
        queue: Component,
        containersByName: Map<String, Container>,
        relationshipsByTag: MutableMap<String, MutableList<Relationship>>
    ) {
        logger.debug("investigate command queue ${queue.name} - stage 2")

        val commandPublishers = mutableListOf<Pair<Element, Relationship>>()
        var commandListener: Element? = null

        relationshipsByTag.allByTag(queue.name).onEach {
            logger.debug("investigate command relationship $it")

            if (it.destination.hasTag(C4Tags.Infrastructure.BROKER)) {

                containersByName[nameCanonizer(queue.commandQueueListener())]?.let { listener ->
                    commandListener = listener
                    logger.debug("1. identified related listener ${listener.name}")
                }
                containersByName[nameCanonizer(queue.commandQueuePublisher())]?.let { publisher ->
                    commandPublishers.add(publisher to it)
                    logger.debug("2. identified related publisher ${publisher.name}")
                }
            }
        }

        logger.debug("command listener is ${commandListener?.name} and publishers are ${commandPublishers.map { it.first.name }}")

        commandListener?.let { listener ->
            commandPublishers
                .distinctBy { it.first }
                .onEach { (publisher, relationship) ->
                    if (listener is Container && publisher is Container) {
                        if (relationshipsByTag.allBy(publisher.name, listener.name, queue.name).isEmpty()) {
                            publisher.uses(
                                listener,
                                relationship.descriptionOrDefault("publishes ${queue.command()} commands"),
                                queue.technology,
                                InteractionStyle.Asynchronous
                            )?.let {
                                it.addTags(queue.name, C4Tags.Architecture.COMMAND)
                                relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                                logger.info("inferred relationship: $it")
                            }
                        } else {
                            logger.debug("command relationship ${publisher.name} -sends to-> ${listener.name} already exists")
                        }
                        if (relationshipsByTag.allBy(publisher.name, queue.name, queue.name).isEmpty()) {
                            publisher.uses(
                                queue,
                                relationship.descriptionOrDefault("publishes ${queue.command()} commands"),
                                queue.technology,
                                InteractionStyle.Asynchronous
                            )?.let {
                                it.addTags(queue.name, C4Tags.Architecture.COMMAND)
                                relationshipsByTag.getOrPut(queue.name, { mutableListOf() }).add(it)
                                logger.info("inferred relationship: $it")
                            }
                        } else {
                            logger.debug("event relationship ${publisher.name} -sends to-> ${queue.name} already exists")
                        }
                    }
                }
        }
    }

    private fun Relationship.descriptionOrDefault(default: String) =
        this.description.takeIf { it.isEmpty() } ?: default

    private fun Map<String, List<Relationship>>.allByTag(tag: String): List<Relationship> =
        this.filterKeys { it == tag || tag.contains(it) || it.contains(tag) }
            .flatMap { it.value }

    private fun Map<String, List<Relationship>>.allBy(
        sourceName: String,
        destinationName: String,
        tag: String
    ): List<Relationship> = this.allByTag(tag).filter { it.connects(sourceName, destinationName) }
}
