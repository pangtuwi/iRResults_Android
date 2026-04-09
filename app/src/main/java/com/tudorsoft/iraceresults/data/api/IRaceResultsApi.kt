package com.tudorsoft.iraceresults.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface IRaceResultsApi {

    @GET("{leagueid}/drivers")
    suspend fun getDrivers(
        @Path("leagueid") leagueId: String
    ): Response<List<DriverResponse>>

    @GET("{leagueid}/leaguename")
    suspend fun getLeagueName(
        @Path("leagueid") leagueId: String
    ): Response<LeagueNameResponse>

    @GET("{leagueid}/classes")
    suspend fun getClasses(
        @Path("leagueid") leagueId: String
    ): Response<List<ClassResponse>>

    @GET("{leagueid}/classtotals")
    suspend fun getClassTotals(
        @Path("leagueid") leagueId: String
    ): Response<List<List<Any>>>

    @GET("{leagueid}/teamstotals")
    suspend fun getTeamStandings(
        @Path("leagueid") leagueId: String
    ): Response<List<TeamStandingResponse>>

    @GET("{leagueid}/rounds")
    suspend fun getRounds(
        @Path("leagueid") leagueId: String
    ): Response<List<RoundResponse>>

    @GET("{leagueid}/penaltiesjson")
    suspend fun getPenalties(
        @Path("leagueid") leagueId: String
    ): Response<List<PenaltyResponse>>

    @GET("{leagueid}/licencepoints")
    suspend fun getLicencePoints(
        @Path("leagueid") leagueId: String
    ): Response<List<List<Any>>>

    @GET("{leagueid}/fullresults")
    suspend fun getFullResults(
        @Path("leagueid") leagueId: String
    ): Response<List<RoundEventResponse>>

    @GET("{leagueid}/colortheme")
    suspend fun getColorTheme(
        @Path("leagueid") leagueId: String
    ): Response<ColorThemeResponse>

    @GET("leaguelist")
    suspend fun getLeagueList(): Response<List<LeagueListItemResponse>>

    @retrofit2.http.GET("{leagueid}/laptimes")
    suspend fun getDriverLapTimes(
        @Path("leagueid") leagueId: String,
        @retrofit2.http.Query("subsession_id") subsessionId: Int,
        @retrofit2.http.Query("cust_id") custId: Int
    ): Response<List<LapTimeResponse>>
}
