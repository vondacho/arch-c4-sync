package edu.obya.c4.appl.usecase

import edu.obya.c4.appl.util.AppOption
import edu.obya.c4.domain.strategy.PropertyWeights
import edu.obya.c4.test.util.CloudTest
import com.structurizr.model.Enterprise
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToUsingFields
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

internal class SynchronizeUseCaseIT : StringSpec({

    "returns remote model synchronized with local model" {

        val root = "src/test/resources"

        val remoteWeights: PropertyWeights = mapOf(
            "workspace.name" to 10,
            "workspace.description" to 10,
            "model.enterprise.name" to 20,
            "model.people" to 10,
            "person.description" to 10
        )

        val localWeights: PropertyWeights = mapOf(
            "workspace.name" to 20, // higher priority than remote
            "model.enterprise.name" to 10, // lower priority than remote
            "model.people" to 10, // same priority as remote
            "person.description" to 20, // higher priority than remote
            "model.relationships" to 10, // higher priority than remote
            "configuration.styles.elements" to 10, // higher priority than remote
            "configuration.styles.relationships" to 10 // higher priority than remote
        )

        SynchronizeUseCase(
            remoteReaderProvider = { CloudTest.RemoteReader },
            remoteWriterProvider = { CloudTest.RemoteWriter },
            localWeightsProvider = { localWeights },
            remoteWeightsProvider = { remoteWeights }
        )
            .run(
                setOf(
                    AppOption(SynchronizeUseCase.Option.WithDsl).addParameter("$root/c4-local-test.dsl"),
                    AppOption(SynchronizeUseCase.Option.WithNaming).addParameter("$root/c4-naming-test.properties"),
                    AppOption(SynchronizeUseCase.Option.WithDownload)
                        .addParameter(CloudTest.cloudId.id.toString())
                        .addParameter(CloudTest.cloudId.credential.apiKey)
                        .addParameter(CloudTest.cloudId.credential.apiSecret)
                )
            )
            ?.let { model ->
                model.state.let {
                    it.name?.shouldBe("local.workspace.name")
                    it.description?.shouldBe("remote.workspace.description")
                    it.model shouldNotBe null
                    with(it.model!!) {
                        softwareSystems.shouldHaveSize(1)
                        people.shouldHaveSize(1)
                        relationships.shouldHaveSize(1)
                    }
                }
                model.weights shouldContainExactly mapOf(
                    "workspace.name" to 20,
                    "workspace.description" to 10,
                    "model.enterprise.name" to 20,
                    "model.people" to 10,
                    "model.systems" to 0,
                    "model.relationships" to 10,
                    "model.deploymentNodes" to 0,
                    "person.name" to 0,
                    "person.description" to 20,
                    "person.location" to 0,
                    "person.tags" to 0,
                    "system.location" to 0,
                    "system.name" to 0,
                    "system.description" to 0,
                    "system.tags" to 0,
                    "system.containers" to 0,
                    "container.name" to 0,
                    "container.description" to 0,
                    "container.technology" to 0,
                    "container.tags" to 0,
                    "container.components" to 0,
                    "component.name" to 0,
                    "component.description" to 0,
                    "component.technology" to 0,
                    "component.tags" to 0,
                    "component.code" to 0,
                    "configuration.styles.elements" to 10,
                    "configuration.styles.relationships" to 10,
                    "relationship.description" to 0,
                    "relationship.technology" to 0,
                    "relationship.tags" to 0,
                    "viewSet.systemLandscapeViews" to 0,
                    "viewSet.systemContextViews" to 0,
                    "viewSet.containerViews" to 0,
                    "viewSet.componentViews" to 0,
                    "viewSet.filteredViews" to 0,
                    "viewSet.deploymentViews" to 0
                )
            }
    }
})
