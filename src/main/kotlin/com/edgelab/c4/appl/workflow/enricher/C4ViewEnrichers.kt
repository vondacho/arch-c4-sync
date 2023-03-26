package com.edgelab.c4.appl.workflow.enricher

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Tags
import com.edgelab.c4.domain.enricher.C4ModelEnricher
import com.edgelab.c4.util.logger
import com.structurizr.Workspace
import com.structurizr.model.Component
import com.structurizr.view.ComponentView
import com.structurizr.view.ElementView

fun ComponentView.withQueueDependencies() {
    this.elements
        .allComponents()
        .flatMap { it.relationships }
        .onEach { rel ->
            if (rel.destination.hasTag(C4Tags.Infrastructure.QUEUE)) {
                runCatching {
                    this.add(rel.destination as Component)
                }.onFailure {
                    this.javaClass.logger().info(it.message)
                }
            }
        }
}

fun ComponentView.withDatabaseSchemaDependencies() {
    this.elements
        .allComponents()
        .flatMap { it.relationships }
        .onEach { rel ->
            if (rel.destination.hasTag(C4Tags.Infrastructure.KEYSPACE) ||
                rel.destination.hasTag(C4Tags.Infrastructure.SCHEMA)
            ) {
                runCatching {
                    this.add(rel.destination as Component)
                }.onFailure {
                    this.javaClass.logger().info(it.message)
                }
            }
        }
}

object C4ServiceComponentViewEnricher : C4ModelEnricher<Long, Workspace> {

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            views.componentViews.onEach {
                it.withQueueDependencies()
                it.withDatabaseSchemaDependencies()
                it.setExternalSoftwareSystemBoundariesVisible(true)
            }
        }
    }
}

private fun Collection<ElementView>.allComponents() = this.map { it.element }.filterIsInstance<Component>()


