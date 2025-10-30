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
