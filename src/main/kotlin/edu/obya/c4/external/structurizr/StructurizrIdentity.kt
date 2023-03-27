package edu.obya.c4.external.structurizr

typealias WorkspaceId = Long

data class CloudId(val id: WorkspaceId, val credential: Credential)

data class Credential(val apiKey: String, val apiSecret: String)
