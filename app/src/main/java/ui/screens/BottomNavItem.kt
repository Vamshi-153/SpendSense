    package com.example.spendsense.ui

    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.AttachMoney
    import androidx.compose.material.icons.filled.Article
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.Person
    import androidx.compose.ui.graphics.vector.ImageVector


    sealed class BottomNavItem(
        val route: String,
        val title: String,
        val icon: ImageVector
    ) {
        object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
        object Transactions : BottomNavItem("transactions", "Transactions", Icons.Filled.Article)
        object Investments : BottomNavItem("investments", "Investments", Icons.Filled.AttachMoney)
        object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
    }
