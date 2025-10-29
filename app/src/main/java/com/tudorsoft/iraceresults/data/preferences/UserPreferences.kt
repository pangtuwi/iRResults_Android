package com.tudorsoft.iraceresults.data.preferences

data class UserPreferences(
    val isSetupComplete: Boolean = false,
    val leagueId: String = "",
    val custId: String = "",
    val displayName: String = "",
    val driverClass: String = ""
)
