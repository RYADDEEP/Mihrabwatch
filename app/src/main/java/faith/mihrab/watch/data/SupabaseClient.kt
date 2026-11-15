package faith.mihrab.watch.data

import faith.mihrab.watch.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.CodeVerifierCache
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

val supabase: SupabaseClient by lazy {
    buildSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
}

/** The watch's own private identity on [supabase] — one per installed app (see [DeviceIdentity]). */
val deviceIdentity: DeviceIdentity by lazy { DeviceIdentity(supabase) }

/**
 * The parameters after [key] exist for the unit tests only (a faked server has no Android
 * Context); production passes none of them and gets the library's defaults.
 */
internal fun buildSupabaseClient(
    url: String,
    key: String,
    sessionManager: SessionManager? = null,
    codeVerifierCache: CodeVerifierCache? = null,
    lifecycleCallbacks: Boolean = true,
): SupabaseClient = createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
    // device_pairings rows carry 11+ columns (sync_payload JSONB, created_at, paired_at,
    // user_id, …) but the watch decodes them into narrow data classes. Without an explicit
    // lenient serializer this relies on an undocumented library default and silently fails
    // decodeSingle/decodeRecord. Mirrors SyncPayloadJson (SyncPayload.kt).
    defaultSerializer = KotlinXSerializer(
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        },
    )
    // ⭐ THE WATCH IS SIGNED IN AS ITS OWN ANONYMOUS IDENTITY (the pairing lock).
    // The session (access + refresh token) is kept by the library's own storage — the app's
    // private SharedPreferences — loaded on every launch (autoLoadFromStorage) and refreshed
    // while the app runs (alwaysAutoRefresh). The database stamps the pending record with this
    // identity, and only this identity can read it. DeviceIdentity decides WHEN to sign in.
    install(Auth) {
        sessionManager?.let { this.sessionManager = it }
        codeVerifierCache?.let { this.codeVerifierCache = it }
        enableLifecycleCallbacks = lifecycleCallbacks
    }
    install(Postgrest)
    // ⛔ REALTIME TAKES THE SESSION'S TOKEN — NEVER A FIXED ONE.
    // The channel join resolves its access_token through
    // `resolveAccessToken(realtime, keyAsFallback = false)`, whose order is
    // jwtToken → client accessToken provider → the Auth plugin's session → (supabaseKey, disabled).
    // Before the lock this client had no Auth plugin, so b1b19f4 set jwtToken to the public key
    // (without it the join went out with NO token and was silent). With Auth installed that line
    // would now BEAT the session: the watch would listen as nobody, and after the lock nobody
    // reads anything. So no jwtToken: the join carries the session's token, and Realtime follows
    // every refresh by itself (RealtimeImpl collects Auth.sessionStatus and calls setAuth).
    install(Realtime)
}
