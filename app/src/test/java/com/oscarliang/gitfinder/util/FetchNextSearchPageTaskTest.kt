package com.oscarliang.gitfinder.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.withTransaction
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.api.RepoSearchResponse
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.db.RepoDao
import com.oscarliang.gitfinder.model.RepoSearchResult
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
class FetchNextSearchPageTaskTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dao = mockk<RepoDao>(relaxed = true)
    private val service = mockk<GithubService>(relaxed = true)
    private lateinit var fetchNextSearchPageTask: FetchNextSearchPageTask

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
        fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = "foo",
            number = 2,
            db = db,
            repoDao = dao,
            service = service
        )
    }

    @Test
    fun testSearchNextPageNull() = runTest {
        coEvery { dao.findRepoSearchResult("foo") } returns null
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(null) }
    }

    @Test
    fun testSearchNextPageFalse() = runTest {
        val ids = listOf(0, 1)
        val searchResult = RepoSearchResult("foo", 2, ids)
        coEvery { dao.findRepoSearchResult("foo") } returns searchResult

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(false)) }
    }

    @Test
    fun testSearchNextPageTrue() = runTest {
        val ids = listOf(2, 3)
        val searchResult = RepoSearchResult("foo", 4, ids)
        coEvery { dao.findRepoSearchResult("foo") } returns searchResult
        val repos = TestUtil.createRepos(2, "foo", "bar", "owner")
        val response = RepoSearchResponse(4, repos)
        coEvery { service.searchRepos("foo", 2, 2) } returns response

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()

        coVerify { service.searchRepos("foo", 2, 2) }
        coVerify { dao.insertRepos(repos) }
        val updatedResult = RepoSearchResult("foo", 4, listOf(2, 3, 0, 1))
        coVerify { dao.insertRepoSearchResult(updatedResult) }
        verify { observer.onChanged(Resource.success(true)) }
    }

    @Test
    fun testSearchNextPageTrueError() = runTest {
        val ids = listOf(0, 1)
        val searchResult = RepoSearchResult("foo", 4, ids)
        coEvery { dao.findRepoSearchResult("foo") } returns searchResult
        coEvery { service.searchRepos("foo", 2, 2) } throws Exception("idk")

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.error("idk", true)) }
    }

}