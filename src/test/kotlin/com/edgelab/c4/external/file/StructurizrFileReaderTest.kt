package com.edgelab.c4.external.file

import com.edgelab.c4.external.structurizr.toModel
import com.structurizr.model.Enterprise
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToUsingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths

internal class StructurizrFileReaderTest : StringSpec({

    val root = "src/test/resources"

    "returns the expected model from JSON file" {
        val c4Model = Paths.get(root, "c4-local-test.json").toModel()

        c4Model shouldNotBe null

        with(c4Model!!.state) {
            name?.shouldBe("local.WORKSPACE.name")
            description?.shouldBe("local.workspace.description")
            model shouldNotBe null
            with(model!!) {
                enterprise?.shouldBeEqualToUsingFields(Enterprise("local.workspace.model.enterprise.name"))
                softwareSystems.shouldHaveSize(0)
                people.shouldHaveSize(1)
            }
        }
    }

    "returns the expected model from DSL file" {
        val c4Model = Paths.get(root, "c4-local-test.dsl").toModel()

        c4Model shouldNotBe null

        with(c4Model!!.state) {
            name?.shouldBe("local.WORKSPACE.name")
            description?.shouldBe("local.workspace.description")
            model shouldNotBe null
            with(model!!) {
                enterprise?.shouldBeEqualToUsingFields(Enterprise("local.workspace.model.enterprise.name"))
                softwareSystems.shouldHaveSize(1)
                people.shouldHaveSize(1)
                relationships.shouldHaveSize(1)
            }
        }
    }
})
