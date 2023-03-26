package com.edgelab.c4.appl.workflow.filter

import com.edgelab.c4.appl.workflow.Context
import com.edgelab.c4.appl.workflow.Filter

object DoNothingFilter : Filter {
    override val name: String = "do nothing"
    override fun execute(context: Context) = Unit
}

