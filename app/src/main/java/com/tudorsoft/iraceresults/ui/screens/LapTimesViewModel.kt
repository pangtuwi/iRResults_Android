package com.tudorsoft.iraceresults.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tudorsoft.iraceresults.data.DriverResult
import com.tudorsoft.iraceresults.data.api.DriverLapData
import com.tudorsoft.iraceresults.data.api.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LapTimesViewModel : ViewModel() {

    private val _lapData = MutableStateFlow<List<DriverLapData>>(emptyList())
    val lapData: StateFlow<List<DriverLapData>> = _lapData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchLapTimes(leagueId: String, subsessionId: Int, drivers: List<DriverResult>) {
        if (drivers.isEmpty()) {
            _lapData.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("LapTimesViewModel", "Fetching lap times for ${drivers.size} drivers in session: $subsessionId")
                
                // Fetch each driver's lap time concurrently
                val fetchedData = drivers.map { driver ->
                    async {
                        try {
                            val response = RetrofitClient.api.getDriverLapTimes(
                                leagueId = leagueId,
                                subsessionId = subsessionId,
                                custId = driver.custId
                            )
                            if (response.isSuccessful && response.body() != null) {
                                val laps = response.body()!!
                                // Convert seconds to ms since UI expects ms
                                val timesMs = laps.map { (it.time * 1000.0).toLong() }
                                DriverLapData(
                                    custId = driver.custId,
                                    displayName = driver.displayName,
                                    lapTimes = timesMs
                                )
                            } else {
                                Log.w("LapTimesViewModel", "No laps found for driver ${driver.displayName} (${response.code()})")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("LapTimesViewModel", "Error fetching laps for driver: ${driver.displayName}", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                if (fetchedData.isEmpty()) {
                    _error.value = "No lap data found for the selected drivers."
                } else {
                    _lapData.value = fetchedData
                }
            } catch (e: Exception) {
                Log.e("LapTimesViewModel", "Error managing lap time fetch", e)
                _error.value = "An error occurred while fetching lap data."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
