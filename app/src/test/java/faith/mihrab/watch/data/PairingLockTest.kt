package faith.mihrab.watch.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * THE PROOF of the pairing lock, Wear OS side, against a faked Supabase (a real local HTTP +
 * websocket server): the watch signs in as its own anonymous identity BEFORE its pending insert,
 * keeps it across launches, and every device_pairings call — the insert, the expires_at
 * correction, both reads and both Realtime joins — carries that identity's session.
 */
@OptIn(ExperimentalTime::class)
class PairingLockTest {
    private val server = FakeSupabaseServer()
    private val clients = mutableListOf<SupabaseClient>()

    @After
    fun tearDown() = runBlocking {
        clients.forEach { runCatching { it.close() } }
        server.close()
    }

    /** One "launch" of the watch app: a fresh client over the session storage it was given. */
    private fun launch(storage: MemorySessionManager = MemorySessionManager()): Launch {
        val client = server.client(storage).also { clients += it }
        val identity = DeviceIdentity(client)
        return Launch(client, identity, PairingRepository(client, identity), SyncPayloadSource(client, identity))
    }

    private data class Launch(
        val client: SupabaseClient,
        val identity: DeviceIdentity,
        val pairing: PairingRepository,
        val sync: SyncPayloadSource,
    ) {
        val bearer: String get() = "Bearer ${client.auth.currentAccessTokenOrNull()}"
    }

    private fun test(block: suspend CoroutineScope.() -> Unit) = runBlocking { withTimeout(30_000) { block() } }

    @Test
    fun signIn_happensBeforeThePendingInsert_andTheInsertIsTodaysFourFields() = test {
        val watch = launch()
        watch.pairing.createPairing()

        val order = server.calls.map { "${it.method} ${it.path.substringBefore('?')}" }
        val signUp = order.indexOf("POST /auth/v1/signup")
        val insert = order.indexOf("POST /rest/v1/device_pairings")
        assertTrue("sign-in must happen: $order", signUp >= 0)
        assertTrue("sign-in must come before the insert: $order", signUp < insert)
        assertEquals(1, server.count("/auth/v1/signup"))

        // PostgREST bulk shape: one row in an array.
        val sent = Json.parseToJsonElement(server.calls[insert].body)
        val body = (if (sent is JsonArray) sent.single() else sent).jsonObject
        assertEquals(setOf("pairing_code", "status", "paired_device_type", "expires_at"), body.keys)
        assertEquals("pending", body["status"]!!.jsonPrimitive.content)
        assertEquals("watch", body["paired_device_type"]!!.jsonPrimitive.content)
    }

    @Test
    fun secondLaunch_restoresTheSameIdentity_andDoesNotSignInAgain() = test {
        val storage = MemorySessionManager()
        val first = launch(storage)
        first.pairing.createPairing()
        val firstUser = first.client.auth.currentUserOrNull()?.id
        assertNotNull(firstUser)
        assertEquals(1, server.count("/auth/v1/signup"))

        val second = launch(storage)
        second.pairing.fetchPairing(server.pairingId)

        assertEquals("no second sign-in", 1, server.count("/auth/v1/signup"))
        assertEquals(firstUser, second.client.auth.currentUserOrNull()?.id)
        assertEquals(first.bearer, second.bearer)
        assertEquals(second.bearer, server.calls.last { it.path.startsWith("/rest/v1") }.authorization)
    }

    @Test
    fun theInsert_theCorrection_andBothReads_carryTheSession() = test {
        val watch = launch()
        val row = watch.pairing.createPairing()
        assertEquals("the expires_at correction ran", "2026-11-16T01:05:00Z", row.expiresAt)
        watch.pairing.fetchPairing(server.pairingId)
        val payload = watch.sync.read(server.pairingId)
        assertNotNull(payload)

        val rest = server.calls.filter { it.path.startsWith("/rest/v1/device_pairings") }
        assertEquals(listOf("POST", "PATCH", "GET", "GET"), rest.map { it.method })
        assertTrue(rest[1].path.contains("id=eq.${server.pairingId}"))
        assertTrue(rest[3].path.contains("select=sync_payload"))
        val bearer = watch.bearer
        assertFalse("a session token, never the public key", bearer.endsWith(FAKE_KEY))
        rest.forEach { assertEquals("${it.method} ${it.path}", bearer, it.authorization) }
    }

