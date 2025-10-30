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
}
