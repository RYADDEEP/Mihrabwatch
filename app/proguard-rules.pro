# ProGuard/R8 rules for Mihrabwatch (release builds)
#
# STATUS: R8 IS OFF. `isMinifyEnabled = false` in app/build.gradle.kts. This file is wired
# into the release build but inert. It exists to record why, and what would have to be true
# before it is turned on.
#
# ---------------------------------------------------------------------------------------
# WHY OFF
# ---------------------------------------------------------------------------------------
# The release package is ~43 MB, of which ~42 MB (96.6%) is unminified dex, so the prize is
# real. It is not being taken yet for one specific reason:
#
#   supabase-kt 3.0.3 ships NO consumer ProGuard rules at all. Verified by enumerating every
#   entry of supabase-kt / postgrest-kt / realtime-kt / auth-kt AARs in the Gradle cache:
#   each contains only R.txt, AndroidManifest.xml, classes.jar and aar-metadata.properties.
#   No proguard.txt, no META-INF/proguard/.
#
# Every Supabase decode routes through KotlinXSerializer, which calls the RUNTIME reflective
# kotlinx.serialization.serializer(KType) lookup rather than the compile-time T.serializer().
# That lookup walks KType -> KClass -> Companion.serializer()/$serializer, so it depends on
# members surviving shrinking and obfuscation.
#
# Compounding it: no test in this project decodes JSON. app/src/test has two suites, both pure
# prayer-time maths. A broken shrink would compile, pass the suite, and fail on a real watch at
# the moment of pairing or sync. That is the worst outcome available, and worse than 43 MB.
#
# ---------------------------------------------------------------------------------------
# WHAT IS ALREADY COVERED (no hand-written rules needed for these)
# ---------------------------------------------------------------------------------------
# These libraries ship their own consumer rules, which AGP applies automatically:
#
#   kotlinx-serialization-core  META-INF/proguard/kotlinx-serialization-common.pro
#                               META-INF/com.android.tools/r8/kotlinx-serialization-r8.pro
#     Generic `-if @kotlinx.serialization.Serializable class **` rules that keep Companion
#     fields, serializer() methods and $serializer descriptors. These match all of this app's
#     @Serializable classes, including the private ones.
#   kotlinx-coroutines-core/-android   META-INF/proguard/coroutines.pro
#   ktor-utils                         META-INF/proguard/ktor.pro (volatile fields + the
#                                      HttpClientEngineContainer ServiceLoader entry)
#   okhttp                             META-INF/proguard/okhttp3.pro
#   kotlinx-datetime                   META-INF/proguard/datetime.pro
#   androidx.startup                   proguard.txt (keeps Initializer subclasses — this is
#                                      what protects supabase's SupabaseInitializer)
#   wear tiles / protolayout / datastore   protobuf GeneratedMessageLite field rules
#   watchface-complications-data           complication wire formats
#
# The three manifest components (MainActivity, MihrabTileService,
# MihrabComplicationDataSourceService) are auto-kept by AGP's generated manifest keep rules.
#
# ---------------------------------------------------------------------------------------
# BEFORE THIS CAN BE TURNED ON
# ---------------------------------------------------------------------------------------
# 1. Add a serialization round-trip unit test (decode a real sync_payload into SyncPayload)
#    and an instrumented test that exercises decodeSingle<PairingRow>() against a MINIFIED
#    build. Without those the config is only proven to compile, not to work.
# 2. Keep the reflection-sensitive surface whole, since it ships no rules of its own:
#      -keep class io.github.jan.supabase.** { *; }
#      -keep class io.ktor.** { *; }
#      -keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature,InnerClasses
#    Note androidx.compose and androidx.wear are ~11 MB of the dex and are NOT reflection
#    sensitive in these ways, so most of the win survives keeping the above intact.
# 3. MihrabTileService references "faith.mihrab.watch.MainActivity" as a string literal in its
#    LaunchAction. MainActivity is manifest-kept so it is not renamed, but that coupling is
#    invisible to R8 and must be re-checked if the manifest entry ever changes.
# 4. Note that android.enableR8.fullMode is unset and therefore ON by default, which is
#    stricter about generic signatures than legacy mode.
