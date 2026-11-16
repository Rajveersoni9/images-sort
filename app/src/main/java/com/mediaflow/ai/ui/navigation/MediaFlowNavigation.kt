package com.mediaflow.ai.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.mediaflow.ai.ui.components.BottomNavBar
import com.mediaflow.ai.ui.screens.activity.ActivityScreen
import com.mediaflow.ai.ui.screens.activity.ActivityViewModel
import com.mediaflow.ai.ui.screens.collections.AlbumDetailScreen
import com.mediaflow.ai.ui.screens.collections.CollectionsScreen
import com.mediaflow.ai.ui.screens.collections.CollectionsViewModel
import com.mediaflow.ai.ui.screens.gallery.GalleryScreen
import com.mediaflow.ai.ui.screens.gallery.GalleryViewModel
import com.mediaflow.ai.ui.screens.search.SearchScreen
import com.mediaflow.ai.ui.screens.search.SearchViewModel
import com.mediaflow.ai.ui.screens.settings.SettingsScreen
import com.mediaflow.ai.ui.screens.settings.SettingsViewModel
import com.mediaflow.ai.ui.screens.viewer.MediaViewerScreen
import com.mediaflow.ai.ui.screens.viewer.MediaViewerViewModel

@Composable
fun MediaFlowNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on fullscreen media viewer
    val isViewerOpen = currentRoute?.startsWith("media_viewer") == true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (!isViewerOpen) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Gallery.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Gallery.route,
            enterTransition = { fadeIn(animationSpec = tween(280)) },
            exitTransition = { fadeOut(animationSpec = tween(280)) },
            popEnterTransition = { fadeIn(animationSpec = tween(280)) },
            popExitTransition = { fadeOut(animationSpec = tween(280)) }
        ) {
            composable(Screen.Gallery.route) {
                val viewModel: GalleryViewModel = hiltViewModel()
                GalleryScreen(
                    viewModel = viewModel,
                    onNavigateToMedia = { mediaId ->
                        navController.navigate(Screen.MediaViewer.createRoute(mediaId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToActivity = {
                        navController.navigate(Screen.Activity.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Collections.route) {
                val viewModel: CollectionsViewModel = hiltViewModel()
                CollectionsScreen(
                    viewModel = viewModel,
                    onAlbumClick = { albumId ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                    }
                )
            }

            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType })
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
                val viewModel: CollectionsViewModel = hiltViewModel()
                AlbumDetailScreen(
                    albumId = albumId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMediaClick = { mediaId ->
                        navController.navigate(Screen.MediaViewer.createRoute(mediaId))
                    }
                )
            }

            composable(Screen.Search.route) {
                val viewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = viewModel,
                    onMediaClick = { mediaId ->
                        navController.navigate(Screen.MediaViewer.createRoute(mediaId))
                    }
                )
            }

            composable(Screen.Activity.route) {
                val viewModel: ActivityViewModel = hiltViewModel()
                ActivityScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.MediaViewer.route,
                arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: 0L
                val viewModel: MediaViewerViewModel = hiltViewModel()
                MediaViewerScreen(
                    mediaId = mediaId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
