package com.example.hotelbooking

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hotelbooking.ui.theme.HotelBookingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HotelBookingTheme {
                HotelBookingApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun HotelBookingApp() {
    val navController = rememberNavController()

    val authVM = viewModel<AuthViewModel>()
    val authState by authVM.authState.collectAsState()

    val startDestination = if (authVM.isLoggedIn) "Home" else "Login"
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(authState) {
        if (!authVM.isLoggedIn){
            navController.navigate("Login")
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label,
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    label = { Text(it.label) },
                    selected = it.label == currentRoute,
                    onClick = { navController.navigate(it.label) },
                    enabled = authVM.isLoggedIn
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = Color(0xFF6D9773),
            navigationBarContentColor = Color.White
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Hotel Booking") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6D9773),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("Home") {
                    HomeScreen(Modifier.padding(innerPadding))
                }
                composable("History") {
                    BookingHistoryScreen(Modifier.padding(innerPadding))
                }
                composable("Login"){
                    LoginScreen({navController.navigate("Home"){ popUpTo("Home")} }, {navController.navigate("Register")})
                }
                composable("Register"){
                    RegisterScreen({navController.popBackStack("Login", true)}, {navController.popBackStack("Login", false)})
                }
                composable("Profile"){
                    ProfileScreen(Modifier.padding(innerPadding), {navController.navigate("Login")})
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    HISTORY("History", R.drawable.bookmark),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HotelBookingTheme {
        Greeting("Android")
    }
}