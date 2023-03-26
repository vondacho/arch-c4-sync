package com.edgelab.c4.domain.structurizr.strategy

import com.edgelab.c4.domain.strategy.PropertyScores
import com.edgelab.c4.domain.strategy.PropertyWeights
import com.edgelab.c4.domain.strategy.electValue
import com.edgelab.c4.util.notEmpty
import com.structurizr.view.Branding
import com.structurizr.view.Configuration
import com.structurizr.view.Styles

class MergeConfigurationStrategy {

    fun execute(
        master: Configuration,
        other: Configuration,
        scores: PropertyScores,
        weights: PropertyWeights
    ): (Configuration) -> Unit =

        { configuration ->
            master.branding.merge(other.branding, scores, weights)(configuration.branding)
            master.styles.merge(other.styles, scores, weights)(configuration.styles)
            "configuration.themes".electValue(master.themes, scores, other.themes, weights)
                ?.let { configuration.setThemes(*it) }
        }

    private fun Branding.merge(other: Branding, scores: PropertyScores, weights: PropertyWeights): (Branding) -> Unit =
        { branding ->
            branding.font = "configuration.branding.font".electValue(this.font, scores, other.font, weights)
            branding.logo = "configuration.branding.logo".electValue(this.logo, scores, other.logo, weights)
        }

    private fun Styles.merge(other: Styles, scores: PropertyScores, weights: PropertyWeights): (Styles) -> Unit =
        { styles ->
            "configuration.styles.elements".electValue(this.elements.notEmpty(), scores, other.elements.notEmpty(), weights)
                ?.onEach { styles.elements.add(it) }
            "configuration.styles.relationships".electValue(this.relationships.notEmpty(), scores, other.relationships.notEmpty(), weights)
                ?.onEach { styles.relationships.add(it) }
        }
}
