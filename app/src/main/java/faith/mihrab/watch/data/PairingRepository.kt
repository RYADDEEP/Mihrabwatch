package faith.mihrab.watch.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val PAIRING_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
private const val PAIRING_CODE_LENGTH = 8
private const val PAIRING_EXPIRY_MINUTES = 5L

@Serializable
data class PairingRow(
    val id: String,
    @SerialName("pairing_code") val pairingCode: String,
    val status: String,
    @SerialName("paired_device_type") val pairedDeviceType: String,
    @SerialName("paired_user_id") val pairedUserId: String? = null,
    @SerialName("expires_at") val expiresAt: String,
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

    suspend fun createPairing(): PairingRow {
        val code = generateCode()
        val expiresAt = Instant.now()
            .plus(PAIRING_EXPIRY_MINUTES, ChronoUnit.MINUTES)
            .toString()
        val payload = PairingInsert(
            pairingCode = code,
            status = "pending",
            pairedDeviceType = "watch",
            expiresAt = expiresAt,
        )
        return supabase
            .from("device_pairings")
            .insert(payload) { select() }
            .decodeSingle()
    }

    fun observePairing(pairingId: String): Flow<PairingRow> = flow {
        val channel = supabase.channel("pairing:$pairingId")
        val updates = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "device_pairings"
            filter("id", FilterOperator.EQ, pairingId)
        }
        channel.subscribe()
        try {
            updates
                .mapNotNull { runCatching { it.decodeRecord<PairingRow>() }.getOrNull() }
                .collect { emit(it) }
        } finally {
            supabase.realtime.removeChannel(channel)
        }
    }
}
