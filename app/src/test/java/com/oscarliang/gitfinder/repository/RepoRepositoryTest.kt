package com.oscarliang.gitfinder.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.withTransaction
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.api.RepoSearchResponse
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.db.RepoDao
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.model.RepoSearchResult
import com.oscarliang.gitfinder.util.MainDispatcherRule
import com.oscarliang.gitfinder.util.RateLimiter
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class RepoRepositoryTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dao = mockk<RepoDao>(relaxed = true)
    private val service = mockk<GithubService>(relaxed = true)
    private val rateLimiter = mockk<RateLimiter<String>>(relaxed = true)
    private lateinit var repository: RepoRepository

    @Before
    fun init() {
        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
        val db = mockk<GithubDatabase>(relaxed = true)
        val transaction = slot<suspend () -> Unit>()
        coEvery { db.withTransaction(capture(transaction)) } coAnswers {
            transaction.captured.invoke()
        }
        every { db.repoDao() } returns dao
        repository = RepoRepository(
            db = db,
            repoDao = dao,
            service = service,
            rateLimiter = rateLimiter
        )
    }

    @Test
    fun testSearchFromDb() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns false
        val ids = listOf(0, 1)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        every { dao.getRepoSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<Repo>>()
        every { dao.getOrdered(ids) } returns dbData
        coEvery { dao.findRepoSearchResult("foo") } returns null

        val observer = mockk<Observer<Resource<List<Repo>>>>(relaxed = true)
        repository.search("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = RepoSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        val repos = TestUtil.createRepos(2, "foo", "bar", "owner")
        dbData.postValue(repos)
        verify { observer.onChanged(Resource.success(repos)) }
        verify { service wasNot Called }
    }

    @Test
    fun testSearchFromNetwork() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf(0, 1)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        every { dao.getRepoSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<Repo>>()
        every { dao.getOrdered(ids) } returns dbData
        val repos = TestUtil.createRepos(2, "foo", "bar", "owner")
        coEvery { dao.findRepoSearchResult("foo") } returns null
        val response = RepoSearchResponse(2, repos)
        coEvery { service.searchRepos("foo", 10) } returns response

        val observer = mockk<Observer<Resource<List<Repo>>>>(relaxed = true)
        repository.search("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = RepoSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(repos)
        coVerify { service.searchRepos("foo", 10) }
        coVerify { dao.insertRepos(repos) }
        coVerify { dao.insertRepoSearchResult(searchResult) }
        verify { observer.onChanged(Resource.success(repos)) }
    }

    @Test
    fun searchFromNetworkError() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf(0, 1)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        every { dao.getRepoSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<Repo>>()
        every { dao.getOrdered(ids) } returns dbData
        val repos = TestUtil.createRepos(2, "foo", "bar", "owner")
        coEvery { dao.findRepoSearchResult("foo") } returns null
        coEvery { service.searchRepos("foo", 10) } throws Exception("idk")

        val observer = mockk<Observer<Resource<List<Repo>>>>(relaxed = true)
        repository.search("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = RepoSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(repos)
        coVerify { service.searchRepos("foo", 10) }
        coVerify { observer.onChanged(Resource.error("idk", repos)) }
        verify { rateLimiter.reset("foo") }
    }

    @Test
    fun testSearchNextPageNull() = runTest {
        coEvery { dao.findRepoSearchResult("foo") } returns null
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNextPage("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(null) }
    }

    @Test
    fun testSearchNextPageFalse() = runTest {
        val ids = listOf(1, 2)
        val searchResult = RepoSearchResult("foo", 2, ids)
        coEvery { dao.findRepoSearchResult("foo") } returns searchResult
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNextPage("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(false)) }
    }

    @Test
    fun testSearchNextPageTrue() = runTest {
        val ids = listOf(1, 2)
        val searchResult = RepoSearchResult("foo", 10, ids)
        coEvery { dao.findRepoSearchResult("foo") } returns searchResult
        val repos = TestUtil.createRepos(2, "foo", "bar", "owner")
        val response = RepoSearchResponse(2, repos)
        coEvery { service.searchRepos("foo", 10, 2) } returns response

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNextPage("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(true)) }
    }

}