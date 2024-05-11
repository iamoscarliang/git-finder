package com.oscarliang.gitfinder.ui.search

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.util.LoadMoreState
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @Rule
    @JvmField
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testSearchLoading() {
        startSearchScreen(searchResults = Resource.loading(null))
        composeTestRule.onAllNodesWithTag("shimmer")[0].assertIsDisplayed()
    }

    @Test
    fun testSearchLoadingWithData() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startSearchScreen(searchResults = Resource.loading(listOf(repo)))
        composeTestRule.onNodeWithTag("shimmer").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("foo").assertIsDisplayed()
    }

    @Test
    fun testSearchSuccess() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startSearchScreen(searchResults = Resource.success(listOf(repo)))
        composeTestRule.onNodeWithTag("shimmer").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("foo").assertIsDisplayed()
    }

    @Test
    fun testSearchError() {
        startSearchScreen(searchResults = Resource.error("idk", null))
        composeTestRule.onNodeWithText("idk").assertIsDisplayed()
    }

    @Test
    fun testRefresh() {
        var retryCalled = false
        startSearchScreen(
            onRetry = {
                retryCalled = true
            },
            searchResults = Resource.error("idk", null)
        )
        composeTestRule.onNodeWithText("idk").assertIsDisplayed()
        composeTestRule.onNodeWithTag("refresh").performTouchInput { swipeDown() }
        assertEquals(retryCalled, true)
    }

    @Test
    fun testSearchNextPage() {
        var loadNextPageCalled = false
        val repos = TestUtil.createRepos(10, "foo", "bar", "owner")
        startSearchScreen(
            onLoadNextPage = {
                loadNextPageCalled = true
            },
            searchResults = Resource.success(repos)
        )
        composeTestRule.onNodeWithTag("searchResults").performScrollToIndex(9)
        composeTestRule.onNodeWithText("foo9").assertIsDisplayed()
        assertEquals(loadNextPageCalled, true)
    }

    @Test
    fun testLoadMoreState() {
        val repos = TestUtil.createRepos(10, "foo", "bar", "owner")
        startSearchScreen(
            searchResults = Resource.success(repos),
            loadMoreState = LoadMoreState(isRunning = true, hasMore = true, null)
        )
        composeTestRule.onNodeWithTag("searchResults").performScrollToIndex(10)
        composeTestRule.onNodeWithTag("progressbar").assertIsDisplayed()
    }

    @Test
    fun testLoadMoreStateError() {
        val repos = TestUtil.createRepos(10, "foo", "bar", "owner")
        startSearchScreen(
            searchResults = Resource.success(repos),
            loadMoreState = LoadMoreState(isRunning = false, hasMore = false, "idk")
        )
        composeTestRule.onNodeWithTag("searchResults").performScrollToIndex(10)
        composeTestRule.onAllNodesWithText("idk")[0].assertIsDisplayed()
    }

    @Test
    fun testLoadMoreStateNoMore() {
        val repos = TestUtil.createRepos(10, "foo", "bar", "owner")
        startSearchScreen(
            searchResults = Resource.success(repos),
            loadMoreState = LoadMoreState(isRunning = false, hasMore = false, null)
        )
        composeTestRule.onNodeWithTag("searchResults").performScrollToIndex(10)
        composeTestRule.onNodeWithText(getString(R.string.no_more_result)).assertIsDisplayed()
    }

    @Test
    fun testNavigateToDetail() {
        var clickRepoCalled = false
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startSearchScreen(
            onRepoClick = {
                assertEquals(it, repo)
                clickRepoCalled = true
            },
            searchResults = Resource.success(listOf(repo))
        )
        composeTestRule.onNodeWithText("foo").performClick()
        assertEquals(clickRepoCalled, true)
    }

    @Test
    fun testClickBookmark() {
        var clickBookmarkCalled = false
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startSearchScreen(
            onBookmarkClick = {
                assertEquals(it, repo)
                clickBookmarkCalled = true
            },
            searchResults = Resource.success(listOf(repo))
        )
        composeTestRule.onNodeWithTag(testTag = "bookmark", useUnmergedTree = true).performClick()
        assertEquals(clickBookmarkCalled, true)
    }

    private fun startSearchScreen(
        onRepoClick: (Repo) -> Unit = {},
        onBookmarkClick: (Repo) -> Unit = {},
        onSearch: (String) -> Unit = {},
        onLoadNextPage: () -> Unit = {},
        onRetry: () -> Unit = {},
        onRetryNextPage: () -> Unit = {},
        searchResults: Resource<List<Repo>>?,
        loadMoreState: LoadMoreState? = null
    ) {
        composeTestRule.setContent {
            SearchScreen(
                onRepoClick = onRepoClick,
                onBookmarkClick = onBookmarkClick,
                onSearch = onSearch,
                onLoadNextPage = onLoadNextPage,
                onRetry = onRetry,
                onRetryNextPage = onRetryNextPage,
                searchResults = searchResults,
                loadMoreState = loadMoreState
            )
        }
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String {
        return composeTestRule.activity.getString(id, *args)
    }

}