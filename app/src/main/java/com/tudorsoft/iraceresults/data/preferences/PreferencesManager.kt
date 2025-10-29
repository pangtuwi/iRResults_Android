package com.tudorsoft.iraceresults.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        private val LEAGUE_ID = stringPreferencesKey("league_id")
        private val CUST_ID = stringPreferencesKey("cust_id")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val DRIVER_CLASS = stringPreferencesKey("driver_class")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            isSetupComplete = preferences[SETUP_COMPLETE] ?: false,
            leagueId = preferences[LEAGUE_ID] ?: "",
            custId = preferences[CUST_ID] ?: "",
            displayName = preferences[DISPLAY_NAME] ?: "",
            driverClass = preferences[DRIVER_CLASS] ?: ""
        )
    }

    suspend fun saveUserInfo(
        leagueId: String,
        custId: String,
        displayName: String,
        driverClass: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETE] = true
            preferences[LEAGUE_ID] = leagueId
            preferences[CUST_ID] = custId
            preferences[DISPLAY_NAME] = displayName
            preferences[DRIVER_CLASS] = driverClass
        }
    }

    suspend fun clearUserInfo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
