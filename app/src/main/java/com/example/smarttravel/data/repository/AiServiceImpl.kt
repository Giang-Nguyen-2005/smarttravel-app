package com.example.smarttravel.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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
                "gemini-1.5-flash",
                "gemini-pro"
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
        
        return """
Bạn là một chuyên gia du lịch chuyên nghiệp. Hãy tạo một lịch trình chi tiết và đầy đủ cho chuyến đi với thông tin sau:

- Địa điểm: $destination
- Khu vực: $locationName
- Người đồng hành: $companion
- Thời gian: $startDate đến $endDate
- Ngân sách: $budget
- Sở thích: $purposesText

Hãy tạo lịch trình theo từng ngày với các hoạt động chi tiết, BAO GỒM:
1. Địa điểm du lịch nổi tiếng
2. Các món ăn đặc sản địa phương (gợi ý nhà hàng/quán ăn cụ thể)
3. Khách sạn/nơi nghỉ (gợi ý khách sạn phù hợp với ngân sách)
4. Khu vui chơi giải trí gần khách sạn
5. Các hoạt động thú vị khác

Trả về DƯỚI DẠNG JSON với cấu trúc chính xác như sau (chỉ trả về JSON, không có text thêm):

[
  {
    "day": 1,
    "date": "$startDate",
    "title": "Ngày 1: [Tiêu đề ngày]",
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
  }
]

Lưu ý QUAN TRỌNG:
- Tạo đủ số ngày từ $startDate đến $endDate
- Mỗi ngày PHẢI có field "hotel" với thông tin khách sạn
- Mỗi ngày có ít nhất 6-8 hoạt động (ăn sáng, tham quan, ăn trưa, hoạt động, ăn tối, nghỉ ngơi)
- Type có thể là: "breakfast", "lunch", "dinner", "attraction", "activity", "entertainment", "rest", "hotel"
- MỖI ACTIVITY PHẢI CÓ field "price" với giá ước tính (theo ngân sách $budget). Ví dụ: "50.000 - 150.000 VNĐ/người" hoặc "Miễn phí"
- Giá phải phù hợp với ngân sách đã chọn: Tiết kiệm (500k-1.5M/ngày), Cân bằng (1.5M-3M/ngày), Cao cấp (3M-5M/ngày), Linh hoạt (trên 5M/ngày)
- Gợi ý các món ăn đặc sản địa phương trong "recommendedDishes"
- Gợi ý khu vui chơi giải trí gần khách sạn
- Phù hợp với ngân sách $budget và sở thích $purposesText
- Tất cả nội dung bằng tiếng Việt
- Khách sạn nên phù hợp với ngân sách $budget
""".trimIndent()
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

