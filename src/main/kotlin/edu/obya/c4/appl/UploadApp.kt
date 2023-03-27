package edu.obya.c4.appl

import edu.obya.c4.appl.usecase.UploadUseCase
import edu.obya.c4.appl.util.toCloudId
import edu.obya.c4.appl.util.toPath

fun main(args: Array<String>) {
    UploadUseCase.run(args.toPath(), args.toCloudId(1))
}
