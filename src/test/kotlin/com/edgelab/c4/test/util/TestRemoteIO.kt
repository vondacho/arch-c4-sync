package com.edgelab.c4.test.util

import com.edgelab.c4.domain.C4Model
import com.edgelab.c4.domain.C4Reader
import com.edgelab.c4.domain.C4Writer
import com.edgelab.c4.external.structurizr.CloudId
import com.edgelab.c4.external.structurizr.Credential
import com.edgelab.c4.external.structurizr.toModel
import com.structurizr.Workspace
import java.nio.file.Paths

object CloudTest {

    val cloudId = CloudId(0L, Credential("test", "test"))

    object RemoteReader : C4Reader<CloudId, Long, Workspace> {
        override fun read(source: CloudId): C4Model<Long, Workspace>? =
            Paths.get("src/test/resources", "c4-remote-test.json").toModel()
    }

    object RemoteWriter : C4Writer<Long, Workspace, CloudId> {
        override fun write(model: C4Model<Long, Workspace>, destination: CloudId) {}
    }
}
