package com.tudorsoft.iraceresults.data.api

import com.google.gson.annotations.SerializedName

data class DriverResponse(
    @SerializedName("cust_id")
    val custId: Int,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("class")
    val driverClass: String?
)

data class LeagueNameResponse(
    @SerializedName("leagueid")
    val leagueId: String,
    @SerializedName("leaguename")
    val leagueName: String
)
