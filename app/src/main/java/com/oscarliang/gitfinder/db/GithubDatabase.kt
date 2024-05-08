package com.oscarliang.gitfinder.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.model.RepoSearchResult

@Database(
    entities = [Repo::class, RepoSearchResult::class],
    version = 1,
    exportSchema = false
)
abstract class GithubDatabase : RoomDatabase() {

    abstract fun repoDao(): RepoDao

}