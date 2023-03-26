package com.edgelab.c4.domain.structurizr.strategy

import com.edgelab.c4.domain.strategy.NodeMapper
import com.edgelab.c4.domain.structurizr.util.leafNodes
import com.edgelab.c4.util.logger
import com.structurizr.model.Container
import com.structurizr.model.Model

class MapNodesStrategy(val nodeMapper: NodeMapper) {
    private val logger = this.javaClass.logger()

    fun execute(): (Model) -> Unit =
        { model ->
            val containers = model.elements.filterIsInstance<Container>()
            val nodesMap = model.deploymentNodes.flatMap { it.leafNodes() }.associateBy { it.name }
            containers.onEach { container ->
                nodesMap[nodeMapper(container.name)]?.let { node ->
                    logger.info("deploy container ${container.name} to node ${node.name}")
                    node.add(container)
                }
            }
        }
}
