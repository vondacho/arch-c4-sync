package edu.obya.c4.domain.structurizr.strategy

import edu.obya.c4.domain.strategy.NameCanonizer
import edu.obya.c4.domain.strategy.NameComparator
import edu.obya.c4.domain.strategy.PropertyScores
import edu.obya.c4.domain.strategy.PropertyWeights
import com.structurizr.documentation.Documentation

class MergeDocumentationStrategy(
    val nameComparator: NameComparator,
    val nameCanonizer: NameCanonizer
) {
    fun execute(
        master: Documentation,
        other: Documentation,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (Documentation) -> Unit {
        return { }
    }
}
