package com.oscarliang.gitfinder.db

import android.util.SparseIntArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.model.RepoSearchResult

@Dao
abstract class RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRepos(repositories: List<Repo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRepoSearchResults(result: RepoSearchResult)

    @Query("SELECT * FROM repo_search_results WHERE `query` = :query")
    abstract fun search(query: String): LiveData<RepoSearchResult?>

    @Query("SELECT * FROM repo_search_results WHERE `query` = :query")
    abstract fun getSearchResult(query: String): RepoSearchResult?

    @Query("DELETE FROM repo_search_results WHERE `query` = :query")
    abstract fun deleteSearchResult(query: String)

    fun getOrdered(repoIds: List<Int>): LiveData<List<Repo>> {
        val order = SparseIntArray()
        repoIds.withIndex().forEach {
            order.put(it.value, it.index)
        }
        return getReposById(repoIds).map { repositories ->
            repositories.sortedWith(compareBy { order.get(it.id) })
        }
    }

    @Query("SELECT * FROM repos WHERE id in (:repoIds)")
    protected abstract fun getReposById(repoIds: List<Int>): LiveData<List<Repo>>

    @Query("SELECT * FROM repos WHERE bookmark = 1")
    abstract fun getBookmarks(): LiveData<List<Repo>>

    @Update
    abstract suspend fun updateRepo(repo: Repo)

}