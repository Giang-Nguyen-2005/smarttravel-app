package com.example.smarttravel.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarttravel.R
import com.example.smarttravel.ui.theme.SmarttravelTheme

@Composable
fun SocialButton(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier
            .size(65.dp)
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Social Icon"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SocialButtonPreview() {
    SmarttravelTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Thay R.drawable.ic_launcher_foreground bằng icon thật của bạn
            SocialButton(iconRes = R.drawable.icon_google, onClick = {})
            Spacer(modifier = Modifier.width(16.dp))
            SocialButton(iconRes = R.drawable.icon_facebook, onClick = {})
            Spacer(modifier = Modifier.width(16.dp))
            SocialButton(iconRes = R.drawable.icon_instagram, onClick = {})
        }
    }
}