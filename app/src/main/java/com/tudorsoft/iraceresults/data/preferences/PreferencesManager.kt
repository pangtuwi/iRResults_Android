package com.tudorsoft.iraceresults.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tudorsoft.iraceresults.data.League
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        private val LEAGUE_ID = stringPreferencesKey("league_id")  // Deprecated
        private val LEAGUE_NAME = stringPreferencesKey("league_name")  // Deprecated
        private val CUST_ID = stringPreferencesKey("cust_id")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val DRIVER_CLASS = stringPreferencesKey("driver_class")
        private val SELECTED_LEAGUE_ID = stringPreferencesKey("selected_league_id")
        private val SAVED_LEAGUES = stringPreferencesKey("saved_leagues")
    }

    private val gson = Gson()

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            isSetupComplete = preferences[SETUP_COMPLETE] ?: false,
            leagueId = preferences[LEAGUE_ID] ?: "",
            leagueName = preferences[LEAGUE_NAME] ?: "",
            custId = preferences[CUST_ID] ?: "",
            displayName = preferences[DISPLAY_NAME] ?: "",
            driverClass = preferences[DRIVER_CLASS] ?: "",
            selectedLeagueId = preferences[SELECTED_LEAGUE_ID] ?: "",
            savedLeagues = preferences[SAVED_LEAGUES] ?: "[]"
        )
    }

    suspend fun saveUserInfo(
        leagueId: String,
        leagueName: String,
        custId: String,
        displayName: String,
        driverClass: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETE] = true
            preferences[LEAGUE_ID] = leagueId
            preferences[LEAGUE_NAME] = leagueName
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

    // Multi-league management functions

    suspend fun getLeagues(): List<League> {
        return context.dataStore.data.map { preferences ->
            getLeaguesFromJson(preferences[SAVED_LEAGUES] ?: "[]")
        }.first()
    }

    suspend fun addLeague(league: League) {
        context.dataStore.edit { preferences ->
            val currentLeagues = getLeaguesFromJson(preferences[SAVED_LEAGUES] ?: "[]")
            // Only add if not already present
            if (currentLeagues.none { it.leagueId == league.leagueId }) {
                val updatedLeagues = currentLeagues + league
                preferences[SAVED_LEAGUES] = gson.toJson(updatedLeagues)

                // If no league selected, set this as selected
                if (preferences[SELECTED_LEAGUE_ID].isNullOrEmpty()) {
                    preferences[SELECTED_LEAGUE_ID] = league.leagueId
                }
            }
        }
    }

    suspend fun removeLeague(leagueId: String) {
        context.dataStore.edit { preferences ->
            val currentLeagues = getLeaguesFromJson(preferences[SAVED_LEAGUES] ?: "[]")
            val updatedLeagues = currentLeagues.filter { it.leagueId != leagueId }
            preferences[SAVED_LEAGUES] = gson.toJson(updatedLeagues)

            // If removed league was selected, switch to first available
            if (preferences[SELECTED_LEAGUE_ID] == leagueId) {
                preferences[SELECTED_LEAGUE_ID] = updatedLeagues.firstOrNull()?.leagueId ?: ""
            }
        }
    }

    suspend fun setSelectedLeague(leagueId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LEAGUE_ID] = leagueId
        }
    }

    private fun getLeaguesFromJson(json: String): List<League> {
        return try {
            val type = object : TypeToken<List<League>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
