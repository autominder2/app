---
description: Use after AutoMinder UI changes to inspect the connected AVD, capture screenshots, and verify real visual output in light, dark, and large text where safe.
allowed-tools: Read, Grep, Bash
effort: high
---

# AutoMinder AVD Visual QA

## Goal

Verify real UI on the emulator. Do not trust code alone — screenshots are the evidence.

## Environment recipe (proven on this machine)

- adb: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`
- Emulator: `Medium_Phone_API_36.1` (1080x2400). Boot if no device:
  `Start-Process "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -ArgumentList '-avd','Medium_Phone_API_36.1','-no-snapshot-save' -WindowStyle Minimized`
- Wake screen before capture: `adb shell input keyevent KEYCODE_WAKEUP` then `KEYCODE_MENU`.
- **Launch trap:** debug builds expose LeakCanary's launcher activity. Never use `monkey` with the LAUNCHER category — launch explicitly:
  `adb shell am start -n com.autominder.app/.MainActivity`
- **Screencap trap:** PowerShell 5.1 `>` corrupts binary output. Capture via cmd:
  `cmd /c "\"<adb>\" exec-out screencap -p > D:\tmp\autominder-qa\<name>.png"`
- Screenshots go to `D:\tmp\autominder-qa\`.
- Splash screen takes ~5s; wait before the first capture, retake if the teal car splash is captured.
- Bottom nav tap coordinates (1080x2400): Home (126, 2225), Vehicles (400, 2225), Records (676, 2225), Settings (952, 2225).

## Screens to capture when reachable

Dashboard/Home, Vehicles, Vehicle Detail top, Vehicle Detail reminders, Records, Add Reminder, Add Service, Add Fuel if reachable, Settings, Pro Paywall, dark-mode Dashboard if safe, large-font Dashboard if safe.

## Safety

- Do not wipe app data unless explicitly approved.
- If changing emulator settings, ALWAYS: record original → apply temporary → capture → restore → verify restore.
  - Dark mode: `adb shell cmd uimode night yes|no` (verify with `adb shell "cmd uimode night"`)
  - Font scale: `adb shell settings get system font_scale` / `settings put system font_scale 1.5` / restore to original.
- If restore is uncertain, do not change the setting; report manual QA needed.

## Visual checklist

Confirm:
- no `Year: 0`
- no raw unformatted odometer (must be `201,000 km`, not `201000 km`)
- no dead/blank CTA
- no false paywall rows; Free/Pro column headers present
- no generic "OK" chip for unknown/disabled status
- status visible by text + color + shape/accent
- OVERDUE cards lead with "Overdue by X km" when the mileage trigger fired, never a future date first
- no clipped titles
- no huge dead space
- no lavender/default-Material surfaces where AutoMinder teal-tonal is expected
- dark mode legible
- large text not clipped
- bottom nav active state clear

## Output

For each screenshot: path, screen, mode (light/dark/large-font), what it confirms, remaining issue if found.

End with: pass/fail visual verdict + next recommended slice.
