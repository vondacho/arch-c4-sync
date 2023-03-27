package edu.obya.c4.domain.strategy

/**
 * Venn partitioning of two sets: elements at the intersection and the others.
 * The elements at the intersection get coupled as a pair.
 * The other elements get stored into a degenerated pair.
 */
fun <E> Set<E>.couple(other: Set<E>, comparator: (E, E) -> Boolean): List<Pair<E, E?>> {
    return this.filter { leader -> other.none { challenger -> comparator(leader, challenger) } }.map { it to null }
        .plus(
            other.filter { challenger -> this.none { leader -> comparator(leader, challenger) } }.map { it to null }
                .plus(
                    this.mapNotNull { leader ->
                        other.find { challenger -> comparator(leader, challenger) }?.let { leader to it }
                    })
        )
}
