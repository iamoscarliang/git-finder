package com.oscarliang.gitfinder.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.gitfinder.api.ApiResponse
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.api.RepoSearchResponse
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.db.RepoDao
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.model.RepoSearchResult
import com.oscarliang.gitfinder.util.AbsentLiveData
import com.oscarliang.gitfinder.util.MainDispatcherRule
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.mock
import retrofit2.Response

@RunWith(JUnit4::class)
class NewsRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: RepoRepository
    private val dao = mockk<RepoDao>(relaxed = true)
    private val service = mockk<GithubService>(relaxed = true)

    @Before
    fun init() {
        val db = mockk<GithubDatabase> {
            // We run the db transaction in ioDispatcher, but runInTransaction need
            // dependency of main looper. Whenever runInTransaction is called we take
            // the Runnable argument and run it
            every { runInTransaction(any()) } answers {
                firstArg<Runnable>().run()
            }
        }
        every { db.repoDao() } returns dao
        repository = RepoRepository(db, dao, service)
    }

    @Test
    fun searchNextPageNull() = runTest {
        every { dao.getSearchResult("foo") } returns null
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNextPage("foo", 10, this, StandardTestDispatcher())
            .observeForever(observer)
        advanceUntilIdle()   // Yields to perform the registrations
        verify { observer.onChanged(null) }
    }

    @Test
    fun searchFromDb() = runTest {
        val ids = listOf(1, 2)
        val observer = mockk<Observer<Resource<List<Repo>>>>(relaxed = true)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        every { dao.search("foo") } returns dbSearchResult
        val repositories = MutableLiveData<List<Repo>>()
        every { dao.getOrdered(ids) } returns repositories

        repository.search(
            "foo", 10, this,
            StandardTestDispatcher(testScheduler),
            mainDispatcherRule.testDispatcher
        ).observeForever(observer)

        verify { observer.onChanged(Resource.loading(null)) }
        verify { service wasNot Called }
        clearMocks(observer)

        val dbResult = RepoSearchResult("foo", ids)
        dbSearchResult.postValue(dbResult)

        val repoList = listOf<Repo>()
        repositories.postValue(repoList)
        verify { observer.onChanged(Resource.success(repoList)) }
        verify { service wasNot Called }
    }

    @Test
    fun searchFromServer() = runTest {
        val ids = listOf(1, 2)
        val repo1 = TestUtil.createRepo(1, "repo 1", "desc 1", "owner")
        val repo2 = TestUtil.createRepo(2, "repo 2", "desc 2", "owner")

        val observer = mockk<Observer<Resource<List<Repo>>>>(relaxed = true)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        every { dao.search("foo") } returns dbSearchResult
        val repositories = MutableLiveData<List<Repo>>()
        every { dao.getOrdered(ids) } returns repositories

        val repoList = listOf(repo1, repo2)
        val apiResponse = RepoSearchResponse(repoList)

        val callLiveData = MutableLiveData<ApiResponse<RepoSearchResponse>>()
        every { service.searchRepos("foo", 10) } returns callLiveData

        repository.search(
            "foo", 10, this,
            StandardTestDispatcher(testScheduler),
            mainDispatcherRule.testDispatcher
        ).observeForever(observer)

        verify { observer.onChanged(Resource.loading(null)) }
        verify { service wasNot Called }
        clearMocks(observer)

        dbSearchResult.postValue(null)
        verify { dao.getOrdered(any()) wasNot Called }
        verify { service.searchRepos("foo", 10) }

        val updatedResult = MutableLiveData<RepoSearchResult>()
        every { dao.search("foo") } returns updatedResult
        updatedResult.postValue(RepoSearchResult("foo", ids))

        callLiveData.postValue(ApiResponse.create(Response.success(apiResponse)))
        advanceUntilIdle()   // Yields to perform the registrations
        verify { dao.insertRepos(repoList) }
        repositories.postValue(repoList)
        verify { observer.onChanged(Resource.success(repoList)) }
        verify { service.searchRepos(any(), any()) wasNot Called }
    }

    @Test
    fun searchFromServerError() = runTest {
        every { dao.search("foo") } returns AbsentLiveData.create()
        val apiResponse = MutableLiveData<ApiResponse<RepoSearchResponse>>()
        every { service.searchRepos("foo", 10) } returns apiResponse

        val observer = mockk<Observer<Resource<List<Repo>>>>(relaxed = true)
        repository.search(
            "foo", 10, this,
            StandardTestDispatcher(testScheduler),
            mainDispatcherRule.testDispatcher
        ).observeForever(observer)
        verify { observer.onChanged(Resource.loading(null)) }

        apiResponse.postValue(ApiResponse.create(Exception("idk")))
        verify { observer.onChanged(Resource.error("idk", null)) }
    }

}