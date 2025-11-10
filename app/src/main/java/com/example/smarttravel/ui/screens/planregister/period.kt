package com.example.smarttravel.ui.screens.planregister

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarttravel.ui.components.AppTopBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.viewmodel.PlanViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PeriodScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- State cho Lịch ---
    // 1. State để lưu lựa chọn của người dùng (local)
    var startDate by remember { mutableStateOf<LocalDate?>(uiState.startDate) }
    var endDate by remember { mutableStateOf<LocalDate?>(uiState.endDate) }

    // 2. State cho thư viện Kizitonwose
    val currentMonth = remember { YearMonth.now() }
    val startMonth = currentMonth
    val endMonth = remember { currentMonth.plusMonths(12) } // Cho phép chọn trong 1 năm
    val firstDayOfWeek = remember { DayOfWeek.MONDAY } // Bắt đầu bằng Thứ Hai

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    // State để cập nhật tiêu đề "Tháng X, Năm YYYY"
    var headerMonth by remember { mutableStateOf(currentMonth) }

    LaunchedEffect(calendarState.firstVisibleMonth) {
        headerMonth = calendarState.firstVisibleMonth.yearMonth
    }
    // --- Kết thúc State cho Lịch ---

    Scaffold(
        // Bạn có thể ẩn BottomBar trong luồng này nếu muốn
        // bottomBar = {
        //    AppBottomBar(navController = navController, currentRoute = Screen.Calendar.route)
        // }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
                // Chỉ lấy padding dưới cùng từ Scaffold
                .padding(bottom = paddingValues.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Thanh Nút quay lại + tiến trình
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                ) {
                    AppTopBar(onBackClick = { navController.popBackStack() })
                    Spacer(modifier = Modifier.width(12.dp))
                    LinearProgressIndicator(
                        progress = { 0.4f }, // Cập nhật tiến trình
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFE0E0E0),
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }

            item {
                Text(
                    text = "Khi nào chuyến đi của bạn sẽ bắt đầu và kết thúc? 📅",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Text(
                    text = "Chọn ngày cho chuyến đi của bạn. Điều này giúp chúng tôi lập kế hoạch hành trình hoàn chỉnh cho khoảng thời gian du lịch của bạn.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // --- LỊCH THẬT ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 16.dp)
                ) {
                    // 1. Tiêu đề (Tháng 11, 2025)
                    CalendarHeader(month = headerMonth)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Tên các ngày (T2, T3, T4...)
                    DaysOfWeekHeader(firstDayOfWeek = firstDayOfWeek)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Lịch
                    HorizontalCalendar(
                        state = calendarState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        dayContent = { day ->
                            // --- Logic hiển thị ngày ---
                            DayCell(
                                day = day,
                                startDate = startDate,
                                endDate = endDate,
                                onClick = { clickedDate ->
                                    // --- Logic chọn khoảng ngày ---
                                    if (startDate == null) {
                                        startDate = clickedDate
                                    } else if (endDate == null) {
                                        if (clickedDate.isBefore(startDate)) {
                                            // Nếu chọn ngày trước start -> reset
                                            startDate = clickedDate
                                        } else if (clickedDate.isAfter(startDate)) {
                                            // Nếu chọn ngày sau start -> hoàn tất
                                            endDate = clickedDate
                                        }
                                    } else {
                                        // Nếu đã chọn cả 2 -> reset
                                        startDate = clickedDate
                                        endDate = null
                                    }
                                }
                            )
                        }
                    )
                }
            }

            // Nút Tiếp tục
            item {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        // Lưu ngày đã chọn vào ViewModel
                        viewModel.setDates(startDate!!, endDate!!)
                        navController.navigate(Screen.Economy.route)
                    },
                    enabled = startDate != null && endDate != null, // Chỉ bật khi đã chọn đủ
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// --- CÁC COMPONENT PHỤ CHO LỊCH ---

@Composable
private fun CalendarHeader(month: YearMonth) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
    Text(
        text = month.format(formatter)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun DaysOfWeekHeader(firstDayOfWeek: DayOfWeek) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = daysOfWeek(firstDayOfWeek)
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi")),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp) // Đảm bảo các cột thẳng hàng
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onClick: (LocalDate) -> Unit
) {
    val date = day.date
    val isSelectable = day.position == DayPosition.MonthDate && date >= LocalDate.now()

    val isStartDate = date == startDate
    val isEndDate = date == endDate
    val inRange = startDate != null && endDate != null && date > startDate && date < endDate

    val backgroundColor = when {
        isStartDate || isEndDate -> MaterialTheme.colorScheme.primary // Màu xanh đậm
        inRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // Màu xanh nhạt
        else -> Color.Transparent
    }

    val textColor = when {
        isStartDate || isEndDate -> Color.White // Chữ trắng
        !isSelectable -> Color.Gray.copy(alpha = 0.5f) // Chữ mờ
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .width(40.dp) // Đảm bảo vừa vặn
            .aspectRatio(1f) // Làm cho ô vuông
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                enabled = isSelectable,
                onClick = { onClick(date) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}