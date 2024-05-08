package com.oscarliang.gitfinder.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Search : Screen("search")

    data object Bookmarks : Screen("bookmarks")

    data object Detail : Screen(
        route = "detail/{repoUrl}",
        navArguments = listOf(navArgument("repoUrl") {
            type = NavType.StringType
        })
    ) {
        fun createRoute(repoUrl: String) = "detail/$repoUrl"
    }
}