package edu.obya.c4.appl.workflow.filter

import edu.obya.c4.appl.workflow.Context
import edu.obya.c4.appl.workflow.Filter

object DoNothingFilter : Filter {
    override val name: String = "do nothing"
    override fun execute(context: Context) = Unit
}

