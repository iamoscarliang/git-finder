package com.oscarliang.gitfinder.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.gitfinder.model.RepoSearchResult
import com.oscarliang.gitfinder.util.TestUtil
import com.oscarliang.gitfinder.util.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepoDaoTest : GithubDatabaseTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertAndRead() {
        val repo = TestUtil.createRepo(1, "foo", "bar", "owner")
        db.repoDao().insertRepos(listOf(repo))
        val loaded = db.repoDao().getRepoById(1).getOrAwaitValue()
        assertNotNull(loaded)
        assertEquals(loaded.name, "foo")
        assertEquals(loaded.description, "bar")
        assertEquals(loaded.owner.name, "owner")
    }


    @Test(expected = AssertionError::class)
    fun insertSearchResultWithoutRepo() {
        db.repoDao().insertRepoSearchResults(RepoSearchResult("foo", listOf(1, 2)))
        throw AssertionError("Must fail because repo does not exist")
    }

    @Test
    fun insertSearchResult() {
        val repo = TestUtil.createRepos(2, "foo", "bar", "owner")
        val searchResults = RepoSearchResult("foo", listOf(0, 1))
        db.runInTransaction {
            db.repoDao().insertRepos(repo)
            db.repoDao().insertRepoSearchResults(searchResults)
        }

        val result = db.repoDao().search("foo").getOrAwaitValue()
        val list = db.repoDao().getOrdered(result!!.repoIds).getOrAwaitValue()
        assertEquals(list.size, 2)

        val first = list[0]
        assertEquals(first.name, "foo0")
        assertEquals(first.description, "bar0")

        val second = list[1]
        assertEquals(second.name, "foo1")
        assertEquals(second.description, "bar1")
    }

}