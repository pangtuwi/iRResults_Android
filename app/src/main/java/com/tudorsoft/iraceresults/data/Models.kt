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

data class TeamStanding(
    val position: Int,
    val teamName: String,
    val driver1: String,
    val driver2: String,
    val driver3: String,
    val totalPoints: Int
)

data class Round(
    val roundNo: Int,
    val trackName: String,
    val startTime: String
)

data class Penalty(
    val protestId: Int,
    val roundName: String,
    val roundNo: Int,
    val scoreEvent: String = "",
    val driverName: String,
    val stewardsDecision: String
)
