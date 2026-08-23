package faith.mihrab.watch.data

import faith.mihrab.watch.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

val supabase by lazy {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
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
        install(Postgrest)
        // ⛔ REALTIME NEEDS A TOKEN OF ITS OWN, AND WITHOUT ONE IT IS SILENT.
        // The channel join resolves its access_token through
        // `resolveAccessToken(realtime, keyAsFallback = false)`. That chain is
        // jwtToken → client accessToken provider → the Auth plugin → supabaseKey,
        // and the last step is DISABLED by keyAsFallback=false. This client has no
        // Auth plugin and had no jwtToken, so every step returned null and the join
        // went out with NO access_token at all — the socket connected, the channel
        // reported "ok", and not one postgres change was ever delivered.
        //
        // Measured: an independent websocket client sending access_token=<anon key>,
        // same table and same filter, received the UPDATE immediately. The server
        // was never the problem — REPLICA IDENTITY DEFAULT is sufficient here,
        // because the watch reads the NEW record and never the old one.
        //
        // Scoped to Realtime deliberately: Postgrest already authenticates fine
        // through the apikey header and is left exactly as it was.
        install(Realtime) {
            jwtToken = BuildConfig.SUPABASE_ANON_KEY
        }
    }
}
