package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ahb_pms_prefs")

class PreferencesManager(context: Context) {
    private val appContext = context.applicationContext
    private val keyLanguage = stringPreferencesKey("app_language")

    val languageFlow: Flow<AppLanguage> = appContext.dataStore.data.map { prefs ->
        when (prefs[keyLanguage]) {
            AppLanguage.BN.name -> AppLanguage.BN
            else -> AppLanguage.EN
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        appContext.dataStore.edit { prefs ->
            prefs[keyLanguage] = language.name
        }
    }
}
