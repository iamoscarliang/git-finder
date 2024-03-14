package com.oscarliang.gitfinder.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "repos")
data class Repo(
    @PrimaryKey
    @field:SerializedName("id")
    val id: Int,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("description")
    val description: String?,
    @field:SerializedName("owner")
    @field:Embedded(prefix = "owner_")
    val owner: Owner,
    @field:SerializedName("stargazers_count")
    val stars: Int,
    @field:SerializedName("forks_count")
    val forks: Int,
    @field:SerializedName("language")
    val language: String?,
    @field:SerializedName("html_url")
    val url: String,
    val bookmark: Boolean = false
) {

    data class Owner(
        @field:SerializedName("login")
        val name: String,
        @field:SerializedName("avatar_url")
        val avatarUrl: String?
    )

}