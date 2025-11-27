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
import androidx.compose.foundation.layout.*

@Composable
fun AppTopBar(
    onBackClick: () -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant, // nền theo theme
    iconTint: Color = MaterialTheme.colorScheme.onSurface // icon theo theme
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .background(color = containerColor, shape = CircleShape)
            .clickable { onBackClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.back_icon),
            contentDescription = "Back",
            tint = iconTint,
            modifier = Modifier.size(10.dp)
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
