package edu.obya.c4.domain.structurizr.strategy

import edu.obya.c4.domain.strategy.NameComparator
import com.structurizr.model.Element
import com.structurizr.model.Relationship
import com.structurizr.view.ElementView
import com.structurizr.view.RelationshipView

fun elementComparator(comparator: NameComparator) = { a: Element, b: Element ->
    a.canonicalName.equals(b.canonicalName, true) || comparator(a.name, b.name)
}

fun elementViewComparator(comparator: (Element, Element) -> Boolean) =
    { a: ElementView, b: ElementView -> comparator(a.element, b.element) }

fun relationshipComparator(comparator: (Element, Element) -> Boolean) =
    { a: Relationship, b: Relationship ->
        comparator(a.source, b.source)
                && a.interactionStyle == b.interactionStyle
                && a.interactionStyle == b.interactionStyle
                && a.technology == b.technology
                && a.properties.sameNameAs(b.properties)
    }

fun relationshipViewComparator(comparator: (Relationship, Relationship) -> Boolean) =
    { a: RelationshipView, b: RelationshipView ->
        (a.description == b.description) && comparator(a.relationship, b.relationship)
    }


private fun Map<String, String>.sameNameAs(other: Map<String, String>) = this["name"] == other["name"]
