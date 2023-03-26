package com.edgelab.c4.domain.enricher

import com.edgelab.c4.domain.C4Model

interface C4ModelEnricher<I, T> {
    fun enrich(model: C4Model<I, T>)
}
