package edu.obya.c4.util

fun String?.notNull(): String = this ?: ""
fun String.notEmpty(): String? = this.takeIf { it.isNotEmpty() }
fun String.emptyIf(term: String): String? = this.notEmpty()?.takeIf { it != term }
fun String.normalize() = this.lowercase().replace(" ", "-").replace(".", "-")

val JOCKER = "*"
