package com.tudorsoft.iraceresults.data

data class League(
    val leagueId: String,
    val leagueName: String
)

data class Driver(
    val displayName: String,
    val className: String,
    val custId: Int = 0
)

data class RacingClass(
    val name: String,
    val id: String
)

data class StandingEntry(
    val position: Int,
    val driverName: String,
    val points: Int,
    val className: String = ""
)
