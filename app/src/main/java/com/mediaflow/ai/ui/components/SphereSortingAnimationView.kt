package com.mediaflow.ai.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mediaflow.ai.ui.theme.BlueAccent
import com.mediaflow.ai.ui.theme.PurplePrimary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class SphereParticle(
    val phi: Float,
    val theta: Float,
    val color: Color
)

data class FolderTarget(
    val id: String,
    val name: String,
    val iconColor: Color,
    var count: Int
)

@Composable
fun SphereSortingAnimationView(
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(true) }
    var sortedCount by remember { mutableIntStateOf(1420) }
    var targetSize by remember { mutableIntStateOf(17233) }
    var screenshotsDocsCount by remember { mutableIntStateOf(340) }
    var duplicatesCount by remember { mutableIntStateOf(128) }
    var activeCategoryName by remember { mutableStateOf("Travel") }
    var activeMatchPercent by remember { mutableIntStateOf(96) }
    var activePhotoIndex by remember { mutableIntStateOf(0) }

    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=400&q=80"
    )

    val folders = remember {
        mutableStateListOf(
            FolderTarget("1", "Selfies", Color(0xFFEC4899), 184),
            FolderTarget("2", "Events & weddings", Color(0xFF8B5CF6), 92),
            FolderTarget("3", "Travel", Color(0xFF3B82F6), 341),
            FolderTarget("4", "Screenshots", Color(0xFF6366F1), 215),
            FolderTarget("5", "Receipts & docs", Color(0xFF64748B), 125),
            FolderTarget("6", "Duplicates", Color(0xFFEF4444), 128),
            FolderTarget("7", "Family & friends", Color(0xFFA855F7), 246),
            FolderTarget("8", "Food", Color(0xFFF59E0B), 108),
            FolderTarget("9", "Nature & views", Color(0xFF10B981), 412),
            FolderTarget("10", "Animals & pets", Color(0xFF14B8A6), 87),
            FolderTarget("11", "Things & shopping", Color(0xFF06B6D4), 64)
        )
    }

    var selectedFolderIndex by remember { mutableIntStateOf(2) }
    var folderPulseIndex by remember { mutableIntStateOf(-1) }

    // 3D Orbit particles
    val particles = remember {
        val list = mutableListOf<SphereParticle>()
        val count = 36
        val palette = listOf(
            Color(0xFF7C3AED), Color(0xFF2563EB), Color(0xFFEC4899),
            Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF06B6D4)
        )
        for (i in 0 until count) {
            val phi = Math.acos(-1.0 + (2.0 * i) / count).toFloat()
            val theta = (Math.sqrt(count * PI) * phi).toFloat()
            list.add(SphereParticle(phi, theta, palette[i % palette.size]))
        }
        list
    }

    // Infinite rotation
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_rot")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    // Ray pulse animation
    val rayDashOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash"
    )

    // Flying particle transition
    val flyAnim = remember { Animatable(0f) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1600)
            val nextFolder = (0 until folders.size).random()
            selectedFolderIndex = nextFolder
            activeCategoryName = folders[nextFolder].name
            activeMatchPercent = (92..99).random()
            activePhotoIndex = (activePhotoIndex + 1) % samplePhotos.size

            flyAnim.snapTo(0f)
            flyAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )

            // Increment folder count
            folders[nextFolder].count += 1
            folderPulseIndex = nextFolder
            sortedCount += 1
            if (activeCategoryName.contains("Screenshot") || activeCategoryName.contains("Receipt")) {
                screenshotsDocsCount += 1
            }
            if (activeCategoryName.contains("Duplicate")) {
                duplicatesCount += 1
            }

            delay(250)
            folderPulseIndex = -1
        }
    }

    GlassmorphicSurface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title & Pause/Play
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(PurplePrimary, BlueAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Neural Sorter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "3D Spherical Vector Indexing",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) Color(0xFFF59E0B) else PurplePrimary)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Target Size", value = "$targetSize")
                StatItem(label = "Photos sorted", value = "$sortedCount", color = PurplePrimary)
                StatItem(label = "Screenshots", value = "$screenshotsDocsCount")
                StatItem(label = "Duplicates", value = "$duplicatesCount", color = Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central 3D Sphere & Spotlight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background 3D Rotating Sphere Orbit
                Canvas(
                    modifier = Modifier.size(230.dp)
                ) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width * 0.42f
                    val rad = (rotationAngle * PI / 180.0).toFloat()

                    // Draw outer subtle glowing orbit ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(PurplePrimary.copy(alpha = 0.12f), Color.Transparent),
                            center = centerOffset,
                            radius = radius * 1.2f
                        ),
                        radius = radius * 1.1f,
                        center = centerOffset
                    )

                    // Draw particles projected onto 2D plane with perspective
                    particles.forEach { p ->
                        val x = radius * sin(p.phi) * cos(p.theta + rad)
                        val y = radius * sin(p.phi) * sin(p.theta + rad) * 0.35f + radius * cos(p.phi) * 0.9f
                        val z = radius * sin(p.phi) * sin(p.theta + rad)

                        // Perspective scale & alpha
                        val scale = (z + radius) / (2f * radius) // 0 to 1
                        val particleRadius = 3f + (scale * 7f)
                        val alpha = 0.25f + (scale * 0.75f)

                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = particleRadius,
                            center = Offset(centerOffset.x + x, centerOffset.y + y)
                        )
                    }

                    // Trajectory vector beam from center to target
                    val trajPathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), rayDashOffset)
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = centerOffset,
                        end = Offset(centerOffset.x, size.height - 10f),
                        strokeWidth = 3f,
                        pathEffect = trajPathEffect
                    )
                }

                // Center Spotlight Card
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = PurplePrimary)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(3.dp, Color.White, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(samplePhotos[activePhotoIndex])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Spotlight Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Spotlight AI Match Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$activeCategoryName $activeMatchPercent%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Flying Dispersal Card
                if (flyAnim.value > 0f && flyAnim.value < 1f) {
                    val flyProgress = flyAnim.value
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = (flyProgress * 180).toInt()
                                )
                            }
                            .scale(1f - (flyProgress * 0.45f))
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                            .graphicsLayer { alpha = 1f - flyProgress }
                    ) {
                        AsyncImage(
                            model = samplePhotos[activePhotoIndex],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Folders Horizontal Strip
            Text(
                text = "Target Categories (${folders.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                folders.forEachIndexed { index, folder ->
                    val isSelected = index == selectedFolderIndex
                    val isPulsing = index == folderPulseIndex
                    val cardScale = if (isPulsing) 1.08f else if (isSelected) 1.04f else 1.0f

                    Box(
                        modifier = Modifier
                            .scale(cardScale)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) folder.iconColor.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) folder.iconColor else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = folder.iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = folder.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${folder.count}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = folder.iconColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color = Color.Unspecified
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
