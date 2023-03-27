package edu.obya.c4.appl

import edu.obya.c4.appl.usecase.ServiceDeploymentViewGenUseCase
import edu.obya.c4.appl.util.toCloudId
import edu.obya.c4.appl.util.toPath

fun main(args: Array<String>) {
    ServiceDeploymentViewGenUseCase.run(args.toPath(), args.toCloudId(1))
}
