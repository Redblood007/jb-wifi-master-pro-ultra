# JB WiFi Master Pro Ultra

A standalone Android application for internet diagnostics, Wi-Fi analysis, authorized network security auditing, and VPN diagnostics.

## Project status

This repository is the new, independent project foundation. It is not a completed Android app and does not yet contain an installable APK.

## Planned Android stack

- Kotlin
- Jetpack Compose
- Android Wi-Fi and Connectivity APIs
- Android VpnService with WireGuard integration planned as a separate module
- Supabase backend integration planned for account and service features

## Planned modules

1. Internet speed, latency, jitter, packet-loss, and DNS diagnostics
2. Wi-Fi signal, band, channel, and congestion analysis
3. Authorized network inventory and router security checks
4. VPN connection diagnostics and WireGuard integration
5. Traffic usage and connection monitoring
6. Diagnostic history, reports, and settings

## Security and platform boundaries

- Security testing is limited to networks the user owns or is explicitly authorized to assess.
- No Wi-Fi password cracking, credential theft, or unauthorized network access.
- Android version, device hardware, location permissions, and OS restrictions limit available Wi-Fi scan and connection functions.
- Speed tests measure the current internet path; the app cannot increase ISP-provided bandwidth.

## Build status

Initial repository documentation only. Android source implementation, backend configuration, automated tests, signed APK, and device validation are pending.
