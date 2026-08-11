package faith.mihrab.watch.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Lays [content] out so its bounding box is inscribed in a circle of [radius] — the clear
 * circle inside the Prayer Home ring.
 *
 * This is what makes "nothing may cross the stroke" true by construction rather than by
 * arithmetic. A fixed width cannot hold that promise: the block's height moves with the
 * wearer's font scale, and its width moves with the language, so the widest a block may be
 * depends on how tall it turned out to be.
 *
 * Two passes:
 *  1. measure at the full diameter to learn the block's natural height `h`
 *  2. re-measure constrained to the corner chord `2·√(r² − (h/2)²)`
 *
 * One extra pass is enough because every line inside is single-line and auto-sized: narrowing
 * can only shrink a line's font, which shrinks the height, which would only ever widen the
 * chord. The second measure is therefore conservative, never optimistic.
 *
 * If the block is taller than the circle even at the type floor, the chord is clamped rather
 * than collapsing to zero — the overflow becomes visible in a preview instead of silently
 * landing on the gold.
 */
@Composable
fun InscribedColumn(
    radius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { _ ->
        val r = radius.toPx()
        val diameter = (r * 2f).roundToInt().coerceAtLeast(0)
        val full = Constraints(maxWidth = diameter, maxHeight = diameter)

        val probe = subcompose(Slot.Probe, content).firstOrNull()?.measure(full)
        val halfHeight = ((probe?.height ?: 0) / 2f).coerceAtMost(r * MAX_HALF_HEIGHT_RATIO)
        val chord = 2f * sqrt((r * r - halfHeight * halfHeight).coerceAtLeast(0f))
        val width = chord.roundToInt().coerceIn(0, diameter)

        val placeable = subcompose(Slot.Final, content).firstOrNull()
            ?.measure(Constraints(maxWidth = width, maxHeight = diameter))

        val w = placeable?.width ?: 0
        val h = placeable?.height ?: 0
        layout(w, h) { placeable?.place(0, 0) }
    }
}

/** Keeps the chord positive when a pathological font scale makes the block taller than the circle. */
private const val MAX_HALF_HEIGHT_RATIO = 0.9f

private enum class Slot { Probe, Final }
