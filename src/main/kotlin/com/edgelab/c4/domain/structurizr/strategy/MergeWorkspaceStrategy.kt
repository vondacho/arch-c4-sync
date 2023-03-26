package com.edgelab.c4.domain.structurizr.strategy

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.strategy.PropertyScores
import com.edgelab.c4.domain.strategy.PropertyWeights
import com.edgelab.c4.domain.strategy.canonize
import com.edgelab.c4.domain.strategy.electValue
import com.edgelab.c4.util.JOCKER
import com.edgelab.c4.util.emptyIf
import com.structurizr.Workspace

class MergeWorkspaceStrategy(private val mergeContext: C4Model.MergeContext) {
    fun execute(
        master: Workspace,
        other: Workspace,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Workspace {
        with(mergeContext) {

            val workspace = Workspace(
                "workspace.name".electValue(
                    master.name?.emptyIf(JOCKER),
                    scores,
                    other.name?.emptyIf(JOCKER),
                    weights
                )!!
                    .canonize(nameCanonizer),
                "workspace.description".electValue(
                    master.description?.emptyIf(JOCKER),
                    scores,
                    other.description?.emptyIf(JOCKER),
                    weights
                )
            )

            MergeModelStrategy(nameComparator, nameCanonizer)
                .execute(master.model, other.model, scores, weights)
                .onEach { it(workspace.model) }

            MergeRelationshipsStrategy(nameComparator, workspace.model.elements)
                .execute(master.model.relationships, other.model.relationships, scores, weights)
                .onEach { it(workspace.model) }

            MapNodesStrategy(nodeMapper).execute()(workspace.model)

            MergeViewSetStrategy(
                nameComparator,
                nameCanonizer,
                viewFilter,
                workspace.model.elements,
                workspace.model.deploymentNodes
            )
                .execute(master.views, other.views, scores, weights)
                .onEach { it(workspace.views) }

            MergeConfigurationStrategy().execute(
                master.views.configuration,
                other.views.configuration,
                scores,
                weights
            )(workspace.views.configuration)

            MergeDocumentationStrategy(nameComparator, nameCanonizer).execute(
                master.documentation,
                other.documentation,
                scores,
                weights
            )(workspace.documentation)

            return workspace
        }
    }
}
