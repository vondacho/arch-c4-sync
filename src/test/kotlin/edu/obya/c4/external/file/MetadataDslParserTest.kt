package edu.obya.c4.external.file

import edu.obya.c4.external.metadata.MetadataDslParser
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths

internal class MetadataDslParserTest : FreeSpec({

    val root = "src/test/resources"

    "returns the expected model from JSON or YAML file" - {
        listOf(
            { MetadataDslParser().parseJson(Paths.get(root, "c4-metadata-test.json").toFile()) },
            { MetadataDslParser().parseYaml(Paths.get(root, "c4-metadata-test.yaml").toFile()) }
        ).forEach {

            val project = it()

            project shouldNotBe null
            project.components shouldContainKey "recco2"
            with(project.components.getValue("recco2")) {
                owner shouldBe "Lumberjacks"
                dependencies?.components?.shouldContain("consul")
                dependencies?.data?.shouldContainKey("postgresql")
                with(dependencies?.data?.getValue("postgresql")) {
                    this?.shouldContainKey("main")
                    this?.getValue("main")?.shouldContain("recco")
                }
                dependencies?.data?.shouldContainKey("rabbitmq")
                with(dependencies?.data?.getValue("rabbitmq")) {
                    this?.shouldContainKey("main")
                    this?.getValue("main")?.shouldContain("maestro.event.pricing.ingested.recco2")
                }
            }
        }
    }
})
