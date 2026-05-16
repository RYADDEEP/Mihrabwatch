package faith.mihrab.watch.data

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import faith.mihrab.watch.services.MihrabComplicationDataSourceService
import faith.mihrab.watch.services.MihrabTileService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Read-only consumer of the phone-authoritative `device_pairings.sync_payload`.
 *
 * Pattern mirrors [PairingRepository.observePairing]: an initial Postgrest SELECT to bootstrap,
 * then a Supabase Realtime channel filtered to this device's pairing row. The watch never
 * writes back (schema decision #11).
 *
 * On every valid payload the cache is updated and the out-of-app surfaces (Tile, Complication)
 * are pushed a refresh — they read solely from [SyncPayloadCache] since they run in separate
 * processes.
 */
class SyncPayloadRepository(
    private val context: Context,
    private val cache: SyncPayloadCache,
) {
    @Serializable
    private data class SyncPayloadRow(
        @SerialName("sync_payload") val syncPayload: JsonElement? = null,
    )

    fun observe(pairingId: String): Flow<SyncPayloadState> = flow {
        var lastGood: SyncPayload? = null

        emit(SyncPayloadState.Loading)

        // Warm cold-start: surface the last-known-good immediately while the network read runs.
        cache.load()?.let {
            lastGood = it
            emit(SyncPayloadState.Ready(it))
        }

        // Initial bootstrap read.
        val initial = runCatching {
            supabase.from("device_pairings")
                .select(Columns.list("sync_payload")) {
                    filter { eq("id", pairingId) }
                }
                .decodeSingle<SyncPayloadRow>()
                .syncPayload
        }
        val initialState = initial.fold(
            onSuccess = { parse(it, lastGood) },
            onFailure = { SyncPayloadState.Error(it, lastGood) },
        )
        lastGood = applySideEffects(initialState, lastGood)
        emit(initialState)

        // Realtime subscription — re-parse on every UPDATE to this pairing row.
        val channel = supabase.channel("sync:$pairingId")
        val updates = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "device_pairings"
            filter("id", FilterOperator.EQ, pairingId)
        }
        channel.subscribe()
        try {
            updates.collect { action ->
                val element = runCatching {
                    action.decodeRecord<SyncPayloadRow>().syncPayload
                }.getOrElse { action.record["sync_payload"] }
                val state = parse(element, lastGood)
                lastGood = applySideEffects(state, lastGood)
                emit(state)
            }
        } finally {
            supabase.realtime.removeChannel(channel)
        }
    }

    private fun parse(element: JsonElement?, lastGood: SyncPayload?): SyncPayloadState {
        if (element == null || element is JsonNull) return SyncPayloadState.Empty
        return try {
            val payload = SyncPayloadJson.decodeFromJsonElement(SyncPayload.serializer(), element)
            val version = payload.schemaVersion
            if (version != null && version > SUPPORTED_SCHEMA_VERSION) {
                SyncPayloadState.UnsupportedVersion(version, payload)
            } else {
                SyncPayloadState.Ready(payload)
            }
        } catch (t: Throwable) {
            SyncPayloadState.Error(t, lastGood)
        }
    }

    /**
     * Persists a fresh [SyncPayloadState.Ready] and pushes the out-of-app surfaces. Returns the
     * updated last-known-good so the caller can keep showing it on a later parse failure.
     */
    private suspend fun applySideEffects(
        state: SyncPayloadState,
        lastGood: SyncPayload?,
    ): SyncPayload? {
        if (state !is SyncPayloadState.Ready) return lastGood
        cache.save(state.payload)
        runCatching {
            TileService.getUpdater(context).requestUpdate(MihrabTileService::class.java)
            ComplicationDataSourceUpdateRequester.create(
                context,
                ComponentName(context, MihrabComplicationDataSourceService::class.java),
            ).requestUpdateAll()
        }
        return state.payload
    }
}

/**
 * Render states the watch UI maps to (schema §8). [SyncPayloadState.Error] retains the last
 * valid payload so consumers can show last-known-good with a stale indicator.
 */
sealed interface SyncPayloadState {
    data object Loading : SyncPayloadState
    data object Empty : SyncPayloadState
    data class Ready(val payload: SyncPayload) : SyncPayloadState
    /**
     * `schema_version` is newer than this build understands. [payload] is the best-effort
     * lenient parse so consumers can still degrade gracefully (schema §5/§8.C) — e.g. Qibla
     * keeps rendering the bearing while Prayer Home shows an "update" prompt.
     */
    data class UnsupportedVersion(
        val version: Int,
        val payload: SyncPayload? = null,
    ) : SyncPayloadState
    data class Error(val throwable: Throwable, val lastGood: SyncPayload?) : SyncPayloadState
}
