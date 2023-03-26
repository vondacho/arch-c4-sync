package com.edgelab.c4.appl.workflow.enricher

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Tags
import com.edgelab.c4.domain.enricher.C4ModelEnricher
import com.edgelab.c4.domain.structurizr.util.allContainers
import com.edgelab.c4.domain.structurizr.util.atName
import com.edgelab.c4.domain.structurizr.util.ownedBy
import com.structurizr.Workspace
import com.structurizr.model.Container
import com.structurizr.model.Person
import com.structurizr.model.Tags
import com.structurizr.view.ComponentView
import com.structurizr.view.ContainerView

fun ContainerView.withoutPerson() {
    this.removeElementsWithTag(Tags.PERSON)
}

fun ContainerView.withoutCommonInfrastructure() {
    C4Tags.Infrastructure.common.onEach { this.removeElementsWithTag(it) }
}

fun ContainerView.withoutBrokerInfrastructure() {
    this.removeElementsWithTag(C4Tags.Infrastructure.BROKER)
}

fun ContainerView.withoutStorageInfrastructure() {
    this.removeElementsWithTag(C4Tags.Infrastructure.BROKER)
    this.removeElementsWithTag(C4Tags.Infrastructure.DATABASE)
    this.removeElementsWithTag(C4Tags.Infrastructure.CACHE)
}

fun ContainerView.withoutRelationshipsNotConnectedToOwnership(team: Person) {
    this.relationships.filter {
        it.relationship.ownedBy(team).not()
    }.onEach { this.remove(it.relationship) }
}

fun ContainerView.serviceContextOnly(service: Container) {
    this.withoutCommonInfrastructure()
    this.withoutBrokerInfrastructure()
    this.removeRelationshipsNotConnectedToElement(service)
}

object C4ServiceContextViewCleaner : C4ModelEnricher<Long, Workspace> {

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            views.containerViews.onEach { view ->
                val serviceName = view.key.substringAfterLast('-')
                this.model.elements.allContainers().atName(serviceName)?.let { service ->
                    view.serviceContextOnly(service)
                }
            }
        }
    }
}

fun ComponentView.componentRelationshipsOnly() {
    this.relationships.onEach {
        if (it.relationship.source is Container && it.relationship.destination is Container) {
            this.remove(it.relationship)
        }
    }
}

object C4ServiceComponentViewCleaner : C4ModelEnricher<Long, Workspace> {

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            views.componentViews.onEach { it.componentRelationshipsOnly() }
        }
    }
}


