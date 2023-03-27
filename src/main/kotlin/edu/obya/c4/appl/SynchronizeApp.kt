package edu.obya.c4.appl

import edu.obya.c4.appl.usecase.SynchronizeUseCase
import edu.obya.c4.appl.util.toOptions

fun main(args: Array<String>) {
    SynchronizeUseCase().run(args.toOptions { SynchronizeUseCase.Option.fromId(it) })
}
