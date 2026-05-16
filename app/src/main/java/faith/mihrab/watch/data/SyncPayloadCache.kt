package faith.mihrab.watch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.syncPayloadPreferences: DataStore<Preferences> by
    preferencesDataStore(name = "sync_payload")

/**
 * Durable last-known-good payload store. Backs two needs:
 *  1. Warm cold-start UX for the main app (emit cached payload while the network read runs).
 *  2. The only data source for the Tile + Complication, which run in separate processes and
 *     cannot share in-memory state with the app or repository.
 */
class SyncPayloadCache(private val context: Context) {
    private val payloadKey = stringPreferencesKey("last_payload_json")

    val flow: Flow<SyncPayload?> = context.syncPayloadPreferences.data.map { prefs ->
        prefs[payloadKey]?.let { decode(it) }
    }

    suspend fun load(): SyncPayload? = flow.first()

    suspend fun save(payload: SyncPayload) {
        val json = runCatching {
            SyncPayloadJson.encodeToString(SyncPayload.serializer(), payload)
        }.getOrNull() ?: return
        context.syncPayloadPreferences.edit { prefs ->
            prefs[payloadKey] = json
        }
    }

    private fun decode(json: String): SyncPayload? =
        runCatching {
            SyncPayloadJson.decodeFromString(SyncPayload.serializer(), json)
        }.getOrNull()
}
