# DexMon Android Simulator

A minimal Android app that simulates CGM sensor events against the local DexMon backend.

## Prerequisites
- Android Studio (Giraffe+)
- Android Emulator (API 34 recommended)
- Backend running locally (Receiver on 8081). Emulator uses `10.0.2.2` to reach host.

## Configure & Run
1. Open this project directory in Android Studio: `tools/android-simulator`.
2. Build and run on an emulator.
3. Optionally set `RECEIVER_BASE_URL` in Run/Debug configuration (defaults to `http://10.0.2.2:8081`).

The app will send 100 sensor readings to `POST /api/v1/glucose/sensor` at ~2/sec.
