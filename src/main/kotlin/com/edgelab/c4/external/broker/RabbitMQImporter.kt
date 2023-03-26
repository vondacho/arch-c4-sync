package com.edgelab.c4.external.broker

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Tags
import com.edgelab.c4.domain.C4Technologies
import com.edgelab.c4.domain.enricher.C4ModelEnricher
import com.edgelab.c4.domain.strategy.NameCanonizer
import com.edgelab.c4.domain.structurizr.util.*
import com.edgelab.c4.util.logger
import com.structurizr.Workspace

class RabbitMQImporter(private val nameCanonizer: NameCanonizer) : C4ModelEnricher<Long, Workspace> {
    private val logger = this.javaClass.logger()

    private val queues20201101 = listOf(
        "adam.command.compute-assets",
        "adam.event.asset.computed.copernic",
        "adam.event.asset.computed.maestro",
        "arva.command.issuer.index.arva",
        "copernic.command.universe-update.copernic",
        "copernic.internal.command.synchronize-universe.copernic",
        "espresso.event.portfolio.requested.maestro",
        "eve.command.value",
        "eve.event.asset.priced",
        "lipo.event.issuer-proxy.created.arva",
        "maestro.command.price-asset.maestro",
        "maestro.event.pricing.ingested.recco2",
        "maestro.event.run.validated.recco",
        "maestro.event.run.validated.recco2",
        "maestro.event.runs.cleaned.recco2",
        "marketdata.event.asset.created.hubble",
        "marketdata.event.asset.deleted.hubble",
        "marketdata.event.asset.deleted.maestro",
        "marketdata.event.asset.discovered.hubble",
        "marketdata.event.asset.discovered.maestro",
        "marketdata.event.asset.updated.hubble",
        "marketdata.event.asset.updated.maestro",
        "marketdata.event.issuer.created.lipo",
        "marketdata.event.issuer.updated.lipo",
        "marketdata.internal.asset.created.marketdata",
        "marketdata.internal.asset.deleted.marketdata",
        "marketdata.internal.asset.updated.marketdata",
        "marketdata.internal.command.cache.marketdata",
        "marketdata.internal.command.structured-product-update.marketdata",
        "marketdata.internal.command.update.marketdata",
        "marketdata.internal.issuer.created.marketdata",
        "marketdata.internal.issuer.updated.marketdata",
        "marketgen.event.curve.scenarios.created.lipo",
        "marketgen.event.curve.status.lipo",
        "recco.event.run.switched.themis",
        "recco2.event.assets.requested.maestro",
        "recco2.event.pricing.ingested.arva",
        "runs-runner.event.validated.maestro",
        "scalpel.event.default-curve.computed.arva",
        "scalpel.event.default-curve.computed.lipo",
        "scalpel.event.default-curve.error.lipo",
        "scalpel.event.interest-rate-curve.computed.lipo",
        "scalpel.event.interest-rate-curve.error.lipo",
        "scalpel.internal.credit.batch.tasks",
        "scalpel.internal.yield.batch.tasks",
        "scalpel.surgery.incoming.default",
        "scalpel.surgery.incoming.interest",
        "silkroad.responses.hermes",
        "themis.event.run.validated.arva",
        "themis.event.run.validated.recco",
        "themis.internal.command.compute-prc.themis"
    )

    override fun enrich(model: C4Model<Long, Workspace>) {
        with(model.state) {
            val broker = this.model.elements
                .allContainers()
                .havingTag(C4Tags.Infrastructure.BROKER)
                .first()

            val queues = broker.components.byName()

            queues20201101
                .filterNot { queues.containsKey(it) }
                .onEach {
                    val component = broker.addComponent(it, "", C4Technologies.AMQP)

                    component.addTags(C4Tags.Infrastructure.INFRA, C4Tags.Infrastructure.QUEUE)
                    if (component.isCommandQueue()) component.addTags(C4Tags.Architecture.COMMAND)
                    else if (component.isEventQueue()) component.addTags(C4Tags.Architecture.EVENT)

                    logger.debug("imported queue $it from broker ${broker.name}")
                }
        }
    }
}
