package edu.obya.c4.appl

import edu.obya.c4.appl.usecase.DownloadUseCase
import edu.obya.c4.appl.util.toCloudId

fun main(args: Array<String>) {
    DownloadUseCase.run(args.toCloudId())
}
