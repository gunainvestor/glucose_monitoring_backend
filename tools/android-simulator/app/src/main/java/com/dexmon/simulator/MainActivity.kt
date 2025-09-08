package com.dexmon.simulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No UI: fire-and-forget simulation on launch
        val baseUrl = System.getenv("RECEIVER_BASE_URL") ?: "http://10.0.2.2:8081"
        val sensorId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val endpoint = "$baseUrl/api/v1/glucose/sensor"

        scope.launch {
            repeat(100) {
                val value = (70..260).random()
                val body = """
                    {
                      "eventId": "${UUID.randomUUID()}",
                      "sensorId": "$sensorId",
                      "userId": "$userId",
                      "timestamp": "${java.time.Instant.now()}",
                      "glucoseValue": $value,
                      "source": "SENSOR",
                      "version": 1
                    }
                """.trimIndent()
                val req = Request.Builder()
                    .url(endpoint)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                try { client.newCall(req).execute().use { } } catch (_: Exception) {}
                delay(500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
