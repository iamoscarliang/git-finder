package com.oscarliang.gitfinder.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.ui.common.RepoListItem
import com.oscarliang.gitfinder.ui.common.ShimmerListItem
import com.oscarliang.gitfinder.ui.theme.GitFinderTheme
import com.oscarliang.gitfinder.util.LoadMoreState
import com.oscarliang.gitfinder.util.REPO_PER_PAGE_COUNT
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.State
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    onRepoClick: (Repo) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val searchResults by viewModel.searchResults.observeAsState()
    val loadMoreState by viewModel.loadMoreState.observeAsState()
    SearchScreen(
        onRepoClick = onRepoClick,
        onBookmarkClick = { viewModel.toggleBookmark(it) },
        onSearch = { viewModel.setQuery(it, REPO_PER_PAGE_COUNT) },
        onLoadNextPage = { viewModel.loadNextPage() },
        onRetry = { viewModel.retry() },
        onRetryNextPage = { viewModel.retryNextPage() },
        searchResults = searchResults,
        loadMoreState = loadMoreState
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    onRepoClick: (Repo) -> Unit,
    onBookmarkClick: (Repo) -> Unit,
    onSearch: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onRetryNextPage: () -> Unit,
    searchResults: Resource<List<Repo>>?,
    loadMoreState: LoadMoreState?
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val refreshing by remember { mutableStateOf(false) }
        val state = rememberPullRefreshState(refreshing = refreshing, onRefresh = onRetry)

        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(onSearch = onSearch)
            Box(
                Modifier
                    .pullRefresh(state)
                    .testTag("refresh")
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(integerResource(id = R.integer.columns_count)),
                    contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_large)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_large)),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("searchResults")
                ) {
                    if (searchResults?.state == State.LOADING && searchResults.data.isNullOrEmpty()) {
                        items(2) {
                            ShimmerListItem()
                        }
                    } else {
                        val repos = searchResults?.data ?: emptyList()
                        itemsIndexed(
                            items = repos,
                            key = { _, repo -> repo.id }
                        ) { index, repo ->
                            val total = searchResults?.data?.size ?: 0
                            if (index >= total - 1) {
                                onLoadNextPage()
                            }
                            RepoListItem(
                                repo = repo,
                                onRepoClick = { onRepoClick(repo) },
                                onBookmarkClick = { onBookmarkClick(repo) }
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingSection(
                                loadMoreState = loadMoreState
                            ) {
                                onRetryNextPage()
                            }
                        }
                    }
                }
                if (!searchResults?.data.isNullOrEmpty()) {
                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = state,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }

    // Show empty text when search not found
    if (searchResults?.state == State.SUCCESS && searchResults.data?.size == 0) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(id = R.string.empty_search_result),
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // Show retry section when search error
    if (searchResults?.state == State.ERROR && searchResults.data == null) {
        RetrySection(
            error = searchResults.message ?: stringResource(id = R.string.unknown_error)
        ) {
            onRetry()
        }
    }

    // Show snack bar when search next page error
    val error = loadMoreState?.errorMessageIfNotHandled
    if (error != null) {
        LaunchedEffect(key1 = error) {
            scaffoldState.snackbarHostState.showSnackbar(error)
        }
    }
}

@Composable
fun SearchBar(
    onSearch: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(
                start = dimensionResource(id = R.dimen.margin_large),
                end = dimensionResource(id = R.dimen.margin_large),
                bottom = dimensionResource(id = R.dimen.margin_medium)
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            tint = MaterialTheme.colors.secondary,
            contentDescription = "Search",
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_medium)))
        var text by remember {
            mutableStateOf("")
        }
        TextField(
            value = text,
            onValueChange = { text = it },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.subtitle2,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_hint),
                    style = MaterialTheme.typography.subtitle2
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSecondary,
                placeholderColor = MaterialTheme.colors.secondary,
                backgroundColor = MaterialTheme.colors.surface
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSearch(text)
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun LoadingSection(
    loadMoreState: LoadMoreState?,
    onRetryNextPage: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.layout_loading_state_height))
            .fillMaxWidth()
    ) {
        if (loadMoreState?.hasMore == false) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                val error = loadMoreState.errorMessage
                if (error != null) {
                    RetrySection(
                        error = error
                    ) {
                        onRetryNextPage()
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.no_more_result),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.secondary,
                        style = MaterialTheme.typography.body1,
                    )
                }
            }
        }
        if (loadMoreState?.isRunning == true) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primaryVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("progressbar")
            )
        }
    }
}

@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
        Button(
            onClick = { onRetry() },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
        ) {
            Text(
                text = stringResource(id = R.string.retry),
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    GitFinderTheme {
        SearchScreen(
            onRepoClick = {},
            onBookmarkClick = {},
            onSearch = {},
            onLoadNextPage = {},
            onRetry = {},
            onRetryNextPage = {},
            searchResults = Resource.success(
                listOf(
                    Repo(1, "Repo1", "Desc1", Repo.Owner("Owner1", null), 10, 20, "Kotlin", ""),
                    Repo(2, "Repo2", "Desc2", Repo.Owner("Owner2", null), 20, 20, "Java", ""),
                    Repo(3, "Repo3", "Desc3", Repo.Owner("Owner3", null), 30, 40, "Python", "")
                )
            ),
            loadMoreState = LoadMoreState(isRunning = false, hasMore = false, errorMessage = null)
        )
    }
}

@Preview
@Composable
private fun SearchScreenLoadingPreview() {
    GitFinderTheme {
        SearchScreen(
            onRepoClick = {},
            onBookmarkClick = {},
            onSearch = {},
            onLoadNextPage = {},
            onRetry = {},
            onRetryNextPage = {},
            searchResults = Resource.loading(null),
            loadMoreState = LoadMoreState(isRunning = false, hasMore = true, errorMessage = null)
        )
    }
}

@Preview
@Composable
private fun SearchScreenErrorPreview() {
    GitFinderTheme {
        SearchScreen(
            onRepoClick = {},
            onBookmarkClick = {},
            onSearch = {},
            onLoadNextPage = {},
            onRetry = {},
            onRetryNextPage = {},
            searchResults = Resource.error("Search error", null),
            loadMoreState = null
        )
    }
}

@Preview
@Composable
private fun SearchScreenNextPageErrorPreview() {
    GitFinderTheme {
        SearchScreen(
            onRepoClick = {},
            onBookmarkClick = {},
            onSearch = {},
            onLoadNextPage = {},
            onRetry = {},
            onRetryNextPage = {},
            searchResults = Resource.success(
                listOf(
                    Repo(1, "Repo1", "Desc1", Repo.Owner("Owner1", null), 10, 20, "Kotlin", ""),
                    Repo(2, "Repo2", "Desc2", Repo.Owner("Owner2", null), 20, 20, "Java", ""),
                    Repo(3, "Repo3", "Desc3", Repo.Owner("Owner3", null), 30, 40, "Python", "")
                )
            ),
            loadMoreState = LoadMoreState(
                isRunning = false,
                hasMore = false,
                errorMessage = "Search next page error"
            )
        )
    }
}