package com.example.smarttravel.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.DestinationCard
import com.example.smarttravel.ui.theme.SmarttravelTheme

// --- Dữ liệu giả - Cập nhật để có Rating ---
data class SearchItem(
    val id: Int,
    val name: String,
    val location: String,
    val rating: Double, // <-- Thêm Rating
    val imageUrl: String
)

val dummySearchItems = listOf(
    SearchItem(1, "Vịnh Hạ Long", "Quảng ninh", 4.2, "ha_long"),
    SearchItem(2, "Hạ Long", "Việt Nam", 4.8, "avatar"),
    SearchItem(3, "Địa điểm 3", "3", 3.9, "ha_long"),
    SearchItem(4, "Địa điểm 4", "4", 4.5, "avatar"),
    SearchItem(5, "Địa điểm 5", "4", 4.5, "travel_bus"),
)
// --- Kết thúc dữ liệu giả ---

@Composable
fun SearchScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // 1. Top Bar
        SearchTopBar(
            onBackClick = { navController.popBackStack() },
            onCancelClick = { /* ... */ }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Thanh tìm kiếm
        SearchBar(
            searchText = searchText,
            onSearchChanged = { searchText = it }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 3. Tiêu đề
        Text(
            text = "Địa Điểm",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 4. Lưới kết quả (Dùng LazyVerticalGrid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 cột
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(dummySearchItems) { item ->
                // --- TÁI SỬ DỤNG Ở ĐÂY ---
                DestinationCard(
                    // Không truyền modifier width để Grid tự xử lý
                    imageUrl = item.imageUrl,
                    title = item.name,
                    location = item.location,
                    rating = item.rating // Truyền rating
                )
                // --- KẾT THÚC TÁI SỬ DỤNG ---
            }
        }
    }
}

// --- CÁC COMPONENT CON (TopBar, SearchBar - Giữ nguyên) ---
@Composable
private fun SearchTopBar(
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTopBar(onBackClick = onBackClick)
        Text(
            text = "Tìm kiếm",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onSearchChanged: (String) -> Unit
) {
    TextField(
        value = searchText,
        onValueChange = onSearchChanged,
        placeholder = { Text("Tìm kiếm địa điểm", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        SearchScreen(navController = navController)
    }
}