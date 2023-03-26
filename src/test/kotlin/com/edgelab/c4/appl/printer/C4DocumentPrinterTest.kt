package com.edgelab.c4.appl.printer

import com.edgelab.c4.appl.workflow.writer.StructurizrPrinter
import com.edgelab.c4.external.structurizr.toModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths

internal class C4DocumentPrinterTest : StringSpec({

    "produces a markdown representation of the C4 model" {

        val root = "src/test/resources"

        val c4Model = Paths.get(root, "c4-local-test.dsl").toModel()

        c4Model shouldNotBe null

        c4Model?.let { StructurizrPrinter().write(it) }
    }
})
