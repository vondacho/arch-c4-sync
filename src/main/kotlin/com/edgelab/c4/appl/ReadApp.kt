package com.edgelab.c4.appl

import com.edgelab.c4.appl.usecase.ReadUseCase
import com.edgelab.c4.appl.util.toPath

fun main(args: Array<String>) {
    ReadUseCase.run(args.toPath())
}
