package com.example.smarttravel.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiServiceImpl @Inject constructor() : AiService {
    
    // TODO: Thay thế bằng API key thật từ local.properties hoặc Firebase Remote Config
    private val apiKey = "AIzaSyBQ1nLVyIcY7P4vg_Abila76m8rbgkSs9Y"
    
    override suspend fun generateTravelPlan(
        destination: String,
        locationName: String,
        companion: String,
        startDate: String,
        endDate: String,
        budget: String,
        purposes: List<String>
    ): Result<String> {
        return try {
            if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                // Nếu chưa có API key, trả về dữ liệu mẫu
                return Result.success(getSamplePlanDetail())
            }
            
            val prompt = buildPrompt(
                destination = destination,
                locationName = locationName,
                companion = companion,
                startDate = startDate,
                endDate = endDate,
                budget = budget,
                purposes = purposes
            )
            
            android.util.Log.d("AiServiceImpl", "Calling Gemini API directly via HTTP")
            android.util.Log.d("AiServiceImpl", "Prompt length: ${prompt.length}")
            android.util.Log.d("AiServiceImpl", "API Key (first 10 chars): ${apiKey.take(10)}...")
            
            // Gọi API trực tiếp qua HTTP
            val response = withContext(Dispatchers.IO) {
                callGeminiApiDirectly(prompt)
            }
            
            android.util.Log.d("AiServiceImpl", "AI response received, length: ${response.length}")
            android.util.Log.d("AiServiceImpl", "AI response preview: ${response.take(500)}")
            
            Result.success(response)
            
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error calling AI: ${e.message}", e)
            android.util.Log.e("AiServiceImpl", "Stack trace: ${e.stackTraceToString()}")
            
            // Nếu tất cả models đều fail, có thể là API key không hợp lệ
            // Trong trường hợp này, trả về sample data để app vẫn hoạt động
            android.util.Log.w("AiServiceImpl", "All API calls failed, returning sample data")
            Result.success(getSamplePlanDetail())
        }
    }
    
    override suspend fun generateAlternativeSuggestion(
        destination: String,
        locationName: String,
        itemType: String,
        currentItem: Map<String, Any>,
        budget: String,
        dayNumber: Int,
        date: String
    ): Result<Map<String, Any>> {
        return try {
            if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                // Trả về sample data
                return Result.success(getSampleAlternative(itemType))
            }
            
            val prompt = buildAlternativePrompt(
                destination = destination,
                locationName = locationName,
                itemType = itemType,
                currentItem = currentItem,
                budget = budget,
                dayNumber = dayNumber,
                date = date
            )
            
            android.util.Log.d("AiServiceImpl", "Calling AI for alternative suggestion: type=$itemType")
            
            val response = withContext(Dispatchers.IO) {
                callGeminiApiDirectly(prompt)
            }
            
            android.util.Log.d("AiServiceImpl", "Alternative suggestion received, length: ${response.length}")
            
            // Parse response thành Map
            val alternativeItem = parseAlternativeResponse(response, itemType)
            Result.success(alternativeItem)
            
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error generating alternative suggestion: ${e.message}", e)
            // Trả về sample data nếu có lỗi
            Result.success(getSampleAlternative(itemType))
        }
    }
    
    private fun buildAlternativePrompt(
        destination: String,
        locationName: String,
        itemType: String,
        currentItem: Map<String, Any>,
        budget: String,
        dayNumber: Int,
        date: String
    ): String {
        val currentItemJson = org.json.JSONObject(currentItem).toString(2)
        
        return when (itemType) {
            "hotel" -> {
                """
Bạn là chuyên gia du lịch. Hãy đề xuất một khách sạn/nơi nghỉ KHÁC (không trùng với khách sạn hiện tại) cho chuyến đi với thông tin sau:

- Địa điểm: $destination
- Khu vực: $locationName
- Ngân sách: $budget
- Ngày: Ngày $dayNumber ($date)

Khách sạn hiện tại:
$currentItemJson

Yêu cầu:
1. Đề xuất một khách sạn KHÁC HOÀN TOÀN (không trùng tên, địa chỉ)
2. Phù hợp với ngân sách $budget
3. Cùng khu vực hoặc gần khu vực $locationName
4. Có thể là loại khác (ví dụ: resort thay vì hostel, hoặc ngược lại)

Trả về DƯỚI DẠNG JSON với cấu trúc chính xác như sau (chỉ trả về JSON, không có text thêm):

{
  "name": "[Tên khách sạn mới]",
  "location": "[Địa chỉ khách sạn mới]",
  "price": "[Giá phòng/đêm, phù hợp với ngân sách $budget]",
  "rating": "[Xếp hạng sao, ví dụ: 3 sao, 4 sao]",
  "description": "[Mô tả ngắn về khách sạn mới, tại sao phù hợp với chuyến đi]"
}
"""
            }
            "activity" -> {
                val time = currentItem["time"] as? String ?: ""
                val type = currentItem["type"] as? String ?: ""
                """
Bạn là chuyên gia du lịch. Hãy đề xuất một hoạt động/địa điểm KHÁC (không trùng với hoạt động hiện tại) cho chuyến đi với thông tin sau:

- Địa điểm: $destination
- Khu vực: $locationName
- Ngân sách: $budget
- Ngày: Ngày $dayNumber ($date)
- Thời gian: $time
- Loại hoạt động: $type

Hoạt động hiện tại:
$currentItemJson

Yêu cầu:
1. Đề xuất một hoạt động/địa điểm KHÁC HOÀN TOÀN (không trùng tên, địa chỉ)
2. Cùng loại ($type) hoặc tương tự
3. Phù hợp với ngân sách $budget
4. Cùng khu vực hoặc gần khu vực $locationName
5. Có thể tham quan vào thời gian $time

Trả về DƯỚI DẠNG JSON với cấu trúc chính xác như sau (chỉ trả về JSON, không có text thêm):

{
  "time": "$time",
  "type": "$type",
  "name": "[Tên hoạt động/địa điểm mới]",
  "location": "[Địa chỉ mới]",
  "description": "[Mô tả chi tiết về hoạt động/địa điểm mới]",
  "price": "[Giá ước tính, phù hợp với ngân sách $budget]",
  ${if (type in listOf("breakfast", "lunch", "dinner")) """
  "recommendedDishes": ["[Món đặc sản 1]", "[Món đặc sản 2]"]
  """ else if (type == "attraction") """
  "tips": "[Mẹo khi tham quan địa điểm mới này]"
  """ else ""}
}
"""
            }
            else -> throw IllegalArgumentException("Unknown item type: $itemType")
        }
    }
    
    private fun parseAlternativeResponse(response: String, itemType: String): Map<String, Any> {
        return try {
            var jsonString = response.trim()
            // Loại bỏ markdown code blocks nếu có
            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.removePrefix("```json").trim()
            }
            if (jsonString.startsWith("```")) {
                jsonString = jsonString.removePrefix("```").trim()
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.removeSuffix("```").trim()
            }
            
            val jsonObject = org.json.JSONObject(jsonString)
            val result = mutableMapOf<String, Any>()
            
            // Parse các field chung
            jsonObject.keys().forEach { key ->
                when (val value = jsonObject.get(key)) {
                    is org.json.JSONArray -> {
                        val list = mutableListOf<String>()
                        for (i in 0 until value.length()) {
                            list.add(value.getString(i))
                        }
                        result[key] = list
                    }
                    else -> result[key] = value.toString()
                }
            }
            
            result
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error parsing alternative response: ${e.message}", e)
            getSampleAlternative(itemType)
        }
    }
    
    private fun getSampleAlternative(itemType: String): Map<String, Any> {
        return when (itemType) {
            "hotel" -> mapOf(
                "name" to "Khách sạn mẫu",
                "location" to "Địa chỉ mẫu",
                "price" to "200.000 - 500.000 VNĐ/đêm",
                "rating" to "3 sao",
                "description" to "Khách sạn mẫu phù hợp với ngân sách"
            )
            "activity" -> mapOf(
                "time" to "09:00",
                "type" to "attraction",
                "name" to "Địa điểm mẫu",
                "location" to "Địa chỉ mẫu",
                "description" to "Mô tả địa điểm mẫu",
                "price" to "100.000 - 200.000 VNĐ/người"
            )
            else -> emptyMap()
        }
    }
    
    private fun callGeminiApiDirectly(prompt: String): String {
        // Thử gọi ListModels trước để xem models nào available
        val availableModels = try {
            listAvailableModels()
        } catch (e: Exception) {
            android.util.Log.w("AiServiceImpl", "Failed to list models: ${e.message}")
            emptyList()
        }
        
        // Nếu có models từ ListModels, dùng chúng, nếu không thì dùng danh sách mặc định
        val modelNames = if (availableModels.isNotEmpty()) {
            android.util.Log.d("AiServiceImpl", "Available models: $availableModels")
            availableModels
        } else {
            // Thử các model names khác nhau theo thứ tự ưu tiên
            listOf(
                "gemini-1.5-pro-002",
                "gemini-1.5-pro-001",
                "gemini-1.5-flash-002",
                "gemini-1.5-flash-001",
                "gemini-1.5-pro-latest",
                "gemini-1.5-flash-latest",
                "gemini-1.5-pro",
                "gemini-1.5-flash"
                //"gemini-pro"
            )
        }
        
        val apiVersions = listOf("v1beta", "v1")
        
        var lastException: Exception? = null
        
        for (apiVersion in apiVersions) {
            for (modelName in modelNames) {
                try {
                    val url = URL("https://generativelanguage.googleapis.com/$apiVersion/models/$modelName:generateContent?key=$apiKey")
                    android.util.Log.d("AiServiceImpl", "Trying: $apiVersion/models/$modelName")
                    val connection = url.openConnection() as HttpURLConnection
                    
                    try {
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.doOutput = true
                        
                        // Tạo request body
                        val requestBody = JSONObject().apply {
                            put("contents", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("parts", org.json.JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("text", prompt)
                                        })
                                    })
                                })
                            })
                        }
                        
                        // Gửi request
                        connection.outputStream.use { output ->
                            output.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                        }
                        
                        // Đọc response
                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                            val jsonResponse = JSONObject(response)
                            
                            // Parse response
                            val candidates = jsonResponse.getJSONArray("candidates")
                            if (candidates.length() > 0) {
                                val candidate = candidates.getJSONObject(0)
                                val content = candidate.getJSONObject("content")
                                val parts = content.getJSONArray("parts")
                                if (parts.length() > 0) {
                                    val text = parts.getJSONObject(0).getString("text")
                                    android.util.Log.d("AiServiceImpl", "Success with $apiVersion/models/$modelName")
                                    return text
                                }
                            }
                            throw Exception("Không tìm thấy text trong response")
                        } else {
                            val errorResponse = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "Unknown error"
                            android.util.Log.w("AiServiceImpl", "HTTP Error $responseCode for $apiVersion/models/$modelName: $errorResponse")
                            lastException = Exception("HTTP Error $responseCode: $errorResponse")
                            continue
                        }
                    } finally {
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AiServiceImpl", "Failed $apiVersion/models/$modelName: ${e.message}")
                    lastException = e
                    continue
                }
            }
        }
        
        // Nếu tất cả đều fail
        throw lastException ?: Exception("Tất cả models và API versions đều thất bại")
    }
    
    private fun listAvailableModels(): List<String> {
        return try {
            // Thử cả v1beta và v1
            val apiVersions = listOf("v1beta", "v1")
            for (apiVersion in apiVersions) {
                try {
                    val url = URL("https://generativelanguage.googleapis.com/$apiVersion/models?key=$apiKey")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        val jsonResponse = JSONObject(response)
                        val models = jsonResponse.getJSONArray("models")
                        val modelNames = mutableListOf<String>()
                        
                        android.util.Log.d("AiServiceImpl", "Found ${models.length()} models from $apiVersion")
                        
                        for (i in 0 until models.length()) {
                            val model = models.getJSONObject(i)
                            val name = model.getString("name")
                            // Extract model name from "models/gemini-xxx" format
                            val modelName = name.substringAfter("models/")
                            if (modelName.isNotEmpty()) {
                                modelNames.add(modelName)
                                android.util.Log.d("AiServiceImpl", "Found model: $modelName")
                            }
                        }
                        connection.disconnect()
                        if (modelNames.isNotEmpty()) {
                            return modelNames
                        }
                    } else {
                        val errorResponse = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "Unknown"
                        android.util.Log.w("AiServiceImpl", "ListModels failed for $apiVersion: $responseCode - $errorResponse")
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AiServiceImpl", "Error listing models from $apiVersion: ${e.message}")
                }
            }
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error listing models: ${e.message}")
            emptyList()
        }
    }
    
    private fun buildPrompt(
        destination: String,
        locationName: String,
        companion: String,
        startDate: String,
        endDate: String,
        budget: String,
        purposes: List<String>
    ): String {
        val purposesText = purposes.joinToString(", ")
        
        // Tính số ngày
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = try {
            LocalDate.parse(startDate, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val end = try {
            LocalDate.parse(endDate, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now().plusDays(1)
        }
        val numberOfDays = ChronoUnit.DAYS.between(start, end).toInt() + 1
        
        // Tính ngân sách theo ngày dựa trên budget
        val budgetPerDay = when (budget) {
            "Tiết kiệm" -> "500.000 - 1.500.000 VNĐ/ngày"
            "Cân bằng" -> "1.500.000 - 3.000.000 VNĐ/ngày"
            "Cao cấp" -> "3.000.000 - 5.000.000 VNĐ/ngày"
            "Linh hoạt" -> "5.000.000+ VNĐ/ngày"
            else -> "1.500.000 - 3.000.000 VNĐ/ngày"
        }
        
        // Phân bổ ngân sách theo loại dịch vụ
        val budgetBreakdown = when (budget) {
            "Tiết kiệm" -> """
- Khách sạn: 200.000 - 500.000 VNĐ/đêm/phòng
- Ăn sáng: 30.000 - 80.000 VNĐ/người
- Ăn trưa: 50.000 - 150.000 VNĐ/người
- Ăn tối: 80.000 - 200.000 VNĐ/người
- Tham quan: 0 - 100.000 VNĐ/người (ưu tiên địa điểm miễn phí)
- Hoạt động: 50.000 - 200.000 VNĐ/người
- Tổng 1 ngày: 500.000 - 1.500.000 VNĐ/người"""
            "Cân bằng" -> """
- Khách sạn: 500.000 - 1.200.000 VNĐ/đêm/phòng
- Ăn sáng: 50.000 - 150.000 VNĐ/người
- Ăn trưa: 100.000 - 300.000 VNĐ/người
- Ăn tối: 150.000 - 400.000 VNĐ/người
- Tham quan: 50.000 - 300.000 VNĐ/người
- Hoạt động: 100.000 - 500.000 VNĐ/người
- Tổng 1 ngày: 1.500.000 - 3.000.000 VNĐ/người"""
            "Cao cấp" -> """
- Khách sạn: 1.200.000 - 2.500.000 VNĐ/đêm/phòng
- Ăn sáng: 100.000 - 300.000 VNĐ/người
- Ăn trưa: 200.000 - 600.000 VNĐ/người
- Ăn tối: 300.000 - 1.000.000 VNĐ/người
- Tham quan: 100.000 - 500.000 VNĐ/người
- Hoạt động: 200.000 - 1.000.000 VNĐ/người
- Tổng 1 ngày: 3.000.000 - 5.000.000 VNĐ/người"""
            "Linh hoạt" -> """
- Khách sạn: 2.500.000+ VNĐ/đêm/phòng
- Ăn sáng: 200.000+ VNĐ/người
- Ăn trưa: 400.000+ VNĐ/người
- Ăn tối: 500.000+ VNĐ/người
- Tham quan: 200.000+ VNĐ/người
- Hoạt động: 500.000+ VNĐ/người
- Tổng 1 ngày: 5.000.000+ VNĐ/người"""
            else -> """
- Khách sạn: 500.000 - 1.200.000 VNĐ/đêm/phòng
- Ăn sáng: 50.000 - 150.000 VNĐ/người
- Ăn trưa: 100.000 - 300.000 VNĐ/người
- Ăn tối: 150.000 - 400.000 VNĐ/người
- Tham quan: 50.000 - 300.000 VNĐ/người
- Hoạt động: 100.000 - 500.000 VNĐ/người
- Tổng 1 ngày: 1.500.000 - 3.000.000 VNĐ/người"""
        }
        
        // Tạo danh sách các ngày
        val dateList = mutableListOf<String>()
        var currentDate = start
        for (i in 1..numberOfDays) {
            dateList.add(currentDate.format(dateFormatter))
            currentDate = currentDate.plusDays(1)
        }
        
        // Tạo ví dụ JSON cho tất cả các ngày (tối đa 5 ngày để không quá dài)
        val exampleDays = minOf(numberOfDays, 5)
        val jsonExample = buildString {
            append("[\n")
            for (day in 1..exampleDays) {
                val dayDate = dateList[day - 1]
                append("""  {
    "day": $day,
    "date": "$dayDate",
    "title": "Ngày $day: [Tiêu đề ngày]",
    "hotel": {
      "name": "[Tên khách sạn]",
      "location": "[Địa chỉ khách sạn]",
      "price": "[Giá phòng/đêm]",
      "rating": "[Xếp hạng sao]",
      "description": "[Mô tả ngắn về khách sạn]"
    },
    "activities": [
      {
        "time": "08:00",
        "type": "breakfast",
        "name": "[Tên nhà hàng/quán ăn]",
        "location": "[Địa chỉ]",
        "description": "[Mô tả món ăn đặc sản]",
        "price": "[Giá ước tính, ví dụ: 50.000 - 150.000 VNĐ/người]",
        "recommendedDishes": ["[Món 1]", "[Món 2]"]
      },
      {
        "time": "09:00",
        "type": "attraction",
        "name": "[Tên địa điểm tham quan]",
        "location": "[Địa chỉ]",
        "description": "[Mô tả chi tiết về địa điểm]",
        "price": "[Giá vé tham quan, ví dụ: 100.000 - 200.000 VNĐ/người]",
        "tips": "[Mẹo khi tham quan]"
      },
      {
        "time": "12:00",
        "type": "lunch",
        "name": "[Tên nhà hàng]",
        "location": "[Địa chỉ]",
        "description": "[Mô tả món ăn]",
        "price": "[Giá ước tính, ví dụ: 100.000 - 300.000 VNĐ/người]",
        "recommendedDishes": ["[Món đặc sản 1]", "[Món đặc sản 2]"]
      },
      {
        "time": "14:00",
        "type": "activity",
        "name": "[Hoạt động/Khu vui chơi]",
        "location": "[Địa chỉ, gần khách sạn nếu có]",
        "description": "[Mô tả hoạt động]",
        "price": "[Giá ước tính, ví dụ: 200.000 - 500.000 VNĐ/người]"
      },
      {
        "time": "18:00",
        "type": "dinner",
        "name": "[Tên nhà hàng]",
        "location": "[Địa chỉ]",
        "description": "[Mô tả món ăn]",
        "price": "[Giá ước tính, ví dụ: 150.000 - 400.000 VNĐ/người]",
        "recommendedDishes": ["[Món đặc sản]"]
      }
    ]
  }""")
                if (day < exampleDays) {
                    append(",\n")
                } else if (numberOfDays > exampleDays) {
                    append(",\n")
                    append("  // ... TIẾP TỤC TẠO ĐỦ $numberOfDays NGÀY (ngày ${exampleDays + 1} đến ngày $numberOfDays) ...\n")
                }
            }
            append("\n]")
        }
        
        return """
🚨 QUAN TRỌNG: BẠN PHẢI TẠO ĐỦ $numberOfDays NGÀY (TỪ NGÀY $startDate ĐẾN NGÀY $endDate) 🚨

Bạn là một chuyên gia du lịch chuyên nghiệp với kiến thức sâu về giá cả thực tế tại Việt Nam. Hãy tạo một lịch trình chi tiết và đầy đủ cho chuyến đi với thông tin sau:

- Địa điểm: $destination
- Khu vực: $locationName
- Người đồng hành: $companion
- Thời gian: $startDate đến $endDate (TỔNG CỘNG $numberOfDays NGÀY)
- Ngân sách: $budget ($budgetPerDay)
- Sở thích: $purposesText

💰 HƯỚNG DẪN VỀ GIÁ CẢ - PHẢI TUÂN THỦ:
💰 Bạn PHẢI sử dụng giá thực tế tại Việt Nam, không được bịa đặt giá
💰 Phân bổ ngân sách theo mức "$budget":
$budgetBreakdown

💰 QUY TẮC TÍNH GIÁ:
- Giá phải THỰC TẾ với thị trường Việt Nam tại khu vực $locationName
- Không được đưa ra giá quá cao hoặc quá thấp so với thực tế
- Nếu không biết giá chính xác, hãy tra cứu hoặc ước tính dựa trên mức giá trung bình tại Việt Nam
- Giá khách sạn: tính theo phòng/đêm, không phải theo người
- Giá ăn uống: tính theo người
- Giá tham quan: tính theo người (nếu có vé)
- Địa điểm miễn phí: ghi "Miễn phí" thay vì "0 VNĐ"
- Giá phải nằm trong khoảng ngân sách đã phân bổ ở trên


🚨 YÊU CẦU BẮT BUỘC - ĐỌC KỸ:
🚨 BẠN PHẢI TẠO ĐỦ $numberOfDays NGÀY (KHÔNG ĐƯỢC THIẾU NGÀY NÀO, KHÔNG ĐƯỢC CHỈ TẠO 1 NGÀY)
🚨 Danh sách ngày: ${dateList.joinToString(", ")}
🚨 Mỗi ngày PHẢI có một object riêng trong mảng JSON với:
   - Ngày 1: day = 1, date = "$startDate"
   - Ngày 2: day = 2, date = "${dateList.getOrNull(1) ?: ""}"
${if (numberOfDays > 2) "   - Ngày 3: day = 3, date = \"${dateList[2]}\"\n" else ""}${if (numberOfDays > 3) "   - ... và cứ thế cho đến ngày $numberOfDays: day = $numberOfDays, date = \"$endDate\"" else ""}

Hãy tạo lịch trình theo từng ngày với các hoạt động chi tiết, BAO GỒM:
1. Địa điểm du lịch nổi tiếng
2. Các món ăn đặc sản địa phương (gợi ý nhà hàng/quán ăn cụ thể)
3. Khách sạn/nơi nghỉ (gợi ý khách sạn phù hợp với ngân sách)
4. Khu vui chơi giải trí gần khách sạn
5. Các hoạt động thú vị khác

Trả về DƯỚI DẠNG JSON với cấu trúc chính xác như sau (chỉ trả về JSON, không có text thêm):

$jsonExample

🚨 Lưu ý QUAN TRỌNG - ĐỌC KỸ:
🚨 BẠN PHẢI TẠO ĐỦ $numberOfDays NGÀY (KHÔNG ĐƯỢC THIẾU NGÀY NÀO, KHÔNG ĐƯỢC CHỈ TẠO 1 NGÀY)
🚨 Mỗi ngày PHẢI có một object riêng trong mảng JSON với day = 1, 2, 3, ... đến $numberOfDays
💰 Mỗi ngày PHẢI có field "hotel" với thông tin khách sạn và giá PHẢI nằm trong khoảng ngân sách đã phân bổ
💰 Mỗi activity PHẢI có field "price" với giá THỰC TẾ, không được bịa đặt
💰 Tổng chi phí 1 ngày (khách sạn + ăn uống + tham quan + hoạt động) PHẢI nằm trong khoảng $budgetPerDay
💰 Giá phải phù hợp với mức "$budget" và thực tế tại $locationName
- Mỗi ngày có ít nhất 6-8 hoạt động (ăn sáng, tham quan, ăn trưa, hoạt động, ăn tối, nghỉ ngơi)
- Type có thể là: "breakfast", "lunch", "dinner", "attraction", "activity", "entertainment", "rest", "hotel"
- Gợi ý các món ăn đặc sản địa phương trong "recommendedDishes"
- Gợi ý khu vui chơi giải trí gần khách sạn
- Phù hợp với ngân sách $budget và sở thích $purposesText
- Tất cả nội dung bằng tiếng Việt
""".trimIndent()
    }
    
    override suspend fun sendChatMessage(
        message: String,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> {
        return try {
            if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                // Nếu chưa có API key, trả về response mẫu
                return Result.success("Xin chào! Tôi là chatbot hỗ trợ du lịch. Hiện tại API key chưa được cấu hình.")
            }
            
            android.util.Log.d("AiServiceImpl", "Sending chat message: ${message.take(100)}...")
            
            val response = withContext(Dispatchers.IO) {
                callGeminiApiForChat(message, conversationHistory)
            }
            
            android.util.Log.d("AiServiceImpl", "Chat response received, length: ${response.length}")
            
            Result.success(response)
            
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error in chat: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun callGeminiApiForChat(
        message: String,
        conversationHistory: List<Pair<String, String>>
    ): String {
        // Thử lấy danh sách models có sẵn trước
        val availableModels = try {
            listAvailableModels()
        } catch (e: Exception) {
            android.util.Log.w("AiServiceImpl", "Failed to list models for chat: ${e.message}")
            emptyList()
        }
        
        // Nếu có models từ ListModels, dùng chúng, nếu không thì dùng danh sách mặc định
        val modelNames = if (availableModels.isNotEmpty()) {
            android.util.Log.d("AiServiceImpl", "Available models for chat: $availableModels")
            // Lọc bỏ gemini-pro và các models cũ không còn được hỗ trợ
            availableModels.filter { 
                !it.contains("gemini-pro", ignoreCase = true) && 
                !it.contains("gemini-1.0", ignoreCase = true)
            }
        } else {
            // Danh sách fallback (không có gemini-pro vì không còn được hỗ trợ)
            android.util.Log.d("AiServiceImpl", "Using fallback model list")
            listOf(
                "gemini-1.5-flash-002",
                "gemini-1.5-flash-001",
                "gemini-1.5-flash-latest",
                "gemini-1.5-flash",
                "gemini-1.5-pro-002",
                "gemini-1.5-pro-001",
                "gemini-1.5-pro-latest",
                "gemini-1.5-pro"
            )
        }
        
        // Ưu tiên v1beta trước vì v1 có thể không hỗ trợ một số models
        val apiVersions = listOf("v1beta", "v1")
        
        var lastException: Exception? = null
        
        for (apiVersion in apiVersions) {
            for (modelName in modelNames) {
                try {
                    val url = URL("https://generativelanguage.googleapis.com/$apiVersion/models/$modelName:generateContent?key=$apiKey")
                    android.util.Log.d("AiServiceImpl", "Trying chat: $apiVersion/models/$modelName")
                    val connection = url.openConnection() as HttpURLConnection
                    
                    try {
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.doOutput = true
                        
                        // Tạo request body với conversation history
                        val contentsArray = org.json.JSONArray()
                        
                        // Thêm system instruction đầy đủ chỉ lần đầu (khi chưa có lịch sử)
                        // Với các request sau, chỉ cần nhắc nhở ngắn gọn trong tin nhắn người dùng
                        if (conversationHistory.isEmpty()) {
                            contentsArray.put(JSONObject().apply {
                                put("role", "user")
                                put("parts", org.json.JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", """Bạn là chatbot hỗ trợ du lịch. Trả lời CỰC KỲ NGẮN GỌN.

QUY TẮC BẮT BUỘC:
- Tối đa 2-4 câu ngắn
- Dùng bullet points (•) khi liệt kê
- Không giải thích dài dòng
- Trả lời trực tiếp vào câu hỏi
- Dùng tiếng Việt

QUY TẮC QUAN TRỌNG NHẤT - XỬ LÝ CÂU HỎI KHÔNG LIÊN QUAN:
BẠN CHỈ ĐƯỢC TRẢ LỜI CÂU HỎI VỀ DU LỊCH.

Nếu người dùng hỏi về bất kỳ chủ đề nào KHÔNG liên quan đến du lịch (ví dụ: toán học, lịch sử, công nghệ, tin tức, khoa học, văn học, thể thao, giải trí không liên quan du lịch, v.v.), BẠN PHẢI:
1. Từ chối một cách lịch sự
2. Nhắc nhở rằng bạn chỉ hỗ trợ về du lịch
3. Hỏi lại về du lịch

Câu trả lời mẫu BẮT BUỘC:
"Xin lỗi, tôi chỉ có thể hỗ trợ về du lịch thôi. Bạn muốn hỏi gì về du lịch không?"

KHÔNG BAO GIỜ trả lời câu hỏi không liên quan du lịch, dù người dùng có hỏi gì đi nữa.

Nếu câu hỏi hơi liên quan nhưng không rõ ràng, hãy cố gắng liên kết với du lịch hoặc hỏi lại:
"Bạn muốn biết về [chủ đề] trong chuyến du lịch phải không? Tôi có thể tư vấn về..."

Ví dụ câu trả lời tốt cho câu hỏi về du lịch:
"Đà Lạt có nhiều điểm thú vị:
• Hồ Xuân Hương
• Dinh Bảo Đại  
• Thung Lũng Tình Yêu
Chi phí khoảng 2-3 triệu/người/ngày."

KHÔNG viết dài như đoạn văn. Chỉ trả lời ngắn gọn, súc tích.""")
                                    })
                                })
                            })
                            contentsArray.put(JSONObject().apply {
                                put("role", "model")
                                put("parts", org.json.JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", "Hiểu rồi! Tôi sẽ trả lời cực kỳ ngắn gọn, tối đa 2-3 câu. QUAN TRỌNG: Tôi sẽ TỪ CHỐI và không trả lời bất kỳ câu hỏi nào không liên quan đến du lịch. Tôi chỉ hỗ trợ về du lịch thôi.")
                                    })
                                })
                            })
                        }
                        
                        // Thêm lịch sử hội thoại
                        conversationHistory.forEach { (userMsg, botMsg) ->
                            // User message
                            contentsArray.put(JSONObject().apply {
                                put("role", "user")
                                put("parts", org.json.JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", userMsg)
                                    })
                                })
                            })
                            
                            // Bot response
                            contentsArray.put(JSONObject().apply {
                                put("role", "model")
                                put("parts", org.json.JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", botMsg)
                                    })
                                })
                            })
                        }
                        
                        // Thêm tin nhắn hiện tại với nhắc nhở ngắn gọn về quy tắc
                        // Nhắc nhở này giúp model luôn nhớ từ chối câu hỏi không liên quan
                        val userMessageWithReminder = if (conversationHistory.isNotEmpty()) {
                            // Nếu đã có lịch sử, thêm nhắc nhở ngắn gọn nhưng rõ ràng
                            "[QUY TẮC: Bạn CHỈ trả lời câu hỏi về du lịch. Nếu câu hỏi KHÔNG liên quan du lịch (toán, lịch sử, công nghệ, tin tức, v.v.), bạn PHẢI từ chối và trả lời: 'Xin lỗi, tôi chỉ có thể hỗ trợ về du lịch thôi. Bạn muốn hỏi gì về du lịch không?']\n\nCâu hỏi: $message"
                        } else {
                            message
                        }
                        
                        contentsArray.put(JSONObject().apply {
                            put("role", "user")
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", userMessageWithReminder)
                                })
                            })
                        })
                        
                        val requestBody = JSONObject().apply {
                            put("contents", contentsArray)
                        }
                        
                        // Log request để debug (chỉ log một phần)
                        android.util.Log.d("AiServiceImpl", "Request body preview: ${requestBody.toString().take(200)}...")
                        
                        // Gửi request
                        connection.outputStream.use { output ->
                            output.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                        }
                        
                        // Đọc response
                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                            android.util.Log.d("AiServiceImpl", "Response body: ${response.take(500)}")
                            val jsonResponse = JSONObject(response)
                            
                            // Kiểm tra nếu có lỗi trong response
                            if (jsonResponse.has("error")) {
                                val error = jsonResponse.getJSONObject("error")
                                val errorMessage = error.optString("message", "Unknown error")
                                android.util.Log.e("AiServiceImpl", "API Error: $errorMessage")
                                lastException = Exception("API Error: $errorMessage")
                                continue
                            }
                            
                            val candidates = jsonResponse.getJSONArray("candidates")
                            if (candidates.length() > 0) {
                                val candidate = candidates.getJSONObject(0)
                                
                                // Kiểm tra nếu có finishReason và nó không phải là STOP
                                if (candidate.has("finishReason")) {
                                    val finishReason = candidate.getString("finishReason")
                                    if (finishReason != "STOP") {
                                        android.util.Log.w("AiServiceImpl", "Finish reason: $finishReason")
                                    }
                                }
                                
                                val content = candidate.getJSONObject("content")
                                val parts = content.getJSONArray("parts")
                                if (parts.length() > 0) {
                                    val text = parts.getJSONObject(0).getString("text")
                                    android.util.Log.d("AiServiceImpl", "Chat success with $apiVersion/models/$modelName")
                                    return text
                                }
                            }
                            throw Exception("Không tìm thấy text trong response")
                        } else {
                            val errorResponse = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "Unknown error"
                            android.util.Log.e("AiServiceImpl", "HTTP Error $responseCode for chat $apiVersion/models/$modelName")
                            android.util.Log.e("AiServiceImpl", "Error response: $errorResponse")
                            lastException = Exception("HTTP Error $responseCode: $errorResponse")
                            continue
                        }
                    } finally {
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AiServiceImpl", "Failed chat $apiVersion/models/$modelName: ${e.message}")
                    lastException = e
                    continue
                }
            }
        }
        
        // Nếu tất cả đều fail
        throw lastException ?: Exception("Tất cả models và API versions đều thất bại")
    }
    
    private fun buildChatPrompt(userMessage: String): String {
        // Không dùng nữa vì đã có system instruction trong conversation
        return userMessage
    }
    
    override suspend fun rankDestinationsByInterests(
        interests: List<String>,
        destinations: List<com.example.smarttravel.model.Destination>,
        recentPlanDestinationIds: List<String>
    ): Result<List<String>> {
        return try {
            if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                // Trả về sample data - lấy top 10 destinations đầu tiên
                return Result.success(destinations.take(10).map { it.id })
            }
            
            if (destinations.isEmpty()) {
                return Result.success(emptyList())
            }
            
            val prompt = buildRankingPrompt(interests, destinations, recentPlanDestinationIds)
            
            android.util.Log.d("AiServiceImpl", "Calling AI to rank destinations by interests")
            android.util.Log.d("AiServiceImpl", "User interests: ${interests.joinToString(", ")}")
            android.util.Log.d("AiServiceImpl", "Total destinations: ${destinations.size}")
            
            val response = withContext(Dispatchers.IO) {
                callGeminiApiDirectly(prompt)
            }
            
            android.util.Log.d("AiServiceImpl", "Ranking response received, length: ${response.length}")
            
            // Parse response để lấy danh sách destination IDs
            val rankedIds = parseRankingResponse(response, destinations)
            
            Result.success(rankedIds)
            
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error ranking destinations: ${e.message}", e)
            // Trả về sample data nếu có lỗi - lấy top 10 destinations đầu tiên
            Result.success(destinations.take(10).map { it.id })
        }
    }
    
    private fun buildRankingPrompt(
        interests: List<String>,
        destinations: List<com.example.smarttravel.model.Destination>,
        recentPlanDestinationIds: List<String>
    ): String {
        val interestsText = if (interests.isNotEmpty()) {
            interests.joinToString(", ")
        } else {
            "du lịch, khám phá"
        }
        
        // Tạo danh sách destinations để gửi cho AI
        val destinationsList = destinations.take(50).joinToString("\n") { dest ->
            "- ID: ${dest.id}, Tên: ${dest.name}, Địa điểm: ${dest.location_name}, Category: ${dest.category_id}, Rating: ${dest.rating}"
        }
        
        // Thông tin về các điểm đến từ plans gần đây
        val recentPlansInfo = if (recentPlanDestinationIds.isNotEmpty()) {
            val recentDestinations = destinations.filter { it.id in recentPlanDestinationIds }
            val recentNames = recentDestinations.joinToString(", ") { it.name }
            "\n\nCác điểm đến người dùng đã tạo kế hoạch gần đây (trong vòng 30 ngày): $recentNames\nLưu ý: Tránh gợi ý lại các điểm đến này, ưu tiên các điểm đến MỚI và KHÁC."
        } else {
            ""
        }
        
        return """
Bạn là chuyên gia du lịch. Hãy sắp xếp danh sách các điểm đến sau theo mức độ phù hợp với sở thích của người dùng.

Sở thích của người dùng: $interestsText$recentPlansInfo

Danh sách điểm đến:
$destinationsList

Yêu cầu:
1. Sắp xếp các điểm đến theo mức độ phù hợp với sở thích: $interestsText
2. Ưu tiên các điểm đến có rating cao và phù hợp với sở thích
3. TRÁNH gợi ý lại các điểm đến mà người dùng đã tạo kế hoạch gần đây (nếu có)
4. Ưu tiên các điểm đến MỚI, KHÁC với những gì người dùng đã khám phá
5. Trả về tối đa 20 điểm đến phù hợp nhất
6. Chỉ trả về danh sách ID, không cần giải thích

Trả về DƯỚI DẠNG JSON với cấu trúc chính xác như sau (chỉ trả về JSON, không có text thêm):

{
  "ranked_destination_ids": ["id1", "id2", "id3", ...]
}

Lưu ý:
- Chỉ trả về JSON, không có text thêm
- Sắp xếp theo thứ tự từ phù hợp nhất đến ít phù hợp hơn
- Tối đa 20 ID
- Ưu tiên điểm đến mới, khác với những gì đã khám phá
""".trimIndent()
    }
    
    private fun parseRankingResponse(
        response: String,
        destinations: List<com.example.smarttravel.model.Destination>
    ): List<String> {
        return try {
            var cleanJson = response.trim()
            // Loại bỏ markdown code blocks nếu có
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json").trim()
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```").trim()
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```").trim()
            }
            
            val jsonObject = JSONObject(cleanJson)
            val rankedIdsArray = jsonObject.getJSONArray("ranked_destination_ids")
            
            val rankedIds = mutableListOf<String>()
            for (i in 0 until rankedIdsArray.length()) {
                val id = rankedIdsArray.getString(i)
                // Kiểm tra xem ID có tồn tại trong danh sách destinations không
                if (destinations.any { it.id == id }) {
                    rankedIds.add(id)
                }
            }
            
            // Nếu không parse được hoặc không có ID nào hợp lệ, trả về top destinations theo rating
            if (rankedIds.isEmpty()) {
                android.util.Log.w("AiServiceImpl", "No valid IDs parsed, using top rated destinations")
                return destinations.sortedByDescending { it.rating }.take(20).map { it.id }
            }
            
            rankedIds
        } catch (e: Exception) {
            android.util.Log.e("AiServiceImpl", "Error parsing ranking response: ${e.message}", e)
            // Fallback: trả về top destinations theo rating
            destinations.sortedByDescending { it.rating }.take(20).map { it.id }
        }
    }
    
    private fun getSamplePlanDetail(): String {
        return """
[
  {
    "day": 1,
    "date": "2025-11-19",
    "title": "Ngày 1: Khám phá điểm đến",
    "activities": [
      {
        "time": "08:00",
        "type": "breakfast",
        "name": "Bữa sáng tại khách sạn",
        "location": "Khách sạn",
        "description": "Bắt đầu ngày mới với bữa sáng đầy đủ"
      },
      {
        "time": "09:00",
        "type": "attraction",
        "name": "Tham quan địa điểm nổi tiếng",
        "location": "Điểm tham quan chính",
        "description": "Khám phá các địa điểm du lịch nổi bật"
      },
      {
        "time": "12:00",
        "type": "lunch",
        "name": "Nhà hàng địa phương",
        "location": "Trung tâm thành phố",
        "description": "Thưởng thức ẩm thực địa phương"
      },
      {
        "time": "14:00",
        "type": "activity",
        "name": "Hoạt động giải trí",
        "location": "Khu vui chơi",
        "description": "Tham gia các hoạt động thú vị"
      },
      {
        "time": "18:00",
        "type": "dinner",
        "name": "Nhà hàng tối",
        "location": "Khu ẩm thực",
        "description": "Bữa tối thư giãn sau một ngày dài"
      }
    ]
  }
]
""".trimIndent()
    }
}

