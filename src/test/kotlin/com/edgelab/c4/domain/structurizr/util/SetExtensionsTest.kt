package com.edgelab.c4.domain.structurizr.util

import com.edgelab.c4.domain.strategy.couple
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize

internal class SetExtensionsTest : StringSpec({

    "one empty set" {
        val a = setOf(1, 2)
        val b = emptySet<Int>()
        a.couple(b) { aa, bb -> aa == bb }.shouldContainExactlyInAnyOrder(
            1 to null,
            2 to null
        )
    }

    "both empty sets" {
        val a = emptySet<Int>()
        val b = emptySet<Int>()
        a.couple(b) { aa, bb -> aa == bb } shouldHaveSize 0
    }

    "two disjoint sets" {
        val a = setOf(1, 2)
        val b = setOf(3, 4)
        a.couple(b) { aa, bb -> aa == bb }.shouldContainExactlyInAnyOrder(
            1 to null,
            2 to null,
            3 to null,
            4 to null
        )
    }

    "one included set" {
        val a = setOf(1, 2, 3, 4)
        val b = setOf(3, 4)
        a.couple(b) { aa, bb -> aa == bb }.shouldContainExactlyInAnyOrder(
            1 to null,
            2 to null,
            3 to 3,
            4 to 4
        )
    }

    "one partially included set" {
        val a = setOf(1, 2, 3, 4)
        val b = setOf(3, 4, 5, 6)
        a.couple(b) { aa, bb -> aa == bb }.shouldContainExactlyInAnyOrder(
            1 to null,
            2 to null,
            3 to 3,
            4 to 4,
            5 to null,
            6 to null
        )
    }
})
