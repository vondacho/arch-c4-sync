package edu.obya.c4.domain.structurizr.strategy

import edu.obya.c4.domain.C4Tags.purge
import edu.obya.c4.domain.strategy.*
import edu.obya.c4.util.notEmpty
import edu.obya.c4.util.notNull
import com.structurizr.model.Element
import com.structurizr.model.Model
import com.structurizr.model.Relationship
import com.structurizr.model.StaticStructureElement

class MergeRelationshipsStrategy(val nameComparator: NameComparator, val elements: Set<Element>) {

    private val elementComparator = elementComparator(nameComparator)
    private val relationshipComparator = relationshipComparator(elementComparator)

    private val elementsMap = elements.filterIsInstance<StaticStructureElement>().associateBy { it.name }
    private val elementFinder = { name: String -> elementsMap[name] }

    fun execute(
        master: Set<Relationship>,
        other: Set<Relationship>,
        scores: PropertyScores,
        weights: PropertyWeights,
    ): List<(Model) -> Unit> =

        "model.relationships".promoteValues(
            master, scores, other, weights, relationshipComparator,
            { it.add(scores, weights) },
            { a, b -> listOf(a.merge(b, scores, weights)) }
        )

    private fun Relationship.merge(
        other: Relationship,
        scores: PropertyScores,
        weights: PropertyWeights
    ): Promotion<Model> =
        { _ ->
            elementFinder(this.source.name)?.let { source ->
                elementFinder(this.destination.name)?.let { destination ->
                    val relationship = source.uses(
                        destination,
                        "relationship.description".electValue(
                            this.description?.notEmpty(),
                            scores,
                            other.description?.notEmpty(),
                            weights
                        ).notNull(),
                        "relationship.technology".electValue(
                            this.technology?.notEmpty(),
                            scores,
                            other.technology?.notEmpty(),
                            weights
                        ).notNull(),
                        "relationship.interactionStyle".electValue(
                            this.interactionStyle,
                            scores,
                            other.interactionStyle,
                            weights
                        ),
                        "relationship.tags".electSet(this.tagsAsSet, scores, other.tagsAsSet, weights)!!
                            .purge().toTypedArray()
                    )
                    relationship?.url = "relationship.url".electValue(this.url, scores, other.url, weights)
                    "relationship.properties".electValue(this.properties, scores, other.properties, weights)?.onEach {
                        relationship?.addProperty(it.key, it.value)
                    }
                }
            }
        }

    private fun Relationship.add(scores: PropertyScores, weights: PropertyWeights): Promotion<Model> =
        { _ ->
            elementFinder(this.source.name)?.let { source ->
                elementFinder(this.destination.name)?.let { destination ->
                    val relationship = source.uses(
                        destination,
                        "relationship.description".takeValue(this.description, scores, weights),
                        "relationship.technology".takeValue(this.technology, scores, weights),
                        "relationship.interactionStyle".takeValue(
                            this.interactionStyle,
                            scores,
                            weights
                        ),
                        "relationship.tags".takeValue(this.tagsAsSet, scores, weights)!!.purge().toTypedArray()
                    )
                    relationship?.url = "relationship.url".takeValue(this.url, scores, weights)
                    "relationship.properties".takeValue(this.properties, scores, weights)?.onEach {
                        relationship?.addProperty(it.key, it.value)
                    }
                }
            }
        }
}
