package com.mgneg.fakecall.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mgneg.fakecall.telecom.CallAttemptDiagnostics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language") // system | en | ar
        // Burgundy redesign: the one new key. system | light | dark.
        // Absent (every install upgrading from v1.0) means "system",
        // which is exactly the pre-redesign behaviour — no migration
        // needed and no existing key is removed or reinterpreted.
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CALL_MODE = stringPreferencesKey("call_mode") // telecom | app
        val ACCOUNT_LABEL = stringPreferencesKey("account_label")
        val DEFAULT_NAME = stringPreferencesKey("default_name")
        val DEFAULT_NUMBER = stringPreferencesKey("default_number")
        val UPDATE_NOTIFICATIONS = booleanPreferencesKey("update_notifications")
        val AUTOMATION_ENABLED = booleanPreferencesKey("automation_enabled")
        val TELECOM_EVIDENCE = booleanPreferencesKey("telecom_evidence")
        val LAST_CHECK = longPreferencesKey("update_last_check")
        val ETAG = stringPreferencesKey("update_etag")
        val LATEST_VERSION = stringPreferencesKey("update_latest_version")
        val LATEST_NOTES = stringPreferencesKey("update_latest_notes")
        val LATEST_RELEASE_URL = stringPreferencesKey("update_latest_release_url")
        val LATEST_APK_URL = stringPreferencesKey("update_latest_apk_url")
        val LATEST_SHA_URL = stringPreferencesKey("update_latest_sha_url")
        val LATEST_DATE = stringPreferencesKey("update_latest_date")
        val LAST_SEEN_VERSION = stringPreferencesKey("last_seen_version")
        val INTRO_SHOWN = booleanPreferencesKey("intro_shown")
        val RINGTONE_URI = stringPreferencesKey("ringtone_uri")
        // Last-call-attempt diagnostics (see telecom/CallDiagnostics.kt).
        val DIAG_TIMESTAMP = longPreferencesKey("diag_timestamp")
        val DIAG_MODE = stringPreferencesKey("diag_mode")
        val DIAG_ACCOUNT_ENABLED = booleanPreferencesKey("diag_account_enabled")
        val DIAG_PLACE_ATTEMPTED = booleanPreferencesKey("diag_place_attempted")
        val DIAG_PLACE_RESULT = booleanPreferencesKey("diag_place_result")
        val DIAG_PLACE_ERROR_CLASS = stringPreferencesKey("diag_place_error_class")
        val DIAG_PLACE_ERROR_MESSAGE = stringPreferencesKey("diag_place_error_message")
        val DIAG_ROUTE = stringPreferencesKey("diag_route")
        val DIAG_SNAPSHOT = stringPreferencesKey("diag_snapshot")
    }

    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "system" }
    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val callMode: Flow<String> = context.dataStore.data.map { it[Keys.CALL_MODE] ?: "telecom" }
    val accountLabel: Flow<String> = context.dataStore.data.map { it[Keys.ACCOUNT_LABEL] ?: "Fake Call" }
    val defaultName: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_NAME] ?: "" }
    val defaultNumber: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_NUMBER] ?: "" }
    val updateNotifications: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.UPDATE_NOTIFICATIONS] ?: false }

    /**
     * Connection evidence: true after FakeConnectionService actually
     * created an incoming connection (proof the system routed a call to
     * our account); cleared when a later telecom placement fails. Only a
     * fallback signal for account enablement — see AccountEnablementRules.
     */
    val telecomEvidence: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.TELECOM_EVIDENCE] ?: false }

    /** External automation triggers are OFF until the owner opts in. */
    val automationEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.AUTOMATION_ENABLED] ?: false }
    val lastSeenVersion: Flow<String> =
        context.dataStore.data.map { it[Keys.LAST_SEEN_VERSION] ?: "" }
    val introShown: Flow<Boolean> = context.dataStore.data.map { it[Keys.INTRO_SHOWN] ?: false }
    val ringtoneUri: Flow<String?> = context.dataStore.data.map { it[Keys.RINGTONE_URI] }

    /** The most recent trigger attempt's diagnostics; timestamp 0 means none recorded yet. */
    val lastCallAttempt: Flow<CallAttemptDiagnostics> = context.dataStore.data.map { p ->
        CallAttemptDiagnostics(
            timestamp = p[Keys.DIAG_TIMESTAMP] ?: 0L,
            mode = p[Keys.DIAG_MODE] ?: "",
            accountEnabled = p[Keys.DIAG_ACCOUNT_ENABLED] ?: false,
            placeAttempted = p[Keys.DIAG_PLACE_ATTEMPTED] ?: false,
            placeResult = p[Keys.DIAG_PLACE_RESULT] ?: false,
            placeErrorClass = p[Keys.DIAG_PLACE_ERROR_CLASS],
            placeErrorMessage = p[Keys.DIAG_PLACE_ERROR_MESSAGE],
            route = p[Keys.DIAG_ROUTE] ?: "",
            snapshot = p[Keys.DIAG_SNAPSHOT]?.split("\n")?.filter { it.isNotEmpty() } ?: emptyList()
        )
    }

    suspend fun saveLastCallAttempt(d: CallAttemptDiagnostics) = edit { p ->
        p[Keys.DIAG_TIMESTAMP] = d.timestamp
        p[Keys.DIAG_MODE] = d.mode
        p[Keys.DIAG_ACCOUNT_ENABLED] = d.accountEnabled
        p[Keys.DIAG_PLACE_ATTEMPTED] = d.placeAttempted
        p[Keys.DIAG_PLACE_RESULT] = d.placeResult
        d.placeErrorClass?.let { p[Keys.DIAG_PLACE_ERROR_CLASS] = it } ?: p.remove(Keys.DIAG_PLACE_ERROR_CLASS)
        d.placeErrorMessage?.let { p[Keys.DIAG_PLACE_ERROR_MESSAGE] = it }
            ?: p.remove(Keys.DIAG_PLACE_ERROR_MESSAGE)
        p[Keys.DIAG_ROUTE] = d.route
        p[Keys.DIAG_SNAPSHOT] = d.snapshot.joinToString("\n")
    }

    data class UpdateCache(
        val lastCheck: Long = 0,
        val etag: String? = null,
        val latestVersion: String? = null,
        val notes: String = "",
        val releaseUrl: String = "",
        val apkUrl: String = "",
        val shaUrl: String = "",
        val date: String = ""
    )

    val updateCache: Flow<UpdateCache> = context.dataStore.data.map { p ->
        UpdateCache(
            lastCheck = p[Keys.LAST_CHECK] ?: 0,
            etag = p[Keys.ETAG],
            latestVersion = p[Keys.LATEST_VERSION],
            notes = p[Keys.LATEST_NOTES] ?: "",
            releaseUrl = p[Keys.LATEST_RELEASE_URL] ?: "",
            apkUrl = p[Keys.LATEST_APK_URL] ?: "",
            shaUrl = p[Keys.LATEST_SHA_URL] ?: "",
            date = p[Keys.LATEST_DATE] ?: ""
        )
    }

    suspend fun setLanguage(v: String) = edit { it[Keys.LANGUAGE] = v }
    suspend fun setThemeMode(v: String) = edit { it[Keys.THEME_MODE] = v }
    suspend fun setCallMode(v: String) = edit { it[Keys.CALL_MODE] = v }
    suspend fun setAccountLabel(v: String) = edit { it[Keys.ACCOUNT_LABEL] = v }
    suspend fun setDefaultName(v: String) = edit { it[Keys.DEFAULT_NAME] = v }
    suspend fun setDefaultNumber(v: String) = edit { it[Keys.DEFAULT_NUMBER] = v }
    suspend fun setUpdateNotifications(v: Boolean) = edit { it[Keys.UPDATE_NOTIFICATIONS] = v }
    suspend fun setAutomationEnabled(v: Boolean) = edit { it[Keys.AUTOMATION_ENABLED] = v }
    suspend fun setTelecomEvidence(v: Boolean) = edit { it[Keys.TELECOM_EVIDENCE] = v }
    suspend fun setLastSeenVersion(v: String) = edit { it[Keys.LAST_SEEN_VERSION] = v }
    suspend fun setIntroShown(v: Boolean) = edit { it[Keys.INTRO_SHOWN] = v }
    suspend fun setRingtoneUri(v: String?) = edit { p ->
        if (v == null) p.remove(Keys.RINGTONE_URI) else p[Keys.RINGTONE_URI] = v
    }

    suspend fun saveUpdateCache(c: UpdateCache) = edit { p ->
        p[Keys.LAST_CHECK] = c.lastCheck
        c.etag?.let { p[Keys.ETAG] = it }
        c.latestVersion?.let { p[Keys.LATEST_VERSION] = it } ?: p.remove(Keys.LATEST_VERSION)
        p[Keys.LATEST_NOTES] = c.notes
        p[Keys.LATEST_RELEASE_URL] = c.releaseUrl
        p[Keys.LATEST_APK_URL] = c.apkUrl
        p[Keys.LATEST_SHA_URL] = c.shaUrl
        p[Keys.LATEST_DATE] = c.date
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }
}
