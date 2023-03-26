package com.edgelab.c4.appl

import com.edgelab.c4.appl.usecase.UploadUseCase
import com.edgelab.c4.appl.util.toCloudId
import com.edgelab.c4.appl.util.toPath

fun main(args: Array<String>) {
    UploadUseCase.run(args.toPath(), args.toCloudId(1))
}
