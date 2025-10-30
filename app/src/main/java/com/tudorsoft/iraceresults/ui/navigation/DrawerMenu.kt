package com.tudorsoft.iraceresults.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Tables : DrawerMenuItem("tables", "Tables", Icons.Default.List)
    object Rounds : DrawerMenuItem("rounds", "Rounds", Icons.Outlined.CheckCircle)
    object Penalties : DrawerMenuItem("penalties", "Penalties", Icons.Default.Warning)
    object MyPenalties : DrawerMenuItem("my_penalties", "My Penalties", Icons.Default.Person)
    object LicencePoints : DrawerMenuItem("licence_points", "Licence Points", Icons.Default.Star)
    object Teams : DrawerMenuItem("teams", "Teams", Icons.Default.AccountBox)
    object Settings : DrawerMenuItem("settings", "Settings", Icons.Default.Settings)
    object About : DrawerMenuItem("about", "About", Icons.Default.Info)

    companion object {
        val items = listOf(
            Tables,
            Rounds,
            Penalties,
            MyPenalties,
            LicencePoints,
            Teams,
            Settings,
            About
        )
    }
}
