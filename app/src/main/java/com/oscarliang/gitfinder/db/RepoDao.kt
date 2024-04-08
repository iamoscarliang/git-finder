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
interface RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepos(repositories: List<Repo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepoSearchResult(result: RepoSearchResult)
    
    @Query("SELECT * FROM repo_search_results WHERE `query` = :query")
    suspend fun findRepoSearchResult(query: String): RepoSearchResult?

    @Query("SELECT * FROM repo_search_results WHERE `query` = :query")
    fun getRepoSearchResult(query: String): LiveData<RepoSearchResult?>

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
    suspend fun findReposById(repoIds: List<Int>): List<Repo>

    @Query("SELECT * FROM repos WHERE id in (:repoIds)")
    fun getReposById(repoIds: List<Int>): LiveData<List<Repo>>

    @Query("SELECT * FROM repos WHERE id = :repoId")
    fun getRepoById(repoId: Int): LiveData<Repo>

    @Query("SELECT * FROM repos WHERE bookmark = 1")
    suspend fun findBookmarks(): List<Repo>

    @Query("SELECT * FROM repos WHERE bookmark = 1")
    fun getBookmarks(): LiveData<List<Repo>>

    @Update
    suspend fun updateRepo(repo: Repo)

}