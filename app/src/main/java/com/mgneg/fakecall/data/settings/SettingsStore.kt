package com.mgneg.fakecall.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language") // system | en | ar
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val CALL_MODE = stringPreferencesKey("call_mode") // telecom | app
        val ACCOUNT_LABEL = stringPreferencesKey("account_label")
        val DEFAULT_NAME = stringPreferencesKey("default_name")
        val DEFAULT_NUMBER = stringPreferencesKey("default_number")
        val UPDATE_NOTIFICATIONS = booleanPreferencesKey("update_notifications")
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
        val VIBRATE_ON_RING = booleanPreferencesKey("vibrate_on_ring")
    }

    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "system" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: false }
    val callMode: Flow<String> = context.dataStore.data.map { it[Keys.CALL_MODE] ?: "telecom" }
    val accountLabel: Flow<String> = context.dataStore.data.map { it[Keys.ACCOUNT_LABEL] ?: "Fake Call" }
    val defaultName: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_NAME] ?: "" }
    val defaultNumber: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_NUMBER] ?: "" }
    val updateNotifications: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.UPDATE_NOTIFICATIONS] ?: false }
    val lastSeenVersion: Flow<String> =
        context.dataStore.data.map { it[Keys.LAST_SEEN_VERSION] ?: "" }
    val introShown: Flow<Boolean> = context.dataStore.data.map { it[Keys.INTRO_SHOWN] ?: false }
    val ringtoneUri: Flow<String?> = context.dataStore.data.map { it[Keys.RINGTONE_URI] }
    val vibrateOnRing: Flow<Boolean> = context.dataStore.data.map { it[Keys.VIBRATE_ON_RING] ?: true }

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
    suspend fun setDynamicColor(v: Boolean) = edit { it[Keys.DYNAMIC_COLOR] = v }
    suspend fun setCallMode(v: String) = edit { it[Keys.CALL_MODE] = v }
    suspend fun setAccountLabel(v: String) = edit { it[Keys.ACCOUNT_LABEL] = v }
    suspend fun setDefaultName(v: String) = edit { it[Keys.DEFAULT_NAME] = v }
    suspend fun setDefaultNumber(v: String) = edit { it[Keys.DEFAULT_NUMBER] = v }
    suspend fun setUpdateNotifications(v: Boolean) = edit { it[Keys.UPDATE_NOTIFICATIONS] = v }
    suspend fun setLastSeenVersion(v: String) = edit { it[Keys.LAST_SEEN_VERSION] = v }
    suspend fun setIntroShown(v: Boolean) = edit { it[Keys.INTRO_SHOWN] = v }
    suspend fun setRingtoneUri(v: String?) = edit { p ->
        if (v == null) p.remove(Keys.RINGTONE_URI) else p[Keys.RINGTONE_URI] = v
    }
    suspend fun setVibrateOnRing(v: Boolean) = edit { it[Keys.VIBRATE_ON_RING] = v }

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
