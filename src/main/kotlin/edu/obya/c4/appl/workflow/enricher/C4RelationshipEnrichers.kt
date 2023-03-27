package edu.obya.c4.appl.workflow.enricher

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.enricher.C4ModelEnricher
import edu.obya.c4.domain.structurizr.util.byTag
import edu.obya.c4.domain.structurizr.util.isCommand
import edu.obya.c4.domain.structurizr.util.isEvent
import com.structurizr.Workspace

object C4SyncRelationshipsDocumenter : C4ModelEnricher<Long, Workspace> {

    private val queueServerUrl = "https://tools.edgelab.ch/rabbitmq/#/queues/%2Fservices/"

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            this.model.relationships
                .byTag()
                .filterKeys { it.isEvent() || it.isCommand() }
                .onEach {
                    it.value.onEach { relationship ->
                        relationship.url = "$queueServerUrl${it.key}"
                    }
                }
        }
    }
}