    @Test
    fun bothRealtimeSubscriptions_joinWithTheSessionToken_andReceiveTheUpdate() = test {
        val watch = launch()
        watch.identity.ensureSignedIn()
        val token = watch.client.auth.currentAccessTokenOrNull()

        val claimed = async(Dispatchers.IO) { watch.pairing.observePairing(server.pairingId).first() }
        val synced = async(Dispatchers.IO) { watch.sync.updates(server.pairingId).first() }
        while (server.joins.size < 2) delay(20)

        val joins = server.joins.associate { it.topic to it.accessToken }
        assertEquals(token, joins["realtime:pairing:${server.pairingId}"])
        assertEquals(token, joins["realtime:sync:${server.pairingId}"])

        server.pushUpdate(server.row("paired", "user-1"))
        val row = claimed.await()
        assertEquals("paired", row.status)
        assertEquals("user-1", row.pairedUserId)
        val element = synced.await()
        assertEquals(13.7563, element!!.jsonObject["location"]!!.jsonObject["latitude"]!!.jsonPrimitive.content.toDouble(), 0.0)
    }

    @Test
    fun aFailedSignIn_throws_andNoInsertReachesTheServer() = test {
        server.signUpFails = true
        val watch = launch()
        try {
            watch.pairing.createPairing()
            fail("createPairing must throw — PairingScreen maps any throw to today's Error state")
        } catch (expected: Exception) {
            // PairingScreen.kt (unchanged): catch → state = PairingState.Error
        }
        assertEquals(1, server.count("/auth/v1/signup"))
        assertEquals("nothing reached device_pairings", 0, server.count("/rest/v1"))
        // and the sync read fails the same way → SyncPayloadRepository's existing Error(lastGood)
        assertTrue(runCatching { watch.sync.read(server.pairingId) }.isFailure)
        assertEquals(0, server.count("/rest/v1"))
    }

    @Test
    fun fourCallersAtOnce_signInOnlyOnce() = test {
        val watch = launch()
        (1..4).map {
            async(Dispatchers.IO) { watch.pairing.fetchPairing(server.pairingId) }
        }.awaitAll()
        assertEquals(1, server.count("/auth/v1/signup"))
        val bearers = server.calls.filter { it.path.startsWith("/rest/v1") }.map { it.authorization }.toSet()
        assertEquals(setOf(watch.bearer), bearers)
    }

    @Test
    fun aStoredSessionTheServerRejects_isReplacedByANewSignIn() = test {
        server.refresh = FakeSupabaseServer.RefreshAnswer.REJECTED
        val watch = launch(MemorySessionManager(expiredStoredSession()))
        watch.pairing.fetchPairing(server.pairingId)

        assertEquals(1, server.count("/auth/v1/token"))
        assertEquals("cannot be restored → signs in", 1, server.count("/auth/v1/signup"))
        assertFalse(watch.client.auth.currentUserOrNull()?.id == FakeSupabaseServer.STORED_USER)
    }

    @Test
    fun aStoredSessionThatCannotRefreshOffline_isKept_andNoNewIdentityIsMinted() = test {
        server.refresh = FakeSupabaseServer.RefreshAnswer.OFFLINE
        val watch = launch(MemorySessionManager(expiredStoredSession()))
        val result = runCatching { watch.pairing.fetchPairing(server.pairingId) }

        assertTrue(result.exceptionOrNull() is DeviceIdentityUnavailable)
        assertEquals("never a new identity over the stored one", 0, server.count("/auth/v1/signup"))
        assertEquals(0, server.count("/rest/v1"))
    }

    @Test
    fun aStoredSessionThatRefreshes_keepsTheSameIdentity() = test {
        val watch = launch(MemorySessionManager(expiredStoredSession()))
        watch.pairing.fetchPairing(server.pairingId)

        assertEquals(0, server.count("/auth/v1/signup"))
        assertEquals(FakeSupabaseServer.STORED_USER, watch.client.auth.currentUserOrNull()?.id)
    }

    private fun expiredStoredSession() = UserSession(
        accessToken = "access-stored",
        refreshToken = "refresh-stored",
        expiresIn = 3600,
        tokenType = "bearer",
        user = UserInfo(aud = "authenticated", id = FakeSupabaseServer.STORED_USER),
        expiresAt = Clock.System.now() - 2.hours,
    )
}
