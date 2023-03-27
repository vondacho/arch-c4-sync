package edu.obya.c4.appl.util

import edu.obya.c4.external.structurizr.CloudId
import edu.obya.c4.external.structurizr.Credential
import java.nio.file.Path
import java.nio.file.Paths

typealias AppParameter = String
typealias AppParameters = Array<AppParameter>

fun AppParameters.toCloudId(fromIdx: Int = 0): CloudId =
    if (this.size >= fromIdx + 3) CloudId(
        id = this[fromIdx].toLong(),
        credential = Credential(apiKey = this[fromIdx + 1], apiSecret = this[fromIdx + 2])
    )
    else throw IllegalArgumentException("A cloud identity (<workspaceid> <apikey> <apisecret>) must be given as parameter.")

fun AppParameters.toPath(fromIdx: Int = 0): Path = toPathOptional(fromIdx)
    ?: throw IllegalArgumentException("A path to a folder containing one or more of model.dsl|json files must be given as parameter.")

fun AppParameters.toPathOptional(fromIdx: Int = 0): Path? =
    if (this.size >= fromIdx + 1) Paths.get(this[fromIdx]) else null


interface AppOptionId {
    val id: String
}

data class AppOption<T : AppOptionId>(val id: T, private val parameters: MutableList<AppParameter> = mutableListOf()) {
    fun addParameter(parameter: AppParameter): AppOption<T> {
        parameters.add(parameter)
        return this
    }

    fun allParameters(): List<AppParameter> = parameters.toList()

    fun takeParameters(count: Int): List<AppParameter> =
        if (parameters.size <= count) parameters.take(count)
        else throw IllegalStateException("less parameters than expected: $count")

    fun oneParameter(): AppParameter? = parameters.firstOrNull()
    fun oneParameterOrFail(): AppParameter = takeParameters(1).first()
}

fun <T : AppOptionId> AppParameters.toOptions(fromIdx: Int = 0, f: (AppParameter) -> T?): Set<AppOption<T>> =
    this.slice(fromIdx until this.size).toOptions(f).toSet()

fun <T : AppOptionId> List<AppParameter>.toOptions(f: (AppParameter) -> T?): List<AppOption<T>> =
    this.fold(listOf(), { acc, s ->
        if (acc.isNotEmpty()) {
            f(s)?.let { acc + AppOption(it) } ?: acc.also {
                acc.last().addParameter(s)
            }
        } else {
            f(s)?.let { acc + AppOption(it) } ?: acc
        }
    })

fun <T : AppOptionId> Set<AppOption<T>>.at(option: AppOptionId): AppOption<T>? = this.firstOrNull { it.id == option }

