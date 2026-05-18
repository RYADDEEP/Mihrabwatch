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
        install(Realtime)
    }
}
