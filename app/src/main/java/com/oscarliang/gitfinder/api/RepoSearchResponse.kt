package com.oscarliang.gitfinder.api

import com.google.gson.annotations.SerializedName
import com.oscarliang.gitfinder.model.Repo

data class RepoSearchResponse(
    @SerializedName("total_count")
    val count: Int = 0,
    @SerializedName("items")
    val items: List<Repo>
)