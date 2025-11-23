package com.tudorsoft.iraceresults.data.preferences

data class UserPreferences(
    val isSetupComplete: Boolean = false,
    val leagueId: String = "",  // Deprecated - kept for migration
    val leagueName: String = "",  // Deprecated - kept for migration
    val custId: String = "",
    val displayName: String = "",
    val driverClass: String = "",
    val selectedLeagueId: String = "",  // Currently active league
    val savedLeagues: String = "[]"  // JSON array of League objects
)
