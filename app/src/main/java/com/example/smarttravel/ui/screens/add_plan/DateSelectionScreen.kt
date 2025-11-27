package com.example.smarttravel.ui.screens.add_plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.navigation.NavController
import com.example.smarttravel.navigation.Screen
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
fun DateSelectionScreen(
    navController: NavController
) {
    val colorScheme = MaterialTheme.colorScheme
    val today = LocalDate.now()
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    
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
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("vi"))
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Chọn ngày",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            // Hướng dẫn
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Chọn ngày cho kế hoạch của bạn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bạn có thể chọn một ngày hoặc nhiều ngày liên tiếp. Chỉ có thể chọn ngày tương lai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Lịch
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
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
                                    startDate = selectedStartDate,
                                    endDate = selectedEndDate,
                                    onClick = { clickedDate ->
                                        if (selectedStartDate == null) {
                                            selectedStartDate = clickedDate
                                            selectedEndDate = clickedDate
                                        } else if (selectedEndDate == null || selectedEndDate == selectedStartDate) {
                                            if (clickedDate.isBefore(selectedStartDate!!)) {
                                                selectedStartDate = clickedDate
                                            } else if (clickedDate.isAfter(selectedStartDate!!)) {
                                                selectedEndDate = clickedDate
                                            } else {
                                                // Click lại ngày bắt đầu -> bỏ chọn
                                                selectedStartDate = null
                                                selectedEndDate = null
                                            }
                                        } else {
                                            if (clickedDate == selectedStartDate) {
                                                selectedStartDate = null
                                                selectedEndDate = null
                                            } else if (clickedDate == selectedEndDate) {
                                                selectedEndDate = null
                                            } else {
                                                selectedStartDate = clickedDate
                                                selectedEndDate = clickedDate
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
            
            // Nút tiếp tục
            if (selectedStartDate != null) {
                Button(
                    onClick = {
                        val startDateStr = selectedStartDate!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val endDateStr = (selectedEndDate ?: selectedStartDate)!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        navController.navigate("add_plan_flow/$startDateStr/$endDateStr")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Tiếp tục",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

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
    val today = LocalDate.now()
    // Chỉ cho phép chọn ngày tương lai (từ hôm nay trở đi)
    val isSelectable = day.position == DayPosition.MonthDate && !date.isBefore(today)
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
