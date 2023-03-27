package edu.obya.c4.domain.enricher

import edu.obya.c4.domain.C4Model

interface C4ModelEnricher<I, T> {
    fun enrich(model: C4Model<I, T>)
}
