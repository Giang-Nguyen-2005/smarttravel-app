package com.example.smarttravel.ui.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import com.example.smarttravel.ui.theme.SmarttravelTheme
@Composable
fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Nhập số điện thoại",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
    }
}
@Composable
@Preview(showBackground = true)
fun PhoneNumberFieldPreview() {
    var phone by remember { mutableStateOf("+84 01758-000666") }

    SmarttravelTheme {
        PhoneNumberField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

