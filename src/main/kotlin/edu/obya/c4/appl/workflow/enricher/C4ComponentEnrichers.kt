package edu.obya.c4.appl.workflow.enricher

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.C4Tags
import edu.obya.c4.domain.enricher.C4ModelEnricher
import edu.obya.c4.domain.structurizr.util.allComponents
import edu.obya.c4.domain.structurizr.util.havingTag
import com.structurizr.Workspace

object C4QueueComponentsDocumenter : C4ModelEnricher<Long, Workspace> {

    private val queueServerUrl = "https://tools.edgelab.ch/rabbitmq/#/queues/%2Fservices/"

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            this.model.elements.allComponents()
                .havingTag(C4Tags.Infrastructure.QUEUE)
                .onEach {
                    it.url = "$queueServerUrl${it.name}"
                }
        }
    }
}


