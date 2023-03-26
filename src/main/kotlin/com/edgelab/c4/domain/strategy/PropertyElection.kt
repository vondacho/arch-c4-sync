package com.edgelab.c4.domain.strategy

typealias PropertyName = String
typealias PropertyWeight = Int
typealias PropertyWeights = Map<PropertyName, PropertyWeight>
typealias PropertyScores = MutableMap<PropertyName, PropertyWeight>

typealias Promotion<COLLECTION> = (COLLECTION) -> Unit

/**
 * Select one property value or the other depending on the value of their respective weight.
 * Weights are compared and the property with the highest weight wins.
 * Equal weights result in a conflict to be solved by moderation.
 */
fun <T> PropertyName.elect(
    a: T?, aWeights: PropertyWeights,
    b: T?, bWeights: PropertyWeights,
    comparator: (T, T) -> Boolean,
    moderator: (T, T) -> T
): Pair<T, PropertyWeight>? {

    val aw = aWeights[this] ?: 0
    val bw = bWeights[this] ?: 0
    return when {
        a == null -> b?.let { it to bw }
        b == null -> a to aw
        else -> if (comparator(a, b)) {
            when {
                aw == bw -> moderator(a, b) to aw
                aw > bw -> a to aw
                else -> b to bw
            }
        } else null
    }
}

/**
 * Select one set of values or the other depending on the value of their respective weight.
 * Weights are compared and the one with the highest weight wins.
 * The elements of a winning set get promoted.
 * Sets with equal weights result in a conflict which requires finer investigation solved by partitioning into
 * a Venn diagram.
 * Coupled elements result in a conflict to be solved by moderation, and winning elements get promoted.
 * Other non-conflicting elements get promoted.
 */
fun <T, C> PropertyName.electSet(
    a: Set<T>, aWeights: PropertyWeights,
    b: Set<T>, bWeights: PropertyWeights,
    comparator: (T, T) -> Boolean,
    moderator: (T, T) -> List<Promotion<C>>,
    promoter: (T) -> Promotion<C>
): Pair<List<Promotion<C>>, PropertyWeight> {

    val aw = aWeights[this] ?: 0
    val bw = bWeights[this] ?: 0

    return when {
        aw == bw && a.isEmpty() -> b.map { promoter(it) } to bw
        aw == bw && b.isEmpty() -> a.map { promoter(it) } to aw
        aw == bw -> a.couple(b, comparator)
            .flatMap { pair ->
                pair.second?.let { moderator(pair.first, it) } ?: listOf(promoter(pair.first))
            } to bw

        aw > bw -> a.map { promoter(it) } to aw
        else -> b.map { promoter(it) } to bw
    }
}

fun <T> PropertyName.takeValue(
    challenger: T?,
    leaderWeights: PropertyScores,
    challengerWeights: PropertyWeights
): T? = challenger?.also { leaderWeights[this] = challengerWeights[this] ?: 0 }

fun <T> PropertyName.electValue(
    leader: T?,
    leaderWeights: PropertyScores,
    challenger: T?,
    challengerWeights: PropertyWeights
): T? =
    elect(leader, leaderWeights, challenger, challengerWeights, { _, _ -> true }, { _, b -> b })
        ?.also { leaderWeights[this] = it.second }
        ?.first

fun <T> PropertyName.electSet(
    leader: Set<T>?,
    leaderWeights: PropertyScores,
    challenger: Set<T>?,
    challengerWeights: PropertyWeights
): Set<T>? =
    elect(leader, leaderWeights, challenger, challengerWeights, { _, _ -> true }, { a, b -> a + b })
        ?.also { leaderWeights[this] = it.second }
        ?.first

fun <T, C> PropertyName.promoteValues(
    leader: Set<T>, leaderWeights: PropertyScores,
    challenger: Set<T>, challengerWeights: PropertyWeights,
    comparator: (T, T) -> Boolean,
    adder: (T) -> Promotion<C>,
    merger: (T, T) -> List<Promotion<C>>
): List<Promotion<C>> =

    this.electSet(leader, leaderWeights, challenger, challengerWeights,
        comparator,
        { a, b -> merger(a, b) },
        { adder(it) })
        .also { leaderWeights[this] = it.second }
        .first

