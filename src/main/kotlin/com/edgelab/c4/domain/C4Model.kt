package com.edgelab.c4.domain

import com.edgelab.c4.domain.strategy.NameCanonizer
import com.edgelab.c4.domain.strategy.NameComparator
import com.edgelab.c4.domain.strategy.NodeMapper
import com.edgelab.c4.domain.strategy.PropertyWeights

data class C4ModelId<I>(val id: I)

interface C4Model<I, T> {
    val id: C4ModelId<I>
    val state: T
    val weights: PropertyWeights

    data class MergeContext(
        val nameComparator: NameComparator,
        val nameCanonizer: NameCanonizer,
        val viewFilter: C4ViewFilter = NoViewFilter,
        val nodeMapper: NodeMapper
    )

    fun merge(other: C4Model<I, T>, mergeContext: MergeContext): C4Model<I, T>
}

interface C4ViewFilter {
    fun filter(key: String): Boolean
}

object NoViewFilter : C4ViewFilter {
    override fun filter(key: String) = true
}

interface C4Reader<S, I, T> {
    fun read(source: S): C4Model<I, T>?
}

interface C4Writer<I, T, D> {
    fun write(model: C4Model<I, T>, destination: D)
}

interface C4Repository<I, T> {
    fun findOne(id: C4ModelId<I>): C4Model<I, T>?
    fun save(model: C4Model<I, T>): C4Model<I, T>?
}

