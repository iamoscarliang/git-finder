package com.oscarliang.gitfinder.ui.bookmarks

import androidx.databinding.DataBindingComponent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.binding.FragmentBindingAdapters
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.util.DataBindingIdlingResourceRule
import com.oscarliang.gitfinder.util.RecyclerViewMatcher
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class BookmarksFragmentTest {

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule<BookmarksFragment>()

    private lateinit var navController: NavController
    private lateinit var viewModel: BookmarksViewModel
    private lateinit var mockBindingAdapter: FragmentBindingAdapters
    private val repoLiveData = MutableLiveData<List<Repo>>()

    @Before
    fun init() {
        navController = mockk<NavController>(relaxed = true)
        viewModel = mockk<BookmarksViewModel>(relaxed = true)
        mockBindingAdapter = mockk<FragmentBindingAdapters>(relaxed = true)
        every { viewModel.bookmarks } returns repoLiveData
        val scenario = launchFragmentInContainer {
            // Use a TestRunner to load a empty module and inject
            // a mock ViewModel into fragment to verify interaction
            loadKoinModules(module {
                viewModel {
                    viewModel
                }
            })
            BookmarksFragment().apply {
                dataBindingComponent = object : DataBindingComponent {
                    override fun getFragmentBindingAdapters(): FragmentBindingAdapters {
                        return mockBindingAdapter
                    }
                }
            }
        }
        dataBindingIdlingResourceRule.monitorFragment(scenario)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun testLoaded() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        repoLiveData.postValue(listOf(repo))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun clickNews() {
        val repo = TestUtil.createRepo("foo", "bar", "owner").copy(url = "abc")
        repoLiveData.postValue(listOf(repo))
        onView(withText("foo")).perform(click())
        verify { navController.navigate(BookmarksFragmentDirections.actionToNewsDetailFragment("abc")) }
    }

    @Test
    fun clickBookmark() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        repoLiveData.postValue(listOf(repo))
        onView(withId(R.id.btn_bookmark)).perform(click())
        verify { viewModel.toggleBookmark(repo) }
    }

    @Test
    fun nullNews() {
        val repo = TestUtil.createRepo("foo", "bar", "owner")
        repoLiveData.postValue(listOf(repo))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
        repoLiveData.postValue(null)
        onView(listMatcher().atPosition(0)).check(ViewAssertions.doesNotExist())
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.repo_list)
    }

}