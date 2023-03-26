package com.edgelab.c4.external.metadata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

class MetadataDslParser {
    private val jsonMapper = jacksonObjectMapper()
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    fun parseJson(source: File): MetaRepository = jsonMapper.readValue(source, MetaRepository::class.java)
    fun parseYaml(source: File): MetaRepository = yamlMapper.readValue(source, MetaRepository::class.java)

    data class MetaRepository(val components: Map<MetaName, MetaComponent>) {
        data class MetaComponent(
            val owner: MetaOwner,
            val classification: MetaClassification,
            val documentation: MetaDocumentation?,
            val dependencies: MetaDependencies?
        ) {
            data class MetaClassification(val service_type: String, val app_type: String)
            data class MetaDocumentation(val url: String)
            data class MetaDependencies(val components: Set<MetaDependency>, val data: Map<MetaDataStoreType, MetaDataStore>?)
        }
    }
}

typealias MetaName = String
typealias MetaOwner = String
typealias MetaDependency = String
typealias MetaDataStoreType = String
typealias MetaDataStoreInstance = String
typealias MetaDataStoreContent = String
typealias MetaDataStore = Map<MetaDataStoreInstance, Set<MetaDataStoreContent>>
