package com.oscarliang.gitfinder.ui.search

import android.content.Context
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.util.DataBindingIdlingResourceRule
import com.oscarliang.gitfinder.util.EspressoTestUtil.nestedScrollTo
import com.oscarliang.gitfinder.util.RecyclerViewMatcher
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule<SearchFragment>()

    private lateinit var navController: NavController
    private lateinit var viewModel: SearchViewModel
    private val searchResults = MutableLiveData<Resource<List<Repo>>>()
    private val loadMoreState = MutableLiveData<SearchViewModel.LoadMoreState>()

    @Before
    fun init() {
        navController = mockk<NavController>(relaxed = true)
        viewModel = mockk<SearchViewModel>(relaxed = true)
        every { viewModel.searchResults } returns searchResults
        every { viewModel.loadMoreState } returns loadMoreState
        val scenario = launchFragmentInContainer(themeResId = R.style.Theme_GitFinder) {
            // Use a TestRunner to load a empty module and inject
            // a mock ViewModel into fragment to verify interaction
            loadKoinModules(module {
                viewModel {
                    viewModel
                }
            })
            SearchFragment()
        }
        dataBindingIdlingResourceRule.monitorFragment(scenario)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun testBlankSearch() {
        onView(withId(R.id.edit_search)).perform(
            typeText(" "),
            pressKey(KeyEvent.KEYCODE_ENTER)
        )
        // Test is the snack bar show when searching blank input
        onView(withText(getString(R.string.empty_search))).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchLoading() {
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_search)).perform(
            typeText("foo"),
            pressKey(KeyEvent.KEYCODE_ENTER)
        )
        verify { viewModel.setQuery("foo", any()) }
        searchResults.postValue(Resource.loading(null))
        onView(withId(R.id.shimmer_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchLoadingWithData() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        searchResults.postValue(Resource.loading(listOf(repo)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun testSearchSuccess() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        searchResults.postValue(Resource.success(listOf(repo)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun testSearchError() {
        searchResults.postValue(Resource.error("idk", null))
        onView(withId(R.id.text_error)).check(matches(isDisplayed()))
        onView(withId(R.id.text_error)).check(matches(withText("idk")))
    }

    @Test
    fun testSearchNull() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        searchResults.postValue(Resource.success(listOf(repo)))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
        searchResults.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    @Test
    fun testRefresh() {
        searchResults.postValue(Resource.error("idk", null))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipe_refresh_layout)).perform(swipeDown())
        verify { viewModel.retry() }

        searchResults.postValue(Resource.loading(null))
        onView(withId(R.id.shimmer_layout)).check(matches(isDisplayed()))

        val repo = TestUtil.createRepo("foo", "bar", "owner")
        searchResults.postValue(Resource.success(listOf(repo)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun testSearchNextPage() {
        val repos = TestUtil.createRepos(10, "foo", "bar", "owner")
        searchResults.postValue(Resource.success(repos))
        onView(listMatcher().atPosition(9)).perform(nestedScrollTo())
        onView(listMatcher().atPosition(9)).check(matches(isDisplayed()))
        verify { viewModel.loadNextPage() }
    }

    @Test
    fun testLoadMoreState() {
        loadMoreState.postValue(SearchViewModel.LoadMoreState(true, true, null))
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()))
        loadMoreState.postValue(SearchViewModel.LoadMoreState(false, true, null))
        onView(withId(R.id.progressbar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testLoadMoreStateError() {
        loadMoreState.postValue(SearchViewModel.LoadMoreState(true, false, "idk"))
        onView(withText("idk")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testNavigateToDetail() {
        val repo = TestUtil.createRepo("foo", "bar", "owner").copy(url = "abc")
        searchResults.postValue(Resource.success(listOf(repo)))
        onView(withText("foo")).perform(click())
        verify { navController.navigate(SearchFragmentDirections.actionToDetailFragment("abc")) }
    }

    @Test
    fun testBookmark() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        searchResults.postValue(Resource.success(listOf(repo)))
        onView(withId(R.id.btn_bookmark)).perform(click())
        verify { viewModel.toggleBookmark(repo) }
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.repo_list)
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String {
        return ApplicationProvider.getApplicationContext<Context>().getString(id, *args)
    }

}