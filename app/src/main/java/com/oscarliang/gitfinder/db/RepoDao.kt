package com.oscarliang.gitfinder.db

import android.util.SparseIntArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.model.RepoSearchResult

@Dao
abstract class RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRepos(repositories: List<Repo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(result: RepoSearchResult)

    @Query("SELECT * FROM repo_search_results WHERE `query` = :query")
    abstract fun search(query: String): LiveData<RepoSearchResult?>

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

    @Query("SELECT * FROM repos WHERE id = :repoId")
    abstract fun getRepoById(repoId: Int): LiveData<Repo>

    @Query("DELETE FROM repo_search_results WHERE `query` = :query")
    abstract fun deleteAllRepoSearchResults(query: String)

}