package com.edgelab.c4.domain.structurizr

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4ModelId
import com.edgelab.c4.domain.strategy.PropertyWeights
import com.edgelab.c4.domain.structurizr.strategy.MergeWorkspaceStrategy
import com.structurizr.Workspace

class StructurizrModel(
    override val id: C4ModelId<Long>,
    override val state: Workspace,
    override val weights: PropertyWeights = emptyMap()
) : C4Model<Long, Workspace> {

    override fun merge(
        other: C4Model<Long, Workspace>,
        mergeContext: C4Model.MergeContext
    ): C4Model<Long, Workspace> {

        val scores = this.weights.toMutableMap()
        val workspace = MergeWorkspaceStrategy(mergeContext)
            .execute(this.state, other.state, scores, other.weights)

        return StructurizrModel(
            id = this.id,
            state = workspace,
            weights = scores
        )
    }
}
