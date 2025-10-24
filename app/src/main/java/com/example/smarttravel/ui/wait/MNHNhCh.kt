package com.example.smarttravel.ui.wait

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttravel.R

@Preview(showBackground = true, showSystemUi = true )
@Composable
fun MNHNhCh() {
    // Màn hình chính (bo góc như Figma)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(30.dp)) // Bo góc như path trong vector
            .background(Color(0xFF037CAC)),  // Màu nền xanh (#037CAC)
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // --- Tiêu đề ---
            Text(
                text = "Smart Travel",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- Ảnh minh họa ---
            Image(
                painter = painterResource(id = R.drawable.travel_bus),
                contentDescription = "Logo Smart Travel",
                modifier = Modifier
                    .size(250.dp)
                    .padding(4.dp)
            )
        }
    }
}