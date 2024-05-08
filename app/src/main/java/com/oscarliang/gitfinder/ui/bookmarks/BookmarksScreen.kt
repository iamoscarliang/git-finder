package com.oscarliang.gitfinder.ui.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.ui.common.RepoListItem
import com.oscarliang.gitfinder.ui.theme.GitFinderTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookmarksScreen(
    onRepoClick: (Repo) -> Unit,
    viewModel: BookmarksViewModel = koinViewModel()
) {
    val bookmarks by viewModel.bookmarks.observeAsState(initial = emptyList())
    BookmarksScreen(
        onRepoClick = onRepoClick,
        onBookmarkClick = { viewModel.toggleBookmark(it) },
        bookmarks = bookmarks
    )
}

@Composable
fun BookmarksScreen(
    onRepoClick: (Repo) -> Unit,
    onBookmarkClick: (Repo) -> Unit,
    bookmarks: List<Repo>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(integerResource(id = R.integer.columns_count)),
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.margin_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_large)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.margin_large)),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = bookmarks,
                key = { it.id }
            ) { repo ->
                RepoListItem(
                    repo = repo,
                    onRepoClick = { onRepoClick(repo) },
                    onBookmarkClick = { onBookmarkClick(repo) })
            }
        }
        if (bookmarks.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_bookmarks),
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview
@Composable
private fun BookmarksScreenPreview(
) {
    GitFinderTheme {
        BookmarksScreen(
            onRepoClick = {},
            onBookmarkClick = {},
            bookmarks = listOf(
                Repo(1, "Repo1", "Desc1", Repo.Owner("Owner1", null), 10, 20, "Kotlin", ""),
                Repo(2, "Repo2", "Desc2", Repo.Owner("Owner2", null), 20, 20, "Java", ""),
                Repo(3, "Repo3", "Desc3", Repo.Owner("Owner3", null), 30, 40, "Python", "")
            )
        )
    }
}