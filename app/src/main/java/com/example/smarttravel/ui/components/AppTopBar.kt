package com.example.smarttravel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarttravel.R

@Composable
fun AppTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFE0E0E0), // nền xám nhạt
    iconTint: Color = Color(0xFF1A1A1A) // icon màu đen đậm
) {
    Box(
        modifier = modifier
            .size(56.dp) // kích thước đúng với UI mẫu
            .background(color = containerColor, shape = CircleShape)
            .clickable { onBackClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.back_icon),
            contentDescription = "Back",
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}
@Preview(showBackground = true)
@Composable
fun AppTopBarPreview() {
    AppTopBar(
        onBackClick = {},
        modifier = Modifier
    )
}
