package edu.obya.c4.domain.enricher

import edu.obya.c4.domain.C4Model
import edu.obya.c4.domain.C4ViewFilter

interface C4ViewGenerator<I, T> {
    val viewFilter: C4ViewFilter

    fun generate(model: C4Model<I, T>): C4Model<I, T>
}
