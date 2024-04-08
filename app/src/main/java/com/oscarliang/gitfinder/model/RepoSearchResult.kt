package com.oscarliang.gitfinder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.oscarliang.gitfinder.db.GithubTypeConverters

@Entity(tableName = "repo_search_results")
@TypeConverters(GithubTypeConverters::class)
data class RepoSearchResult(
    @PrimaryKey
    val query: String,
    val count: Int,
    val repoIds: List<Int>
)