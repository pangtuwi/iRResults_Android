package com.tudorsoft.iraceresults.data.api

import com.google.gson.annotations.SerializedName

data class DriverResponse(
    @SerializedName("cust_id")
    val custId: Int,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("classnumber")
    val driverClass: Int?
)

data class LeagueNameResponse(
    @SerializedName("leagueid")
    val leagueId: String,
    @SerializedName("leaguename")
    val leagueName: String
)

data class ClassResponse(
    @SerializedName("classnumber")
    val classNumber: Int,
    @SerializedName("classname")
    val className: String
)

data class ClassTotalsResponse(
    @SerializedName("position")
    val position: Int,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("total_points")
    val totalPoints: Int,
    @SerializedName("classnumber")
    val classNumber: Int,
    @SerializedName("classname")
    val className: String?
)

data class TeamStandingResponse(
    @SerializedName("Pos")
    val position: Int?,
    @SerializedName("Team Name")
    val teamName: String?,
    @SerializedName("Driver 1")
    val driver1: String?,
    @SerializedName("Driver 2")
    val driver2: String?,
    @SerializedName("Driver 3")
    val driver3: String?,
    @SerializedName("Total")
    val total: Int?
)

data class RoundResponse(
    @SerializedName("round_no")
    val roundNo: Int,
    @SerializedName("track_name")
    val trackName: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("subsession_ids")
    val subsessionIds: List<Int>?,
    @SerializedName("score_types")
    val scoreTypes: List<Int>?
)

data class PenaltyResponse(
    @SerializedName("protest_id")
    val protestId: Int,
    @SerializedName("round_name")
    val roundName: String,
    @SerializedName("round_no")
    val roundNo: Int,
    @SerializedName("score_event")
    val scoreEvent: String?,
    @SerializedName("lap")
    val lap: String,
    @SerializedName("corner")
    val corner: String,
    @SerializedName("cust_id")
    val custId: Int,
    @SerializedName("driver_statement")
    val driverStatement: String?,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("protesting_cust_id")
    val protestingCustId: Int,
    @SerializedName("protesting_driver_name")
    val protestingDriverName: String,
    @SerializedName("stewards_decision")
    val stewardsDecision: String,
    @SerializedName("time_added")
    val timeAdded: Int,
    @SerializedName("positions")
    val positions: Int,
    @SerializedName("licence_points")
    val licencePoints: Int,
    @SerializedName("championship_points")
    val championshipPoints: Int,
    @SerializedName("disqualified")
    val disqualified: Int,
    @SerializedName("stewards_comments")
    val stewardsComments: String?,
    @SerializedName("penalty_id")
    val penaltyId: Int,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("session_no")
    val sessionNo: Int
)
