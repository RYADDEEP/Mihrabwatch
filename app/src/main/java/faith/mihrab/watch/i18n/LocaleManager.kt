package faith.mihrab.watch.i18n

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Resolves and applies the watch's UI locale from the phone-authoritative `display_language`
 * field on the sync payload (schema v1.1). Falls back through legacy `locale`, the device's
 * own default locale, and finally `"en"` so older phones (or unrecognized tags) still render.
 *
 * `AppCompatDelegate.setApplicationLocales` is a static API and persists the chosen locale
 * across launches automatically — no DataStore caching needed. Idempotent: the call is
 * skipped when the current locale already matches the requested tag.
 */
object LocaleManager {
    private val SUPPORTED: Set<String> = setOf(
        "en", "ar", "id", "ur", "tr", "fr", "ru", "fa", "hi", "ms", "sw", "bn",
    )

    fun resolveTag(displayLanguage: String?, locale: String?): String {
        if (displayLanguage != null && displayLanguage in SUPPORTED) return displayLanguage
        if (locale != null && locale in SUPPORTED) return locale
        val device = Resources.getSystem().configuration.locales[0].language
        if (device in SUPPORTED) return device
        return "en"
    }

    fun apply(tag: String) {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        // toLanguageTags() returns "" when no app locale set, or e.g. "ar" / "ar-SA".
        // Match on the leading subtag so "ar" and "ar-SA" are both treated as already-applied.
        val currentPrimary = current.substringBefore('-').ifEmpty { null }
        if (currentPrimary == tag) return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
