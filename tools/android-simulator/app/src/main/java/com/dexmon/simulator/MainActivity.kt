package com.dexmon.simulator

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.dexmon.simulator.R
import com.dexmon.simulator.data.AppDb
import com.dexmon.simulator.data.CachedReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val client = OkHttpClient()

    private val eventQueue: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
    private lateinit var db: AppDb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDb::class.java, "dexmon.db").build()

        val baseUrl = System.getenv("RECEIVER_BASE_URL") ?: "http://10.0.2.2:8081"
        val gatewayUrl = System.getenv("GATEWAY_BASE_URL") ?: "http://10.0.2.2:8085"
        val endpoint = "$baseUrl/api/v1/glucose/sensor"
        val sensorId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()

        // Producer
        scope.launch {
            while (isActive) {
                val value = (70..260).random()
                val body = """
                    {
                      "eventId": "${UUID.randomUUID()}",
                      "sensorId": "$sensorId",
                      "userId": "$userId",
                      "timestamp": "${Instant.now()}\u0022,
                      "glucoseValue": $value,
                      "source": "SENSOR",
                      "version": 1
                    }
                """.trimIndent()
                eventQueue.add(body)
                delay(500)
            }
        }

        // Consumer: batch send
        scope.launch {
            var baseIntervalMs = 500L
            val minInterval = 200L
            val maxInterval = 2000L
            val batchSize = 10
            while (isActive) {
                baseIntervalMs = adaptInterval(baseIntervalMs, minInterval, maxInterval)
                val batch = mutableListOf<String>()
                while (batch.size < batchSize) {
                    val e = eventQueue.poll() ?: break
                    batch.add(e)
                }
                if (batch.isNotEmpty()) {
                    for (payload in batch) {
                        sendWithRetry(endpoint, payload)
                        delay(50)
                    }
                }
                delay(baseIntervalMs)
            }
        }

        // Prefetcher: adaptive periodic sync from Gateway into Room
        scope.launch {
            var syncInterval = 5000L
            val minSync = 3000L
            val maxSync = 20000L
            while (isActive) {
                syncInterval = adaptInterval(syncInterval, minSync, maxSync)
                try {
                    val to = Instant.now()
                    val from = to.minus(15, ChronoUnit.MINUTES)
                    val url = "$gatewayUrl/api/v1/readings?userId=$userId&from=$from&to=$to"
                    val req = Request.Builder().url(url).get().build()
                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string()
                            if (!body.isNullOrEmpty()) {
                                val arr = JSONArray(body)
                                val list = ArrayList<CachedReading>(arr.length())
                                for (i in 0 until arr.length()) {
                                    val o = arr.getJSONObject(i)
                                    val ts = o.getString("timestamp")
                                    val value = o.getInt("glucose_value")
                                    val sid = o.getString("sensor_id")
                                    val uid = o.getString("user_id")
                                    val src = o.getString("source")
                                    val id = "$sid-$ts"
                                    list.add(CachedReading(id, sid, uid, ts, value, src, 1))
                                }
                                db.readings().upsertAll(list)
                            }
                        }
                    }
                } catch (_: Exception) {}
                delay(syncInterval)
            }
        }
    }

    private suspend fun sendWithRetry(endpoint: String, body: String) {
        var attempt = 0
        val maxAttempts = 5
        while (attempt < maxAttempts) {
            val req = Request.Builder()
                .url(endpoint)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            try {
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful || resp.code in 200..299 || resp.code == 409) {
                        return
                    }
                }
            } catch (_: Exception) {}
            attempt++
            val backoffMs = computeBackoffWithJitter(attempt)
            delay(backoffMs)
        }
        eventQueue.add(body)
    }

    private fun computeBackoffWithJitter(attempt: Int): Long {
        val base = 300L
        val max = 5000L
        val exp = min(1.5.pow(attempt.toDouble()) * base, max.toDouble()).toLong()
        val jitter = Random.nextLong(0, 250)
        return exp + jitter
    }

    private fun adaptInterval(current: Long, minInterval: Long, maxInterval: Long): Long {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return min(maxInterval, current + 250)
        val caps = cm.getNetworkCapabilities(nw) ?: return min(maxInterval, current + 250)
        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCell = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val target = when {
            isWifi -> minInterval
            isCell -> (minInterval + maxInterval) / 2
            else -> maxInterval
        }
        return when {
            current < target -> min(target, current + 100)
            current > target -> max(target, current - 100)
            else -> current
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
