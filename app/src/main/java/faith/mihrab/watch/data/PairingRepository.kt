package faith.mihrab.watch.data

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.security.SecureRandom
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

private const val PAIRING_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
private const val PAIRING_CODE_LENGTH = 8
private const val PAIRING_EXPIRY_MINUTES = 5L
private const val TAG = "Pairing"

@Serializable
data class PairingRow(
    val id: String,
    @SerialName("pairing_code") val pairingCode: String,
    val status: String,
    @SerialName("paired_device_type") val pairedDeviceType: String,
    @SerialName("paired_user_id") val pairedUserId: String? = null,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
private data class PairingInsert(
    @SerialName("pairing_code") val pairingCode: String,
    val status: String,
    @SerialName("paired_device_type") val pairedDeviceType: String,
    @SerialName("expires_at") val expiresAt: String,
)

class PairingRepository {
    private val random = SecureRandom()

    fun generateCode(): String = buildString(PAIRING_CODE_LENGTH) {
        repeat(PAIRING_CODE_LENGTH) {
            append(PAIRING_CODE_CHARSET[random.nextInt(PAIRING_CODE_CHARSET.length)])
        }
    }

    /**
     * Inserts a pending pairing row, then corrects `expires_at` to be relative to the Supabase
     * server clock rather than the device wall clock. On emulators / un-synced watches the wall
     * clock can drift hours behind real time, so a client-computed `expires_at` makes the row
     * born already expired and the phone's `expires_at > NOW()` claim filter rejects it. If the
     * corrective UPDATE fails, the row simply keeps the client value — i.e. the prior behaviour,
     * no regression.
     */
    suspend fun createPairing(): PairingRow {
        Log.d(TAG, "createPairing: entry")
        val code = generateCode()
        Log.d(TAG, "createPairing: code generated code=$code")
        val clientExpiresAt = Instant.now()
            .plus(PAIRING_EXPIRY_MINUTES, ChronoUnit.MINUTES)
            .toString()
        val payload = PairingInsert(
            pairingCode = code,
            status = "pending",
            pairedDeviceType = "watch",
            expiresAt = clientExpiresAt,
        )
        Log.d(TAG, "createPairing: INSERT request expires_at_client=$clientExpiresAt")
        val inserted = supabase
            .from("device_pairings")
            .insert(payload) { select() }
            .decodeSingle<PairingRow>()
        Log.d(
            TAG,
            "createPairing: INSERT response id=${inserted.id} created_at_server=${inserted.createdAt}",
        )

        val serverCreated = inserted.createdAt?.let { parseServerInstant(it) }
        if (serverCreated == null) {
            Log.d(
                TAG,
                "createPairing: server created_at unavailable (createdAt=${inserted.createdAt}) " +
                    "— keeping client expires_at (no regression)",
            )
            return inserted
        }
        val correctExpiresAt = serverCreated
            .plus(PAIRING_EXPIRY_MINUTES, ChronoUnit.MINUTES)
            .toString()
        val updateResult = runCatching {
            supabase.from("device_pairings").update({ set("expires_at", correctExpiresAt) }) {
                filter { eq("id", inserted.id) }
            }
        }
        return if (updateResult.isSuccess) {
            Log.d(
                TAG,
                "createPairing: UPDATE expires_at_corrected=$correctExpiresAt " +
                    "= server_created_at + ${PAIRING_EXPIRY_MINUTES}min",
            )
            inserted.copy(expiresAt = correctExpiresAt)
        } else {
            Log.d(
                TAG,
                "createPairing: UPDATE expires_at FAILED " +
                    "error=${updateResult.exceptionOrNull()?.message} " +
                    "— row keeps client value (no regression)",
            )
            inserted
        }
    }

    fun observePairing(pairingId: String): Flow<PairingRow> = flow {
        val channel = supabase.channel("pairing:$pairingId")
        val updates = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "device_pairings"
            filter("id", FilterOperator.EQ, pairingId)
        }
        Log.d(TAG, "observePairing: subscribe channel=pairing:$pairingId filter=id=eq.$pairingId")
        channel.subscribe()
        try {
            updates.collect { action ->
                Log.d(TAG, "observePairing: event received type=UPDATE pairing_id=$pairingId")
                val typed = runCatching { action.decodeRecord<PairingRow>() }.getOrNull()
                val row = typed ?: run {
                    Log.d(
                        TAG,
                        "observePairing: typed decode failed, trying raw fallback " +
                            "record_keys=${action.record.keys}",
                    )
                    decodeFallback(action.record)
                }
                if (row == null) {
                    Log.d(
                        TAG,
                        "observePairing: decode FAILURE (typed+fallback) " +
                            "record_keys=${action.record.keys}",
                    )
                } else {
                    Log.d(
                        TAG,
                        "observePairing: decode SUCCESS status=${row.status} " +
                            "paired_user_id=${row.pairedUserId}",
                    )
                    emit(row)
                }
            }
        } catch (t: Throwable) {
            if (t !is CancellationException) {
                Log.d(TAG, "observePairing: subscription error=${t.message}")
            }
            throw t
        } finally {
            Log.d(TAG, "observePairing: subscription torn down pairing_id=$pairingId")
            supabase.realtime.removeChannel(channel)
        }
    }

    private fun parseServerInstant(value: String): Instant? = runCatching {
        OffsetDateTime.parse(value).toInstant()
    }.recoverCatching {
        Instant.parse(value)
    }.getOrNull()

    private fun decodeFallback(record: JsonObject): PairingRow? {
        fun field(key: String): String? = (record[key] as? JsonPrimitive)?.contentOrNull
        val id = field("id") ?: return null
        val pairingCode = field("pairing_code") ?: return null
        val status = field("status") ?: return null
        val pairedDeviceType = field("paired_device_type") ?: return null
        val expiresAt = field("expires_at") ?: return null
        return PairingRow(
            id = id,
            pairingCode = pairingCode,
            status = status,
            pairedDeviceType = pairedDeviceType,
            pairedUserId = field("paired_user_id"),
            expiresAt = expiresAt,
            createdAt = field("created_at"),
        )
    }
}
