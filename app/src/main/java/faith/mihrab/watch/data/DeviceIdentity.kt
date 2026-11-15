package faith.mihrab.watch.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "Pairing"

/** The stored identity exists but cannot be refreshed right now (no network). */
class DeviceIdentityUnavailable : IllegalStateException(
    "stored identity cannot be refreshed now; a new one would orphan this watch's record",
)

/**
 * The watch's own anonymous identity — ONE per installed app, never one per run.
 *
 * Every call that touches `device_pairings` (the pending insert, the expires_at correction, the
 * reads, both Realtime channels) calls [ensureSignedIn] FIRST, so it goes out as this identity.
 *
 *  - a stored session that loads (and refreshes) → that same identity, no sign-in;
 *  - no stored session, or one the server rejects → an anonymous sign-in, saved by the library;
 *  - a stored session that cannot be refreshed now (offline) → [DeviceIdentityUnavailable].
 *    Never a new identity over it: the record this watch waits on, or reads, belongs to the old
 *    one. The callers already turn a throw into today's error state.
 *
 * Single-flight: the pairing screen starts its resume read, the insert, the Realtime listener and
 * the poll close together; without the lock two of them could each mint an identity.
 */
class DeviceIdentity(private val client: SupabaseClient) {
    private val mutex = Mutex()

    suspend fun ensureSignedIn() = mutex.withLock {
        val auth = client.auth
        auth.awaitInitialization()
        when (val status = auth.sessionStatus.value) {
            is SessionStatus.Authenticated -> Unit
            is SessionStatus.RefreshFailure -> {
                Log.d(TAG, "DeviceIdentity: stored identity, refresh failing — not replacing it")
                throw DeviceIdentityUnavailable()
            }
            else -> {
                Log.d(TAG, "DeviceIdentity: no usable identity (${status::class.simpleName}) -> sign in")
                auth.signInAnonymously()
                Log.d(TAG, "DeviceIdentity: signed in user=${auth.currentUserOrNull()?.id}")
            }
        }
    }
}
