package faith.mihrab.watch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pairingPreferences: DataStore<Preferences> by preferencesDataStore(name = "pairing")

data class PairingCredentials(val pairingId: String, val pairedUserId: String)

class PairingDataStore(private val context: Context) {
    private val pairingIdKey = stringPreferencesKey("pairing_id")
    private val pairedUserIdKey = stringPreferencesKey("paired_user_id")

    val credentialsFlow: Flow<PairingCredentials?> = context.pairingPreferences.data.map { prefs ->
        val id = prefs[pairingIdKey]
        val user = prefs[pairedUserIdKey]
        if (id != null && user != null) PairingCredentials(id, user) else null
    }

    suspend fun savePairing(pairingId: String, pairedUserId: String) {
        context.pairingPreferences.edit { prefs ->
            prefs[pairingIdKey] = pairingId
            prefs[pairedUserIdKey] = pairedUserId
        }
    }
}
