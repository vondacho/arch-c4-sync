package com.edgelab.c4.appl

import com.edgelab.c4.appl.usecase.SynchronizeUseCase
import com.edgelab.c4.appl.util.toOptions

fun main(args: Array<String>) {
    SynchronizeUseCase().run(args.toOptions { SynchronizeUseCase.Option.fromId(it) })
}
