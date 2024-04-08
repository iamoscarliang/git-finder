package com.oscarliang.gitfinder.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.gitfinder.model.RepoSearchResult
import com.oscarliang.gitfinder.util.TestUtil
import com.oscarliang.gitfinder.util.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepoDaoTest : GithubDatabaseTest() {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInsertAndLoad() = runTest {
        val repos = TestUtil.createRepos(2, "foo", "bar", "owner")
        db.repoDao().insertRepos(repos)
        val dbData = db.repoDao().findReposById(listOf(0, 1))
        assertNotNull(dbData)
        assertEquals(dbData.size, 2)

        val repo1 = dbData[0]
        assertEquals(repo1.id, 0)
        assertEquals(repo1.name, "foo0")
        assertEquals(repo1.description, "bar0")
        assertEquals(repo1.owner.name, "owner0")

        val repo2 = dbData[1]
        assertEquals(repo2.id, 1)
        assertEquals(repo2.name, "foo1")
        assertEquals(repo2.description, "bar1")
        assertEquals(repo2.owner.name, "owner1")
    }


    @Test(expected = AssertionError::class)
    fun testInsertSearchResultWithoutRepo() = runTest {
        db.repoDao().insertRepoSearchResult(RepoSearchResult("foo", 2, listOf(1, 2)))
        throw AssertionError("Must fail because repo does not exist")
    }

    @Test
    fun testInsertSearchResult() = runTest {
        val repo = TestUtil.createRepos(2, "foo", "bar", "owner")
        val searchResults = RepoSearchResult("foo", 2, listOf(0, 1))
        db.withTransaction {
            db.repoDao().insertRepos(repo)
            db.repoDao().insertRepoSearchResult(searchResults)
        }

        val dbResult = db.repoDao().getRepoSearchResult("foo").getOrAwaitValue()
        val dbData = db.repoDao().getOrdered(dbResult!!.repoIds).getOrAwaitValue()
        assertNotNull(dbData)
        assertEquals(dbData.size, 2)

        val repo1 = dbData[0]
        assertEquals(repo1.id, 0)
        assertEquals(repo1.name, "foo0")
        assertEquals(repo1.description, "bar0")
        assertEquals(repo1.owner.name, "owner0")

        val repo2 = dbData[1]
        assertEquals(repo2.id, 1)
        assertEquals(repo2.name, "foo1")
        assertEquals(repo2.description, "bar1")
        assertEquals(repo2.owner.name, "owner1")
    }

}