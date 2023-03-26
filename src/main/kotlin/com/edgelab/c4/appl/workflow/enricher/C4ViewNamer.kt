package com.edgelab.c4.appl.workflow.enricher

import freemarker.template.Template
import java.io.StringWriter

class C4ViewNamer(private val naming: Map<String, Template>) {

    fun name(viewType: C4ViewType, model: Map<String, Any>): String {
        val writer = StringWriter()
        naming[viewType.name]?.process(model, writer)
        return writer.toString()
    }
}
