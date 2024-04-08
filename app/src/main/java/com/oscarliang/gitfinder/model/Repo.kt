package com.oscarliang.gitfinder.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "repos")
data class Repo(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("owner")
    @Embedded(prefix = "owner_")
    val owner: Owner,
    @SerializedName("stargazers_count")
    val stars: Int,
    @SerializedName("forks_count")
    val forks: Int,
    @SerializedName("language")
    val language: String?,
    @SerializedName("html_url")
    val url: String,
    var bookmark: Boolean = false
) {

    data class Owner(
        @SerializedName("login")
        val name: String,
        @SerializedName("avatar_url")
        val avatarUrl: String?
    )

}