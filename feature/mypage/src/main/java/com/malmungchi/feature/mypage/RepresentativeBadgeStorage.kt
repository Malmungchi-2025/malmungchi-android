package com.malmungchi.feature.mypage


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.representativeBadgeDataStore by preferencesDataStore("representative_badge")

object RepresentativeBadgeStorage {
    private val KEY = stringPreferencesKey("representative_badge_key")

    suspend fun save(context: Context, badgeKey: String) {
        context.representativeBadgeDataStore.edit { prefs ->
            prefs[KEY] = badgeKey
        }
    }

    suspend fun load(context: Context): String? {
        return context.representativeBadgeDataStore.data
            .map { it[KEY] }
            .first()
    }
}