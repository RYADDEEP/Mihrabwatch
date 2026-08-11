package faith.mihrab.watch.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Screen-relative dimension scale.
 *
 * Every size in the app is a ratio of **D**, the container's minimum dimension. The ratios are
 * the design's 454px values divided by 227dp — the proportions that were already correct on a
 * large round watch, made portable to a small one.
 *
 * Reference D values: 192dp (384px), 225dp (450px), 227dp (454px), all at 320dpi.
 *
 * Type is expressed in `sp`, so the wearer's font scale still applies on top. The fit is not
 * guaranteed by these numbers alone — [faith.mihrab.watch.ui.components.InscribedColumn] and
 * per-line auto-sizing absorb font scale and translation width.
 */
@Immutable
data class WatchScale(val d: Dp) {

    // --- Prayer Home ring ---------------------------------------------------
    val ringDiameter: Dp = d * RING_DIAMETER
    val ringStroke: Dp = d * RING_STROKE

    /** Radius of the clear circle inside the ring. Nothing may cross the stroke. */
    val ringClearRadius: Dp = (ringDiameter - ringStroke * 2f) / 2f

    // --- Qibla compass ------------------------------------------------------
    val compassDiameter: Dp = d * COMPASS_DIAMETER
    val compassStroke: Dp = d * COMPASS_STROKE

    // --- Prayer Home type ---------------------------------------------------
    val timeSize: TextUnit = d.toSp(TIME_SIZE)
    val timeSizeMin: TextUnit = d.toSp(TIME_SIZE_MIN)
    val nameSize: TextUnit = d.toSp(NAME_SIZE)
    val countdownSize: TextUnit = d.toSp(COUNTDOWN_SIZE)
    val timeToName: Dp = d * TIME_TO_NAME
    val nameToCountdown: Dp = d * NAME_TO_COUNTDOWN

    /** Edge-state and message copy — same optical size as the countdown. */
    val messageSize: TextUnit = countdownSize

    // --- Pairing ------------------------------------------------------------
    val codeTargetWidth: Dp = d * CODE_TARGET_WIDTH
    val codeToExpiry: Dp = d * CODE_TO_EXPIRY
    val expirySize: TextUnit = d.toSp(EXPIRY_SIZE)
    val connectingSize: TextUnit = d.toSp(CONNECTING_SIZE)
    val errorTitleSize: TextUnit = d.toSp(ERROR_TITLE_SIZE)

    /** One line of expiry copy, reserved so the code never jumps when the caption empties. */
    val expiryLineHeight: Dp = (expirySize.value * LINE_HEIGHT_FACTOR).dp

    /** Inset for screens whose content is text rather than a circle. */
    val screenPadding: Dp = d * SCREEN_PADDING

    private fun Dp.toSp(ratio: Float): TextUnit = maxOf(value * ratio, FLOOR_SP).sp

    companion object {
        /**
         * The absolute type floor (design guide §2.3). Any ratio that would resolve below this
         * stops here — which is what lets the scale shrink safely to a 384px screen.
         */
        const val FLOOR_SP: Float = 13f

        private const val RING_DIAMETER = 0.90f
        private const val RING_STROKE = 0.045f
        private const val COMPASS_DIAMETER = 0.74f
        private const val COMPASS_STROKE = 0.014f
        private const val TIME_SIZE = 0.21f
        private const val TIME_SIZE_MIN = 0.16f
        private const val NAME_SIZE = 0.105f
        private const val COUNTDOWN_SIZE = 0.075f
        private const val TIME_TO_NAME = 0.035f
        private const val NAME_TO_COUNTDOWN = 0.018f
        private const val CODE_TARGET_WIDTH = 0.78f
        private const val CODE_TO_EXPIRY = 0.09f
        private const val EXPIRY_SIZE = 0.058f
        private const val CONNECTING_SIZE = 0.062f
        private const val ERROR_TITLE_SIZE = 0.0705f
        private const val SCREEN_PADDING = 0.106f
        private const val LINE_HEIGHT_FACTOR = 1.4f

        /** D is the container's minimum dimension — a round face is square, but be explicit. */
        fun from(maxWidth: Dp, maxHeight: Dp): WatchScale =
            WatchScale(min(maxWidth.value, maxHeight.value).dp)
    }
}
