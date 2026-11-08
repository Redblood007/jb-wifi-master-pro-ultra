# JB WiFi Master Pro Ultra

Native Android app built with Kotlin and Jetpack Compose.

## Implemented in the current development build
- Network type, Android internet validation, metered state, DNS server and interface display
- HTTPS latency probes, jitter estimate, failed-probe percentage, download and upload throughput using public Cloudflare endpoints
- Nearby Wi-Fi scan results with RSSI, frequency, band, channel and advertised security mode (subject to Android permissions and scan throttling)
- DNS resolution timing for example.com using the device's configured resolver
- Android VPN transport status detection

## Not yet complete / not verified
- No integrated WireGuard VPN client or VPN server provisioning
- No signed release APK or Play Store release
- No device-level QA has been completed; speed-test endpoint behavior and Wi-Fi scan access must be tested on supported phones
- No persistent history, accounts, remote backend, or router configuration feature
- Local device inventory and advanced router audit are not implemented

## Build
Pushes to main run the Android GitHub Actions workflow. The workflow currently produces a debug APK artifact. Debug builds are for testing and are not signed release packages.

## Platform and safety notes
- Wi-Fi scan results depend on Android version, location services, permissions, and OS scan throttling.
- Speed tests measure the current network path; they cannot increase ISP bandwidth.
- Security diagnostics must only be used on networks the operator owns or is authorized to assess. The app does not crack Wi-Fi passwords or bypass access controls.
