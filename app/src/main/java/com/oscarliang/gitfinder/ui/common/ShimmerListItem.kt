package com.oscarliang.gitfinder.ui.common

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.IntSize
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.ui.theme.Gray500
import com.oscarliang.gitfinder.ui.theme.Gray700

@Composable
fun ShimmerListItem() {
    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_large))) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .height(dimensionResource(id = R.dimen.margin_large))
                .clip(MaterialTheme.shapes.small)
                .alpha(0.5f)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(dimensionResource(id = R.dimen.margin_large))
                .clip(MaterialTheme.shapes.small)
                .alpha(0.5f)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(dimensionResource(id = R.dimen.margin_large))
                .clip(MaterialTheme.shapes.small)
                .alpha(0.5f)
                .shimmerEffect()
        )
    }
}

@Suppress("InfiniteTransitionLabel", "InfinitePropertiesLabel")
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Gray500,
                Gray700,
                Gray500,
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}