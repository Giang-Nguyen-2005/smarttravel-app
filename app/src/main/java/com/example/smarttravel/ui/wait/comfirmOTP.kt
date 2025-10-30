package com.example.smarttravel.ui.wait

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.OtpTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmOTPScreen(
    onBackClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {}
) {
    var otpValue by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf(86) } // 1 phút 26 giây = 86s

    // ⏳ Giả lập đếm ngược thời gian
    LaunchedEffect(Unit) {
        while (timer > 0) {
            kotlinx.coroutines.delay(1000)
            timer--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            // 🔙 Nút Back đặt ở góc trên trái
            AppTopBar(onBackClick = onBackClick)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
        ) {
            // 🧩 Tiêu đề
            Text(
                text = "Xác Minh OTP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 📨 Mô tả
            Text(
                text = "Vui lòng kiểm tra email của bạn để lấy mã xác minh",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 🔢 Mã OTP
            Text(
                text = "Mã OTP",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OtpTextField(
                otpText = otpValue,
                onOtpTextChange = { otpValue = it },
                otpCount = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 🔘 Nút xác minh
            PrimaryButton(
                text = "Xác minh",
                onClick = { onVerifyClick(otpValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔁 Gửi lại mã + đồng hồ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gửi Lại Mã",
                    color = Color(0xFF007BFF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = String.format("0:%02d", timer),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewConfirmOTPScreen() {
    ConfirmOTPScreen()
}
