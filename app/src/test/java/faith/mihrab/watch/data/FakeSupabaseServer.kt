package faith.mihrab.watch.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.SessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import java.util.Collections
import java.util.UUID

const val FAKE_KEY = "test-publishable-key"

/**
 * A real local HTTP + websocket server standing in for Supabase: Auth (/auth/v1), PostgREST
 * (/rest/v1/device_pairings) and Realtime (/realtime/v1/websocket). It records every request in
 * order with its Authorization header, and every Realtime join with its access_token.
 */
class FakeSupabaseServer : AutoCloseable {
    data class Call(val method: String, val path: String, val authorization: String?, val body: String)
    data class Join(val topic: String, val accessToken: String?)

    val calls: MutableList<Call> = Collections.synchronizedList(mutableListOf())
    val joins: MutableList<Join> = Collections.synchronizedList(mutableListOf())

    /** What /auth/v1/signup and /auth/v1/token answer. */
    var signUpFails = false
    var refresh: RefreshAnswer = RefreshAnswer.OK

    enum class RefreshAnswer { OK, REJECTED, OFFLINE }

    val pairingId = "11111111-2222-3333-4444-555555555555"
    val createdAt = "2026-11-16T01:00:00+00:00"
    private var tokenCounter = 0

    @Volatile private var socket: WebSocket? = null
    private val server = MockWebServer()

    init {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                calls += Call(
                    method = request.method.orEmpty(),
                    path = path,
                    authorization = request.getHeader("Authorization"),
                    body = request.body.readUtf8(),
                )
                return when {
                    path.startsWith("/realtime/v1/websocket") ->
                        MockResponse().withWebSocketUpgrade(realtimeListener)
                    path.startsWith("/auth/v1/signup") ->
                        if (signUpFails) {
                            json(422, """{"code":422,"error_code":"anonymous_provider_disabled","msg":"Anonymous sign-ins are disabled"}""")
                        } else {
                            json(200, session(UUID.randomUUID().toString()))
                        }
                    path.startsWith("/auth/v1/token") -> when (refresh) {
                        RefreshAnswer.OK -> json(200, session(STORED_USER))
                        RefreshAnswer.REJECTED ->
                            json(400, """{"code":400,"error_code":"refresh_token_not_found","msg":"Invalid Refresh Token: Refresh Token Not Found"}""")
                        RefreshAnswer.OFFLINE -> MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                    }
                    path.startsWith("/rest/v1/device_pairings") -> when (request.method) {
                        "POST" -> json(201, "[${row("pending", null)}]")
                        "PATCH" -> MockResponse().setResponseCode(204)
                        else -> json(200, if (path.contains("select=sync_payload")) {
                            """[{"sync_payload":$PAYLOAD}]"""
                        } else {
                            "[${row("pending", null)}]"
                        })
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
    }

    val url: String get() = server.url("/").toString().trimEnd('/')

    fun client(sessionManager: SessionManager): SupabaseClient = buildSupabaseClient(
        url = url,
        key = FAKE_KEY,
        sessionManager = sessionManager,
        codeVerifierCache = MemoryCodeVerifierCache(),
        lifecycleCallbacks = false,
    )

    fun count(prefix: String, method: String? = null) =
        synchronized(calls) { calls.count { it.path.startsWith(prefix) && (method == null || it.method == method) } }

    /** Pushes a postgres UPDATE of the row to every joined channel. */
    fun pushUpdate(record: String) {
        val joined = synchronized(joins) { joins.map { it.topic } }
        joined.forEach { topic ->
            socket?.send(
                """{"topic":"$topic","event":"postgres_changes","ref":null,"payload":{"ids":[1],"data":{""" +
                    """"type":"UPDATE","schema":"public","table":"device_pairings",""" +
                    """"commit_timestamp":"2026-11-16T01:01:00Z","columns":[],"errors":null,""" +
                    """"record":$record,"old_record":{"id":"$pairingId"}}}}""",
            )
        }
    }

    fun row(status: String, pairedUserId: String?): String =
        """{"id":"$pairingId","pairing_code":"A3BK9M2X","status":"$status",""" +
            """"paired_device_type":"watch","paired_user_id":${pairedUserId?.let { "\"$it\"" } ?: "null"},""" +
            """"expires_at":"2026-11-16T01:05:00+00:00","created_at":"$createdAt","sync_payload":$PAYLOAD}"""

    private val realtimeListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            socket = webSocket
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val message = Json.parseToJsonElement(text).jsonObject
            val event = message["event"]?.jsonPrimitive?.content
            val topic = message["topic"]?.jsonPrimitive?.content ?: return
            val ref = message["ref"]?.jsonPrimitive?.content
            if (event == "phx_join") {
                val payload = message["payload"] as JsonObject
                joins += Join(topic, payload["access_token"]?.jsonPrimitive?.content)
                webSocket.send(
                    """{"topic":"$topic","event":"phx_reply","ref":"$ref","payload":{"status":"ok",""" +
                        """"response":{"postgres_changes":[{"id":1,"event":"UPDATE","schema":"public",""" +
                        """"table":"device_pairings","filter":"id=eq.$pairingId"}]}}}""",
                )
            } else if (ref != null) {
                webSocket.send("""{"topic":"$topic","event":"phx_reply","ref":"$ref","payload":{"status":"ok","response":{}}}""")
            }
        }
    }

    @Synchronized
    private fun session(userId: String): String {
        tokenCounter += 1
        return """{"access_token":"access-$userId-$tokenCounter","token_type":"bearer","expires_in":3600,""" +
            """"refresh_token":"refresh-$tokenCounter","user":{"id":"$userId","aud":"authenticated",""" +
            """"role":"authenticated","is_anonymous":true,"app_metadata":{},"user_metadata":{}}}"""
    }

    private fun json(code: Int, body: String) =
        MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)

    override fun close() {
        socket?.close(1000, null)
        server.shutdown()
    }

    companion object {
        const val STORED_USER = "99999999-0000-0000-0000-000000000001"
        const val PAYLOAD =
            """{"schema_version":1,"location":{"latitude":13.7563,"longitude":100.5018,"display_name":"Bangkok"},""" +
                """"calculation":{"method":"MWL","juristic":"Shafi"},"timezone":"Asia/Bangkok"}"""
    }
}
