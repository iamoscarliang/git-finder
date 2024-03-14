package com.oscarliang.gitfinder.api

import com.google.gson.annotations.SerializedName
import com.oscarliang.gitfinder.model.Repo

data class RepoSearchResponse(
    @SerializedName("items")
    val items: List<Repo>
)