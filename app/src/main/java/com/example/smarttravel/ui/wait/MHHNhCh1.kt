package com.example.smarttravel.ui.wait

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.PrimaryButton

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MHHNhCh1(
    onSkip: () -> Unit = {},
    onStartClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // toàn bộ nội dung thả vào Column (chừa chỗ cho nút phía dưới)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // chừa khoảng cho nút ở dưới
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phần hình ảnh trên cùng
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(10f)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboard_boat),
                    contentDescription = "Hình minh họa du lịch",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = "Skip",
                    color = Color(0xFFCAE9FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 24.dp)
                        .clickable { onSkip() }
                )
            }

            Spacer(modifier = Modifier.height(19.dp))

            // Phần text ở giữa
            Column(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1B1E28),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append("Cuộc đời ngắn ngủi, thế giới thì ") }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFFF7C1E),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append("bao la") }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(35.dp))

                Text(
                    text = "Du Lịch Thông Minh giúp bạn khám phá những hành trình độc đáo, đáng tin cậy đến mọi miền.",
                    color = Color(0xFF7C838D),
                    fontSize = 16.sp,
                    lineHeight = 27.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )

                Spacer(modifier = Modifier.height(35.dp))

                // Indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0D6EFD))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(13.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFCAE9FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFCAE9FF))
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        PrimaryButton(
            text = "Bắt đầu",
            onClick = onStartClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 40.dp)
        )
    }
}
