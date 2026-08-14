package faith.mihrab.watch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wear OS read-only mirror of the phone-authoritative `device_pairings.sync_payload` v1
 * schema. The watch never writes this — it only renders whatever the phone last pushed.
 * Schema contract: docs/MIHRAB_SYNC_PAYLOAD_SCHEMA.md (RATIFIED, May 15 2026).
 *
 * Every field is nullable: a malformed or partial payload must still deserialize so the UI
 * can degrade gracefully (schema §8.D). Schema-version gating happens in the repository,
 * not here.
 */
const val SUPPORTED_SCHEMA_VERSION = 1

/** Lenient, forward-compatible decoder — unknown future fields are ignored (schema §5). */
val SyncPayloadJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Serializable
data class SyncPayload(
    @SerialName("schema_version") val schemaVersion: Int? = null,
    // `date`, `last_updated` and `prayers` are deliberately absent. `date` and `last_updated`
    // existed only to drive the staleness caption, which is gone — and the ring is anchored to
    // the prayer window, never to `last_updated`. `prayers` was the full daily list, which no
    // surface reads now that the watch computes times locally. `ignoreUnknownKeys` means the
    // phone can keep sending all three with no contract break.
    val timezone: String? = null,
    val locale: String? = null,
    // schema v1.1 additive — paired-device UI locale chosen on the phone.
    // Resolved through LocaleManager.resolveTag with locale + device-default fallback.
    @SerialName("display_language") val displayLanguage: String? = null,
    val location: SyncLocation? = null,
    val calculation: SyncCalculation? = null,
    @SerialName("next_prayer") val nextPrayer: SyncNextPrayer? = null,
    val qibla: SyncQibla? = null,
)

@Serializable
data class SyncLocation(
    // `display_name` is deliberately absent: the location caption was removed from the UI.
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class SyncCalculation(
    val method: String? = null,
    val juristic: String? = null,
)

@Serializable
data class SyncNextPrayer(
    val name: String? = null,
    val time: String? = null,
)

@Serializable
data class SyncQibla(
    // `cardinal` is deliberately absent: its last consumer, bearingToCardinalName(), went with
    // the compass reduction in b576a4b. The ring carries no cardinal marks.
    @SerialName("bearing_degrees") val bearingDegrees: Int? = null,
)
