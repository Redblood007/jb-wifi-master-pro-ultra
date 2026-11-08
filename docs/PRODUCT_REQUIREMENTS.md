# Product Requirements — JB WiFi Master Pro Ultra

## Product goal
Build a reliable, professional Android toolkit for improving the user's understanding and management of their own internet and Wi-Fi connections.

## Primary user journeys
- Run a download/upload/latency test and review the result.
- Scan nearby access points and compare signal strength and channel congestion where Android permits.
- Diagnose DNS, gateway, packet loss, and VPN connectivity.
- Inventory devices on a network the user owns or administers.
- Run safe security checks against an explicitly authorized router.
- Connect and diagnose a user-configured WireGuard VPN tunnel.

## Android app architecture
- app: Compose navigation, app shell, permissions, settings.
- core-network: connectivity state, DNS, ping/latency, test abstractions.
- feature-speedtest: speed test UI and measurement orchestration.
- feature-wifi: Wi-Fi scan results, signal/channel analysis.
- feature-security: authorized audit checks and findings.
- feature-vpn: Android VpnService integration and WireGuard adapter.
- feature-monitor: traffic and connection monitoring.
- core-storage: encrypted local preferences and diagnostic history.

## Privacy and safety
- Request only permissions needed for a feature and explain why.
- Keep credentials and private keys out of logs and source control.
- Store secrets using Android Keystore-backed storage.
- Never scan networks without user authorization.
- Do not implement password guessing/cracking or bypass controls.

## Delivery milestones
1. Android Gradle project and Compose shell.
2. Permission-aware Wi-Fi scanner and connection diagnostics.
3. Speed test, DNS, ping, jitter, and packet-loss measurement.
4. Authorized network audit and device inventory.
5. WireGuard VPN diagnostics.
6. Automated tests, release signing, APK build, and physical-device validation.

## Definition of done
A release is complete only when the Android project builds reproducibly, core features pass tests, the signed APK installs on a supported device, and network behavior is verified on a real test network.
