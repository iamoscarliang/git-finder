package com.oscarliang.gitfinder.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oscarliang.gitfinder.ui.bookmarks.BookmarksScreen
import com.oscarliang.gitfinder.ui.detail.DetailScreen
import com.oscarliang.gitfinder.ui.search.SearchScreen
import java.util.Base64

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { fadeOut(tween(200)) },
        modifier = modifier
    ) {
        composable(route = Screen.Search.route) {
            SearchScreen(
                onRepoClick = { repo ->
                    navigateToDetail(navController, repo.url)
                }
            )
        }
        composable(route = Screen.Bookmarks.route) {
            BookmarksScreen(
                onRepoClick = { repo ->
                    navigateToDetail(navController, repo.url)
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = Screen.Detail.navArguments,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(200)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(200)
                )
            }
        ) { backStackEntry ->
            DetailScreen(
                repoUrl = backStackEntry.arguments?.getString("repoUrl")
            )
        }
    }
}

private fun navigateToDetail(navController: NavHostController, repoUrl: String) {
    // Since the url contain '/', we convert them to escape format
    val url = Base64.getUrlEncoder().encodeToString(repoUrl.toByteArray())
    navController.navigate(Screen.Detail.createRoute(url))
}