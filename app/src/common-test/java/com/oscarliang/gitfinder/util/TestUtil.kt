package com.oscarliang.gitfinder.util

import com.oscarliang.gitfinder.model.Repo

private const val UNKNOWN_ID = -1

object TestUtil {

    fun createRepos(
        count: Int,
        name: String,
        description: String,
        owner: String,
    ): List<Repo> {
        return (0 until count).map {
            createRepo(
                name = name + it,
                description = description + it,
                owner = owner + it
            )
        }
    }

    fun createRepo(
        name: String,
        description: String,
        owner: String,
    ) = createRepo(
        id = UNKNOWN_ID,
        name = name,
        description = description,
        owner = owner
    )

    fun createRepo(
        id: Int,
        name: String,
        description: String,
        owner: String,
    ) = Repo(
        id = id,
        name = name,
        description = description,
        owner = Repo.Owner(owner, null),
        stars = 3,
        forks = 3,
        language = null,
        url = "",
        bookmark = false
    )

}