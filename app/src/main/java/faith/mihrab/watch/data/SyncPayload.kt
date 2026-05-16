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
    @SerialName("last_updated") val lastUpdated: String? = null,
    val date: String? = null,
    val timezone: String? = null,
    val locale: String? = null,
    val location: SyncLocation? = null,
    val calculation: SyncCalculation? = null,
    val prayers: List<SyncPrayer>? = null,
    @SerialName("next_prayer") val nextPrayer: SyncNextPrayer? = null,
    val qibla: SyncQibla? = null,
)

@Serializable
data class SyncLocation(
    @SerialName("display_name") val displayName: String? = null,
)

@Serializable
data class SyncCalculation(
    val method: String? = null,
    val juristic: String? = null,
)

@Serializable
data class SyncPrayer(
    val name: String,
    val time: String,
)

@Serializable
data class SyncNextPrayer(
    val name: String? = null,
    val time: String? = null,
)

@Serializable
data class SyncQibla(
    @SerialName("bearing_degrees") val bearingDegrees: Int? = null,
    val cardinal: String? = null,
)
