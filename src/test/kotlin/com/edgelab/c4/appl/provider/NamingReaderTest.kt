package com.edgelab.c4.appl.provider

import com.edgelab.c4.appl.config.NamingProvider.readNaming
import com.edgelab.c4.util.toResourceStream
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly

internal class NamingReaderTest : StringSpec({

    "returns the expected naming from file" {
        val naming = "c4-naming-test.properties".toResourceStream().readNaming()
        naming shouldContainExactly mapOf(
            "local.WORKSPACE.name" to "local.workspace.name",
            "recco2" to "recco",
            "Recco" to "recco"
        )
    }
})
