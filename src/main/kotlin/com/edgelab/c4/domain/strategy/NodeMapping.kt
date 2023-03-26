package com.edgelab.c4.domain.strategy

typealias NodeMapping = Map<String, String>
typealias NodeMapper = (String) -> String

fun NodeMapping.toMapper(): NodeMapper = { a: String -> this[a] ?: a }
