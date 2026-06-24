package com.gfxtool.roblox.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gfxtool.roblox.ui.screens.AboutScreen
import com.gfxtool.roblox.ui.screens.MainScreen
import com.gfxtool.roblox.ui.screens.PermissionGuideSheet
import com.gfxtool.roblox.ui.theme.SurfaceDark

object Routes {
    const val MAIN  = "main"
    const val ABOUT = "about"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(vm: GfxViewModel) {
    val nav = rememberNavController()
    var showPermSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    NavHost(navController = nav, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(vm = vm)
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { nav.popBackStack() })
        }
    }

    if (showPermSheet) {
        ModalBottomSheet(
            onDismissRequest  = { showPermSheet = false },
            sheetState        = sheetState,
            containerColor    = SurfaceDark,
        ) {
            PermissionGuideSheet(onDismiss = { showPermSheet = false })
        }
    }
}
