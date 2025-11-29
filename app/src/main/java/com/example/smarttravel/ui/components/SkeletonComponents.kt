package com.example.smarttravel.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color(0xFFE0E0E0).copy(alpha = 0.6f),
        Color(0xFFF5F5F5).copy(alpha = 0.8f),
        Color(0xFFE0E0E0).copy(alpha = 0.6f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(ShimmerBrush())
    )
}

@Composable
fun SkeletonDestinationCard() {
    Column(
        modifier = Modifier
            .width(200.dp)
            .height(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Skeleton image
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        
        // Skeleton content
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Title
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
            )
            
            // Location
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
            )
        }
    }
}

@Composable
fun SkeletonCategoryItem() {
    SkeletonBox(
        modifier = Modifier
            .width(100.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}

@Composable
fun SkeletonManageDestinationItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Skeleton image
        SkeletonBox(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        // Skeleton content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(18.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .width(50.dp)
                    .height(14.dp)
            )
        }
        
        // Skeleton buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBox(
                modifier = Modifier.size(40.dp)
            )
            SkeletonBox(
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun SkeletonList(
    itemCount: Int = 5,
    item: @Composable () -> Unit
) {
    Column {
        repeat(itemCount) {
            item()
        }
    }
}

