package edu.obya.c4.domain.strategy

typealias Naming = Map<String, String>
typealias NameComparator = (String, String) -> Boolean
typealias NameCanonizer = (String) -> String

fun Naming.toCanonizer(): NameCanonizer = { a: String -> this[a] ?: a }

fun Naming.toComparator(): NameComparator = { a: String, b: String ->
    a.equals(b, true) || (this.containsKey(a) && this.containsKey(b) && this[a] == this[b])
}

fun String.canonize(nameCanonizer: NameCanonizer) = nameCanonizer(this)
