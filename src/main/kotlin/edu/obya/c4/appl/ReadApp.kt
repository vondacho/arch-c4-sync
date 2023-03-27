package edu.obya.c4.appl

import edu.obya.c4.appl.usecase.ReadUseCase
import edu.obya.c4.appl.util.toPath

fun main(args: Array<String>) {
    ReadUseCase.run(args.toPath())
}
