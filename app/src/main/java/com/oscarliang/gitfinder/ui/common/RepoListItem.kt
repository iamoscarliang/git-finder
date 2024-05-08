package com.oscarliang.gitfinder.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.ui.theme.GitFinderTheme
import com.oscarliang.gitfinder.ui.theme.Yellow

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.RepoListItem(
    repo: Repo,
    onRepoClick: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = dimensionResource(id = R.dimen.margin_small),
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.layout_repo_item_height))
            .animateItemPlacement()
            .clickable { onRepoClick() }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.margin_large))
        ) {
            Row {
                GlideImage(
                    model = repo.owner.avatarUrl,
                    loading = placeholder(R.drawable.ic_gitfinder),
                    failure = placeholder(R.drawable.ic_error),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .width(dimensionResource(id = R.dimen.image_owner_width))
                        .height(dimensionResource(id = R.dimen.image_owner_width)),
                    contentDescription = "Owner"
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_medium)))
                Text(
                    text = repo.owner.name,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
            Text(
                text = repo.name,
                textAlign = TextAlign.Start,
                maxLines = 1,
                color = MaterialTheme.colors.onSecondary,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
            Text(
                text = repo.description ?: "",
                textAlign = TextAlign.Start,
                maxLines = 2,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    tint = Yellow,
                    contentDescription = "Star",
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.image_icon_width))
                        .height(dimensionResource(id = R.dimen.image_icon_width))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_small)))
                Text(
                    text = formatText(repo.stars),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_large)))
                Icon(
                    painter = painterResource(R.drawable.ic_fork),
                    tint = MaterialTheme.colors.secondary,
                    contentDescription = "Fork",
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.image_icon_width))
                        .height(dimensionResource(id = R.dimen.image_icon_width))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_small)))
                Text(
                    text = formatText(repo.forks),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_large)))
                Icon(
                    painter = painterResource(R.drawable.ic_code),
                    tint = MaterialTheme.colors.secondary,
                    contentDescription = "Code",
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.image_icon_width))
                        .height(dimensionResource(id = R.dimen.image_icon_width))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_small)))
                Text(
                    text = repo.language ?: "",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { onBookmarkClick() },
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.btn_bookmark_width))
                        .height(dimensionResource(id = R.dimen.btn_bookmark_width))
                ) {
                    Icon(
                        painter = painterResource(
                            if (repo.bookmark) {
                                R.drawable.ic_bookmark
                            } else {
                                R.drawable.ic_bookmark_border
                            }
                        ),
                        tint = if (repo.bookmark) {
                            MaterialTheme.colors.primaryVariant
                        } else {
                            MaterialTheme.colors.secondary
                        },
                        contentDescription = "Bookmark",
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.btn_bookmark_width))
                            .height(dimensionResource(id = R.dimen.btn_bookmark_width))
                    )
                }
            }
        }
    }
}

private fun formatText(count: Int): String {
    val text = if (count >= 1000) {
        val format = count / 1000
        "${format}k"
    } else {
        count.toString()
    }
    return text
}

@Preview
@Composable
private fun RepoListItemPreview() {
    GitFinderTheme {
        LazyVerticalGrid(columns = GridCells.Fixed(1)) {
            item(1) {
                RepoListItem(
                    repo = Repo(
                        1,
                        "Repo1",
                        "Desc1",
                        Repo.Owner("Owner1", null),
                        10,
                        20,
                        "Kotlin",
                        ""
                    ),
                    onRepoClick = {},
                    onBookmarkClick = {}
                )
            }
        }
    }
}
