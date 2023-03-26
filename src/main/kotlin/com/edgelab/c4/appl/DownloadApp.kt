package com.edgelab.c4.appl

import com.edgelab.c4.appl.usecase.DownloadUseCase
import com.edgelab.c4.appl.util.toCloudId

fun main(args: Array<String>) {
    DownloadUseCase.run(args.toCloudId())
}
