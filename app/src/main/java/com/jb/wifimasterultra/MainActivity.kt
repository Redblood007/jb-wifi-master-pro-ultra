package com.jb.wifimasterultra

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UltraApp(this) }
    }
}

@Composable
private fun UltraApp(context: Context) {
    var report by remember { mutableStateOf(readNetworkReport(context)) }
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
                    }
                }
                Button(onClick = { report = readNetworkReport(context) }, modifier = Modifier.fillMaxWidth()) { Text("REFRESH DIAGNOSTICS") }
                Text("TOOLS", color = Color(0xFF55E6C1), fontSize = 12.sp, letterSpacing = 2.sp)
                ToolCard("Speed test", "Download, upload and latency measurement — engine pending.")
                ToolCard("Wi-Fi analyzer", "Signal, band and channel analysis — scanner pending.")
                ToolCard("Security lab", "Authorized router checks only. No password cracking.")
                ToolCard("VPN diagnostics", "Tunnel status and WireGuard integration planned.")
                Text("Build 1.0.0 • Early development preview", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF122238))) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = Color(0xFF55E6C1), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 19.sp)
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
