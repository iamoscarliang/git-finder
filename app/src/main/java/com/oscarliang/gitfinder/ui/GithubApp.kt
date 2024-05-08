package com.oscarliang.gitfinder.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oscarliang.gitfinder.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GithubApp(orientation: Int) {

    val navController = rememberNavController()

    // Show and hide the navigation bar in detail screen
    var showNavigationBar by remember { mutableStateOf(true) }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        showNavigationBar = destination.route != Screen.Detail.route
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                navigationIcon = {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_large)))
                    Icon(
                        painter = painterResource(R.drawable.ic_gitfinder),
                        contentDescription = "GitFinder"
                    )
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        bottomBar = {
            if (orientation == Configuration.ORIENTATION_PORTRAIT && showNavigationBar) {
                BottomNavigationBar(
                    navController = navController,
                    items = navItems,
                    onItemClick = {
                        navController.navigate(it.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBarsIgnoringVisibility)
    ) { innerPadding ->
        Row {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE && showNavigationBar) {
                NavigationRailBar(
                    navController = navController,
                    items = navItems,
                    onItemClick = {
                        navController.navigate(it.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            NavGraph(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<NavItem>,
    onItemClick: (NavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface,
        elevation = dimensionResource(id = R.dimen.margin_small)
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = MaterialTheme.colors.secondary,
                icon = {
                    Icon(
                        painter = painterResource(item.iconId),
                        contentDescription = item.route
                    )
                },
                label = { Text(stringResource(id = item.nameId)) }
            )
        }
    }
}

@Composable
fun NavigationRailBar(
    navController: NavController,
    items: List<NavItem>,
    onItemClick: (NavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationRail(
        backgroundColor = MaterialTheme.colors.surface,
        elevation = dimensionResource(id = R.dimen.margin_small)
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            NavigationRailItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = MaterialTheme.colors.secondary,
                icon = {
                    Icon(
                        painter = painterResource(item.iconId),
                        contentDescription = item.route
                    )
                },
                label = { Text(stringResource(id = item.nameId)) }
            )
        }
    }
}

val navItems = listOf(
    NavItem(
        route = Screen.Search.route,
        nameId = R.string.menu_search,
        iconId = R.drawable.ic_search
    ),
    NavItem(
        route = Screen.Bookmarks.route,
        nameId = R.string.menu_bookmarks,
        iconId = R.drawable.ic_bookmarks
    )
)

data class NavItem(
    val route: String,
    val nameId: Int,
    val iconId: Int
)