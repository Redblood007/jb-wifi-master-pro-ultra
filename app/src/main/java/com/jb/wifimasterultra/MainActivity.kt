package com.jb.wifimasterultra

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UltraApp(this) }
    }
}

private data class WifiItem(val ssid: String, val level: Int, val frequency: Int, val security: String)
private data class SpeedResult(val ping: String, val jitter: String, val packetLoss: String, val download: String, val upload: String, val note: String)

@Composable
private fun UltraApp(activity: ComponentActivity) {
    val context = activity as Context
    var report by remember { mutableStateOf(readNetworkReport(context)) }
    var speed by remember { mutableStateOf(SpeedResult("--", "--", "--", "--", "--", "Run a test to measure this connection.")) }
    var speedBusy by remember { mutableStateOf(false) }
    var wifiBusy by remember { mutableStateOf(false) }
    var wifiMessage by remember { mutableStateOf("Scan nearby networks you are authorized to use.") }
    var wifiItems by remember { mutableStateOf(emptyList<WifiItem>()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allowed = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (allowed) scanWifi(context, { wifiBusy = it }, { wifiItems = it }, { wifiMessage = it })
        else wifiMessage = "Location permission is required by Android to show nearby Wi-Fi scan results."
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF55E6C1), background = Color(0xFF07111F), surface = Color(0xFF122238))) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF07111F)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("JB WIFI MASTER", color = Color.White, fontSize = 13.sp, letterSpacing = 3.sp)
                Text("PRO ULTRA", color = Color(0xFF55E6C1), fontSize = 30.sp)
                Text("Network intelligence dashboard", color = Color.LightGray)
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF122238))) {
                    Column(Modifier.padding(18.dp)) {
                        Text("CURRENT CONNECTION", color = Color(0xFF55E6C1), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(report.type, color = Color.White, fontSize = 24.sp)
                        Text(report.status, color = Color.LightGray)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("INTERNET", report.internet, Modifier.weight(1f))
                    MetricCard("METERED", report.metered, Modifier.weight(1f))
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF122238))) {
                    Column(Modifier.padding(18.dp)) {
                        Text("NETWORK DETAILS", color = Color(0xFF55E6C1))
                        Spacer(Modifier.height(8.dp))
                        Text(report.details, color = Color.White, lineHeight = 22.sp)
                        Button(onClick = { report = readNetworkReport(context) }, modifier = Modifier.fillMaxWidth()) { Text("REFRESH DIAGNOSTICS") }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF122238))) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("INTERNET SPEED TEST", color = Color(0xFF55E6C1), fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricCard("PING", speed.ping, Modifier.weight(1f))
                            MetricCard("JITTER", speed.jitter, Modifier.weight(1f))
                            MetricCard("LOSS", speed.packetLoss, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricCard("DOWNLOAD", speed.download, Modifier.weight(1f))
                            MetricCard("UPLOAD", speed.upload, Modifier.weight(1f))
                        }
                        Text(speed.note, color = Color.LightGray, fontSize = 12.sp)
                        Button(onClick = {
                            if (!speedBusy) {
                                speedBusy = true
                                speed = SpeedResult("Testing", "Testing", "Testing", "Testing", "Testing", "Measuring against Cloudflare's public speed-test endpoint…")
                                runSpeedTest(
                                    onResult = { speed = it; speedBusy = false },
                                    onError = { speed = SpeedResult("--", "--", "--", "--", "--", it); speedBusy = false }
                                )
                            }
                        }, enabled = !speedBusy, modifier = Modifier.fillMaxWidth()) {
                            Text(if (speedBusy) "TEST RUNNING…" else "START SPEED TEST")
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF122238))) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("WI-FI ANALYZER", color = Color(0xFF55E6C1), fontSize = 13.sp)
                        Text(wifiMessage, color = Color.LightGray, fontSize = 12.sp)
                        Button(onClick = {
                            val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
                            if (Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                            val missing = permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                            if (missing.isEmpty()) scanWifi(context, { wifiBusy = it }, { wifiItems = it }, { wifiMessage = it })
                            else permissionLauncher.launch(missing.toTypedArray())
                        }, enabled = !wifiBusy, modifier = Modifier.fillMaxWidth()) {
                            Text(if (wifiBusy) "SCANNING…" else "SCAN WI-FI NETWORKS")
                        }
                        wifiItems.forEach { item ->
                            Column {
                                Text(item.ssid, color = Color.White)
                                Text("${item.level} dBm • ${item.frequency} MHz • ${item.security}", color = Color.LightGray, fontSize = 11.sp)
                            }
                            HorizontalDivider(color = Color(0xFF29415C))
                        }
                    }
                }
                ToolCard("Security lab", "Authorized router security checks only. No password cracking or unauthorized access.")
                ToolCard("VPN diagnostics", "VPN status and WireGuard integration are planned for a later milestone.")
                Text("Build 1.1.0 • Development preview", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF19314A))) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Color(0xFF55E6C1), fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ToolCard(title: String, description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF122238)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontSize = 17.sp)
            Text(description, color = Color.LightGray, fontSize = 13.sp)
        }
    }
}

private data class NetworkReport(val type: String, val status: String, val internet: String, val metered: String, val details: String)

