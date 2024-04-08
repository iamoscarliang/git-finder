package com.oscarliang.gitfinder.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.util.MainDispatcherRule
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SearchViewModelTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutor = InstantTaskExecutorRule()

    private val repository = mockk<RepoRepository>(relaxed = true)
    private lateinit var viewModel: SearchViewModel

    @Before
    fun init() {
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun testSearchWithoutObserver() {
        viewModel.setQuery("foo", 10)
        verify { repository wasNot Called }
    }

    @Test
    fun testSearchWhenObserved() {
        viewModel.searchResults.observeForever(mockk())
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10) }
        clearMocks(repository)
        viewModel.setQuery("bar", 10)
        verify { repository.search("bar", 10) }
    }

    @Test
    fun testChangeWhileObserved() {
        viewModel.searchResults.observeForever(mockk())
        viewModel.setQuery("foo", 10)
        viewModel.setQuery("bar", 10)

        verify { repository.search("foo", 10) }
        verify { repository.search("bar", 10) }
    }

    @Test
    fun testSearchNextPageWithoutQuery() {
        viewModel.searchResults.observeForever(mockk())
        viewModel.loadNextPage()
        verify { repository wasNot Called }
    }

    @Test
    fun testSearchNextPageWithQuery() {
        viewModel.searchResults.observeForever(mockk())
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10) }
        clearMocks(repository)

        viewModel.loadNextPage()
        verify { repository.searchNextPage("foo", 10) }
    }

    @Test
    fun testSearchNextPageWhenChangeQuery() {
        val nextPage = MutableLiveData<Resource<Boolean>?>()
        every { repository.searchNextPage("foo", 10) } returns nextPage

        viewModel.searchResults.observeForever(mockk(relaxed = true))
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10) }

        viewModel.loadMoreState.observeForever(mockk(relaxed = true))
        viewModel.loadNextPage()
        verify { repository.searchNextPage("foo", 10) }
        assertEquals(nextPage.hasActiveObservers(), true)

        viewModel.setQuery("bar", 10)
        assertEquals(nextPage.hasActiveObservers(), false)
        verify { repository.search("bar", 10) }
        verify { repository.searchNextPage("bar", 10) wasNot Called }
    }

    @Test
    fun testRetry() {
        viewModel.retry()
        verify { repository wasNot Called }

        viewModel.setQuery("foo", 10)
        viewModel.retry()
        verify { repository wasNot Called }

        viewModel.searchResults.observeForever(mockk())
        verify { repository.search("foo", 10) }
        clearMocks(repository)

        viewModel.retry()
        verify { repository.search("foo", 10) }
    }

    @Test
    fun testBlankQuery() {
        viewModel.searchResults.observeForever(mockk(relaxed = true))
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10) }
        clearMocks(repository)

        viewModel.setQuery("", 10)
        verify { repository wasNot Called }
    }

    @Test
    fun testResetQuery() {
        val observer = mockk<Observer<SearchViewModel.Query>>(relaxed = true)
        viewModel.query.observeForever(observer)

        viewModel.setQuery("foo", 10)
        verify { observer.onChanged(SearchViewModel.Query("foo", 10)) }
        clearMocks(observer)

        viewModel.setQuery("foo", 10)
        verify { observer wasNot Called }
        viewModel.setQuery("bar", 10)
        verify { observer.onChanged(SearchViewModel.Query("bar", 10)) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testUpdate() = runTest {
        val current = TestUtil.createRepo("a", "b", "c")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        advanceUntilIdle()
        coVerify { repository.updateRepo(updated) }
    }

}