package com.edgelab.c4.util

import com.edgelab.c4.appl.util.AppOption
import com.edgelab.c4.appl.util.AppOptionId
import com.edgelab.c4.appl.util.toOptions
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize


internal class AppParametersTest : StringSpec({

    "should not extract any option" {
        listOf("-x").toOptions(f = { TestOption.fromId(it) }).shouldBeEmpty()
        listOf("").toOptions(f = { TestOption.fromId(it) }).shouldBeEmpty()
        arrayOf("-x").toOptions(f = { TestOption.fromId(it) }).shouldBeEmpty()
        arrayOf("").toOptions(f = { TestOption.fromId(it) }).shouldBeEmpty()
    }

    "should extract a single option without parameter from a given list" {
        val options = listOf("-a").toOptions(f = { TestOption.fromId(it) })
        options shouldHaveSize 1
        options.shouldContainExactly(AppOption(TestOption.A))
    }

    "should extract a single option without parameter from a given array" {
        val options = arrayOf("-a").toOptions(f = { TestOption.fromId(it) })
        options shouldHaveSize 1
        options.shouldContainExactly(AppOption(TestOption.A))
    }

    "should extract a single option with one parameter" {
        val options = listOf("-a", "test").toOptions(f = { TestOption.fromId(it) })
        options shouldHaveSize 1
        options.shouldContainExactly(AppOption(TestOption.A).addParameter("test"))
    }

    "should extract a single option with two parameters" {
        val options = listOf("-a", "test1", "test2").toOptions(f = { TestOption.fromId(it) })
        options shouldHaveSize 1
        options.shouldContainExactly(AppOption(TestOption.A).addParameter("test1").addParameter("test2"))
    }

    "should extract two options without parameter" {
        val options = listOf("-a", "-b").toOptions(f = { TestOption.fromId(it) })
        options shouldHaveSize 2
        options.shouldContainExactlyInAnyOrder(AppOption(TestOption.A), AppOption(TestOption.B))
    }

    "should extract two options with parameter" {
        val options = listOf("-a", "test", "-b", "test").toOptions(f = { TestOption.fromId(it) })
        options shouldHaveSize 2
        options.shouldContainExactlyInAnyOrder(
            AppOption(TestOption.A).addParameter("test"),
            AppOption(TestOption.B).addParameter("test")
        )
    }
})

enum class TestOption(override val id: String) : AppOptionId {
    A("-a"),
    B("-b");

    companion object {
        fun fromId(id: String): TestOption? = values().firstOrNull { it.id == id }
    }
}
