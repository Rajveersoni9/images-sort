package com.mediaflow.ai.ui.navigation

sealed class Screen(val route: String) {
    object Gallery : Screen("gallery")
    object Collections : Screen("collections")
    object Search : Screen("search")
    object Activity : Screen("activity")
    object Settings : Screen("settings")

    object AlbumDetail : Screen("album_detail/{albumId}") {
        fun createRoute(albumId: String) = "album_detail/$albumId"
    }

    object MediaViewer : Screen("media_viewer/{mediaId}") {
        fun createRoute(mediaId: Long) = "media_viewer/$mediaId"
    }
}
