package com.example.smarttravel.ui.screens.planregister

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTopBar
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
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    var startDate by remember { mutableStateOf<LocalDate?>(uiState.startDate) }
    var endDate by remember { mutableStateOf<LocalDate?>(uiState.endDate) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = currentMonth
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    var headerMonth by remember { mutableStateOf(currentMonth) }
    LaunchedEffect(calendarState.firstVisibleMonth) {
        headerMonth = calendarState.firstVisibleMonth.yearMonth
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER CỐ ĐỊNH (STICKY HEADER) - CĂN GIỮA TUYỆT ĐỐI THANH TIẾN ĐỘ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .padding(horizontal = 24.dp)
                    .padding(top = 60.dp, bottom = 16.dp)
            ) {
                // Nút Back (Căn trái)
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    AppTopBar(onBackClick = { navController.popBackStack() })
                }

                // Thanh tiến trình (Căn giữa tuyệt đối trong Box)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .align(Alignment.Center)
                ) {
                    LinearProgressIndicator(
                        progress = { 0.4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }
            // --- KẾT THÚC HEADER CỐ ĐỊNH ---

            // --- PHẦN NỘI DUNG (CUỘN ĐƯỢC) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Spacer để bù đắp khoảng trống dưới sticky header
                item { Spacer(modifier = Modifier.height(8.dp)) }

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
                        text = "Chọn ngày cho chuyến đi của bạn. Điều này giúp chúng tôi lập kế hoạch hành trình hoàn chỉnh.",
                        fontSize = 16.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceVariant)
                            .padding(vertical = 16.dp)
                    ) {
                        CalendarHeader(month = headerMonth)
                        Spacer(modifier = Modifier.height(16.dp))
                        DaysOfWeekHeader(firstDayOfWeek = firstDayOfWeek)
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalCalendar(
                            state = calendarState,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            dayContent = { day ->
                                DayCell(
                                    day = day,
                                    startDate = startDate,
                                    endDate = endDate,
                                    onClick = { clickedDate ->
                                        if (startDate == null) {
                                            startDate = clickedDate
                                        } else if (endDate == null) {
                                            if (clickedDate.isBefore(startDate)) {
                                                startDate = clickedDate
                                            } else if (clickedDate.isAfter(startDate)) {
                                                endDate = clickedDate
                                            }
                                        } else {
                                            startDate = clickedDate
                                            endDate = null
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // --- PHẦN NÚT BẤM (CỐ ĐỊNH Ở DƯỚI) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface) // Nền cho nút bấm cố định
                    .padding(16.dp)
            ) {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        viewModel.setDates(startDate!!, endDate!!)
                        navController.navigate(Screen.Economy.route)
                    },
                    enabled = startDate != null && endDate != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ... Giữ nguyên các hàm CalendarHeader, DaysOfWeekHeader, DayCell ...
@Composable
private fun CalendarHeader(month: YearMonth) {
    val colorScheme = MaterialTheme.colorScheme
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
    Text(
        text = month.format(formatter)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun DaysOfWeekHeader(firstDayOfWeek: DayOfWeek) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = daysOfWeek(firstDayOfWeek)
        daysOfWeek.forEachIndexed { index, dayOfWeek ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi")),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            // Thêm spacing giữa các items (trừ item cuối)
            if (index < daysOfWeek.size - 1) {
                Spacer(modifier = Modifier.width(0.dp))
            }
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
    val colorScheme = MaterialTheme.colorScheme
    val date = day.date
    val isSelectable = day.position == DayPosition.MonthDate && date >= LocalDate.now()
    val isStartDate = date == startDate
    val isEndDate = date == endDate
    val inRange = startDate != null && endDate != null && date > startDate && date < endDate
    val backgroundColor = when {
        isStartDate || isEndDate -> colorScheme.primary
        inRange -> colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    val textColor = when {
        isStartDate || isEndDate -> colorScheme.onPrimary
        !isSelectable -> colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                enabled = isSelectable,
                onClick = { onClick(date) }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Box nhỏ hơn cho background màu xanh
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor),
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
}