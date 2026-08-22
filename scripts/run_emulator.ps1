# AutoMinder Emulator & Live Preview Runner
$ErrorActionPreference = "Stop"

$sdk = "$env:USERPROFILE\AppData\Local\Android\Sdk"
$emuPath = "$sdk\emulator"
$platPath = "$sdk\platform-tools"
$adb = "$platPath\adb.exe"

# Export Environment
$env:PATH = "$emuPath;$platPath;$env:PATH"
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk

Write-Host "=== AutoMinder Emulator Runner ==="
Write-Host "SDK Path: $sdk"

# Check if an emulator device is already running
$devices = & $adb devices | Select-String "device$"
if ($devices.Count -eq 0) {
    Write-Host "No running AVD found. Starting Medium_Phone_API_36.1..."
    Start-Process -FilePath "$emuPath\emulator.exe" -ArgumentList "-avd Medium_Phone_API_36.1 -gpu auto" -WorkingDirectory $emuPath
    Write-Host "Waiting for AVD to appear in ADB..."
    & $adb wait-for-device
    
    Write-Host "Waiting for device boot completion..."
    while ($true) {
        $booted = & $adb shell getprop sys.boot_completed 2>$null
        if ($booted -eq "1") {
            Write-Host "AVD fully booted!"
            break
        }
        Start-Sleep -Seconds 2
    }
} else {
    Write-Host "Connected emulator found: $($devices[0])"
}

# Deploy debug build
Write-Host "Installing AutoMinder debug build..."
& .\gradlew installDebug

# Start app
Write-Host "Launching AutoMinder..."
& $adb shell am start -n com.autominder.app/.MainActivity
Start-Sleep -Seconds 3

# Dismiss keyboard if open
& $adb shell input keyevent 4
Start-Sleep -Seconds 1

# Capture live screen
$destPath = "C:\Users\Light-Tech\.gemini\antigravity-ide\brain\aed0cafa-abd3-480b-b7a2-7008d17f7ac5\avd_live_preview.png"
& $adb shell screencap -p /sdcard/avd_live.png
& $adb pull /sdcard/avd_live.png $destPath

Write-Host "SUCCESS: Live preview saved to $destPath"
