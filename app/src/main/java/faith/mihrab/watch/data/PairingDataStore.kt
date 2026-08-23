package faith.mihrab.watch.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.pairingPreferences: DataStore<Preferences> by preferencesDataStore(name = "pairing")

data class PairingCredentials(val pairingId: String, val pairedUserId: String)

class PairingDataStore(private val context: Context) {
    private val pairingIdKey = stringPreferencesKey("pairing_id")
    private val pairedUserIdKey = stringPreferencesKey("paired_user_id")

    // ⭐ THE ROW THE WATCH IS CURRENTLY WAITING ON, remembered across launches.
    // Without it every fresh composition minted a brand-new pairing row and
    // orphaned the previous one — including the one the phone had just claimed,
    // which is why re-opening the app never helped and why 47 paired rows piled
    // up that no watch ever consumed. Cleared the moment a pairing completes.
    private val pendingPairingIdKey = stringPreferencesKey("pending_pairing_id")

    val credentialsFlow: Flow<PairingCredentials?> = context.pairingPreferences.data.map { prefs ->
        val id = prefs[pairingIdKey]
        val user = prefs[pairedUserIdKey]
        Log.d(
            "Pairing",
            "PairingDataStore: read pairing_id=$id creds=${if (id != null && user != null) "present" else "null"}",
        )
        if (id != null && user != null) PairingCredentials(id, user) else null
    }

    /** The pending row this watch is waiting on, or null if there is none. */
    suspend fun readPending(): String? =
        context.pairingPreferences.data.first()[pendingPairingIdKey]

    suspend fun savePending(pairingId: String) {
        context.pairingPreferences.edit { it[pendingPairingIdKey] = pairingId }
        Log.d("Pairing", "PairingDataStore: pending pairing_id=$pairingId")
    }

    suspend fun clearPending() {
        context.pairingPreferences.edit { it.remove(pendingPairingIdKey) }
        Log.d("Pairing", "PairingDataStore: pending cleared")
    }

    suspend fun savePairing(pairingId: String, pairedUserId: String) {
        context.pairingPreferences.edit { prefs ->
            prefs[pairingIdKey] = pairingId
            prefs[pairedUserIdKey] = pairedUserId
        }
        Log.d(
            "Pairing",
            "PairingDataStore: write pairing_id=$pairingId paired_user_id=$pairedUserId",
        )
    }
}
