package edu.obya.c4.domain.enricher

interface C4ViewCleaner {
    fun clean(viewKey: String)
}

object NoViewCleaner: C4ViewCleaner {
    override fun clean(viewKey: String) = Unit
}
