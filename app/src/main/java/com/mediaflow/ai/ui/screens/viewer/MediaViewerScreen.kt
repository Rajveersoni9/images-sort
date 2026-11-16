package com.mediaflow.ai.ui.screens.viewer

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mediaflow.ai.domain.model.MediaItem
import com.mediaflow.ai.ui.components.GlassmorphicSurface
import com.mediaflow.ai.ui.theme.FavoriteRed
import com.mediaflow.ai.ui.theme.PurplePrimary

@Composable
fun MediaViewerScreen(
    mediaId: Long,
    viewModel: MediaViewerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(mediaId) {
        viewModel.loadMedia(mediaId)
    }

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var controlsVisible by remember { mutableStateOf(true) }

    if (state.mediaItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.mediaItems.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Horizontal Pager for swiping between media
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = state.mediaItems[page]
            ZoomableMediaViewer(
                item = item,
                onSingleTap = { controlsVisible = !controlsVisible },
                onSwipeDownDismiss = onBackClick
            )
        }

        // Top Navigation Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassmorphicSurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0x66000000),
                    borderColor = Color(0x33FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "${pagerState.currentPage + 1} of ${state.mediaItems.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            val isFav = state.currentItem?.isFavorite == true
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) FavoriteRed else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Bar Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                GlassmorphicSurface(
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = Color(0x66000000),
                    borderColor = Color(0x33FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share
                        ViewerActionItem(
                            icon = Icons.Rounded.Share,
                            label = "Share",
                            onClick = {
                                val current = state.currentItem ?: return@ViewerActionItem
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = current.mimeType
                                    putExtra(Intent.EXTRA_STREAM, current.uri)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                            }
                        )

                        // Edit
                        ViewerActionItem(
                            icon = Icons.Rounded.Edit,
                            label = "Edit",
                            onClick = { /* Launch editor */ }
                        )

                        // Info / Details BottomSheet
                        ViewerActionItem(
                            icon = Icons.Rounded.Info,
                            label = "Info",
                            onClick = { viewModel.showMetadataSheet(true) }
                        )

                        // Delete
                        ViewerActionItem(
                            icon = Icons.Rounded.Delete,
                            label = "Delete",
                            tint = FavoriteRed,
                            onClick = {
                                viewModel.deleteCurrent {
                                    if (state.mediaItems.size <= 1) onBackClick()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Metadata Bottom Sheet
        if (state.showMetadataSheet && state.currentItem != null) {
            MetadataBottomSheet(
                mediaItem = state.currentItem!!,
                onDismiss = { viewModel.showMetadataSheet(false) }
            )
        }
    }
}

@Composable
private fun ZoomableMediaViewer(
    item: MediaItem,
    onSingleTap: () -> Unit,
    onSwipeDownDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = {
                        scale = if (scale > 1.2f) 1f else 2.5f
                        offset = Offset.Zero
                    }
                )
            }
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}

@Composable
private fun ViewerActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint.copy(alpha = 0.85f)
        )
    }
}
