package com.gfxtool.roblox.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gfxtool.roblox.ui.screens.AboutScreen
import com.gfxtool.roblox.ui.screens.MainScreen

object Routes {
    const val MAIN  = "main"
    const val ABOUT = "about"
}

@Composable
fun AppNavHost(vm: GfxViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(
                vm = vm,
                onNavigateToAbout = { nav.navigate(Routes.ABOUT) },
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { nav.popBackStack() })
        }
    }
}