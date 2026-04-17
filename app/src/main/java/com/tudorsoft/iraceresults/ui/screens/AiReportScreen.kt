package com.tudorsoft.iraceresults.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tudorsoft.iraceresults.data.api.RetrofitClient
import com.tudorsoft.iraceresults.ui.theme.LeagueTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiReportScreen(
    leagueId: String,
    roundNo: Int,
    theme: LeagueTheme,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var htmlContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(leagueId, roundNo) {
        try {
            isLoading = true
            errorMessage = null
            val response = RetrofitClient.api.getAiReport(leagueId, roundNo)
            if (response.isSuccessful) {
                htmlContent = response.body()?.string() ?: "<p>No report data available.</p>"
            } else {
                errorMessage = "Failed to fetch AI report (HTTP ${response.code()})"
            }
        } catch (e: Exception) {
            errorMessage = "Error loading report: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Race Report - Round $roundNo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.primaryDark,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = theme.primary
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val response = RetrofitClient.api.getAiReport(leagueId, roundNo)
                                    if (response.isSuccessful) {
                                        htmlContent = response.body()?.string() ?: "<p>No report data available.</p>"
                                    } else {
                                        errorMessage = "Failed to fetch AI report (HTTP ${response.code()})"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error loading report: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        Text("Retry")
                    }
                }
            } else if (htmlContent != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = false
                            
                            // Let the WebView background match the app's background automatically
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            
                            // Load content
                            loadDataWithBaseURL(null, htmlContent!!, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        // No dynamic updates required for static HTML so far.
                    }
                )
            }
        }
    }
}
