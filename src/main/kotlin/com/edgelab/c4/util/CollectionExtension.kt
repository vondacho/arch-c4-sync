package com.edgelab.c4.util

fun <T> Collection<T>.notEmpty(): Collection<T>? = this.takeIf { it.isNotEmpty() }
