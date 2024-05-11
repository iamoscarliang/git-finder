package com.oscarliang.gitfinder.ui.bookmarks

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.util.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarksScreenTest {

    @Rule
    @JvmField
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testEmptyBookmarks() {
        startBookmarksScreen(bookmarks = emptyList())
        composeTestRule.onNodeWithText(getString(R.string.no_bookmarks)).assertIsDisplayed()
    }

    @Test
    fun testLoadBookmarks() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startBookmarksScreen(bookmarks = listOf(repo))
        composeTestRule.onNodeWithText("foo").assertIsDisplayed()
    }

    @Test
    fun testNavigateToDetail() {
        var clickRepoCalled = false
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startBookmarksScreen(
            onRepoClick = {
                assertEquals(it, repo)
                clickRepoCalled = true
            },
            bookmarks = listOf(repo)
        )
        composeTestRule.onNodeWithText("foo").performClick()
        assertEquals(clickRepoCalled, true)
    }

    @Test
    fun testClickBookmark() {
        var clickBookmarkCalled = false
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        startBookmarksScreen(
            onBookmarkClick = {
                assertEquals(it, repo)
                clickBookmarkCalled = true
            },
            bookmarks = listOf(repo)
        )
        composeTestRule.onNodeWithTag(testTag = "bookmark", useUnmergedTree = true).performClick()
        assertEquals(clickBookmarkCalled, true)
    }

    private fun startBookmarksScreen(
        onRepoClick: (Repo) -> Unit = {},
        onBookmarkClick: (Repo) -> Unit = {},
        bookmarks: List<Repo>
    ) {
        composeTestRule.setContent {
            BookmarksScreen(
                onRepoClick = onRepoClick,
                onBookmarkClick = onBookmarkClick,
                bookmarks = bookmarks
            )
        }
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String {
        return composeTestRule.activity.getString(id, *args)
    }

}