private fun readNetworkReport(context: Context): NetworkReport {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = manager.activeNetwork
    val caps = network?.let { manager.getNetworkCapabilities(it) }
    val props = network?.let { manager.getLinkProperties(it) }
    val type = when {
        caps == null -> "Offline"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        else -> "Other network"
    }
    val validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    val metered = if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true) "No" else "Yes / unknown"
    val details = buildString {
        append("Internet capability: ").append(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true).append('\n')
        append("Validated by Android: ").append(validated).append('\n')
        append("DNS servers: ").append(props?.dnsServers?.joinToString() ?: "Unavailable").append('\n')
        append("Interface: ").append(props?.interfaceName ?: "Unavailable")
    }
    return NetworkReport(type, if (validated) "Android reports internet access" else "Internet access not confirmed", if (validated) "Validated" else "Not validated", metered, details)
}

private fun runSpeedTest(onResult: (SpeedResult) -> Unit, onError: (String) -> Unit) {
    thread {
        try {
            val pingSamples = mutableListOf<Long>()
            var failedPings = 0
            repeat(5) {
                var connection: HttpURLConnection? = null
                try {
                    connection = URL("https://www.cloudflare.com/cdn-cgi/trace").openConnection() as HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    connection.requestMethod = "GET"
                    val start = System.nanoTime()
                    connection.inputStream.use { input -> input.readBytes() }
                    pingSamples.add((System.nanoTime() - start) / 1_000_000)
                } catch (_: Exception) {
                    failedPings++
                } finally {
                    connection?.disconnect()
                }
            }
            if (pingSamples.isEmpty()) throw IllegalStateException("All latency probes failed")
            val ping = pingSamples.average().toLong()
            val jitter = if (pingSamples.size > 1) {
                pingSamples.zipWithNext { a, b -> kotlin.math.abs(b - a) }.average().toLong()
            } else 0L
            val packetLoss = failedPings * 100 / 5

            val downloadConnection = URL("https://speed.cloudflare.com/__down?bytes=5000000").openConnection() as HttpURLConnection
            downloadConnection.connectTimeout = 12000
            downloadConnection.readTimeout = 20000
            val downloadStart = System.nanoTime()
            val downloaded = downloadConnection.inputStream.use { input ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                }
                total
            }
            val downloadSeconds = ((System.nanoTime() - downloadStart).coerceAtLeast(1L)) / 1_000_000_000.0
            downloadConnection.disconnect()
            val downloadMbps = downloaded * 8.0 / downloadSeconds / 1_000_000.0

            val payload = ByteArray(1_000_000) { (it % 251).toByte() }
            val uploadConnection = URL("https://speed.cloudflare.com/__up").openConnection() as HttpURLConnection
            uploadConnection.requestMethod = "POST"
            uploadConnection.doOutput = true
            uploadConnection.connectTimeout = 12000
            uploadConnection.readTimeout = 20000
            uploadConnection.setFixedLengthStreamingMode(payload.size)
            uploadConnection.setRequestProperty("Content-Type", "application/octet-stream")
            val uploadStart = System.nanoTime()
            uploadConnection.outputStream.use { it.write(payload) }
            val responseCode = uploadConnection.responseCode
            val uploadSeconds = ((System.nanoTime() - uploadStart).coerceAtLeast(1L)) / 1_000_000_000.0
            if (responseCode !in 200..299) throw IllegalStateException("Upload test server returned HTTP $responseCode")
            val uploadMbps = payload.size * 8.0 / uploadSeconds / 1_000_000.0
            uploadConnection.disconnect()

            Handler(Looper.getMainLooper()).post {
                onResult(SpeedResult("${ping} ms", "${jitter} ms", "${packetLoss}%", String.format("%.1f Mbps", downloadMbps), String.format("%.1f Mbps", uploadMbps), "Five HTTPS latency probes; loss is failed probes, not ICMP packet loss. Throughput uses Cloudflare endpoints and varies with server and network load."))
            }
        } catch (error: Exception) {
            Handler(Looper.getMainLooper()).post {
                onError("Speed test failed: ${error.message ?: "network error"}. Check internet access and try again.")
            }
        }
    }
}

private fun scanWifi(
    context: Context,
    onBusy: (Boolean) -> Unit,
    onItems: (List<WifiItem>) -> Unit,
    onMessage: (String) -> Unit
) {
    onBusy(true)
    try {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val started = wifi.startScan()
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val results = wifi.scanResults
                    .sortedByDescending { it.level }
                    .take(30)
                    .map { result ->
                        val security = when {
                            result.capabilities.contains("WPA3") -> "WPA3"
                            result.capabilities.contains("WPA2") -> "WPA2"
                            result.capabilities.contains("WEP") -> "WEP (legacy)"
                            result.capabilities.contains("ESS") -> "Open / legacy"
                            else -> "Unknown"
                        }
                        WifiItem(result.SSID.ifBlank { "(Hidden network)" }, result.level, result.frequency, security)
                    }
                onItems(results)
                onMessage(if (results.isEmpty()) "No scan results. Enable device location and Wi-Fi, then retry. Android may throttle scans." else "Found ${results.size} visible networks. Scan availability is controlled by Android.")
            } catch (e: SecurityException) {
                onMessage("Android denied Wi-Fi scan access. Grant the requested permissions.")
            } finally {
                onBusy(false)
            }
        }, if (started) 2500L else 1000L)
    } catch (e: SecurityException) {
        onMessage("Wi-Fi scan permission is missing.")
        onBusy(false)
    } catch (e: Exception) {
        onMessage("Wi-Fi scan unavailable: ${e.message ?: "unknown error"}")
        onBusy(false)
    }
}
