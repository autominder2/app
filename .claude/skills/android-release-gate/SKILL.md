---
description: Use to run AutoMinder release-hardening Gradle gates and report failures with root cause and next action.
allowed-tools: Bash, Read, Grep
disable-model-invocation: true
effort: medium
---

# AutoMinder Android Release Gate

## Gates

Release-hardening gate (any task touching shipped behavior):

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Release-candidate gate (before tagging a Play Console build — run IN ADDITION):

```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

## Windows invocation (this machine — required or builds hang)

JDK 17 Unix-domain-socket path length issue; always set `_JAVA_OPTIONS` and disable daemon/config cache:

```powershell
$env:_JAVA_OPTIONS = "-Djdk.net.unixdomain.tmpdir=C:\tmp -Djava.net.preferIPv4Stack=true"
& D:\Autominder\gradlew.bat clean assembleDebug --no-configuration-cache --no-daemon *> "$env:TEMP\am_build.txt"
"EXIT:$LASTEXITCODE"; Get-Content "$env:TEMP\am_build.txt" -Tail 40
```

Repeat per task (`testDebugUnitTest`, `lintDebug`, …) with separate temp log files. Do not write log files into the repo root.

Note: the `Picked up _JAVA_OPTIONS` stderr line triggers a spurious PowerShell NativeCommandError message — ignore it; trust `$LASTEXITCODE`.

## Failure protocol

If any command fails:
1. Stop.
2. Show the first meaningful error (not the last stack frame).
3. Explain likely root cause.
4. Do not start another task.
5. Recommend the minimal fix.

## Known pre-existing warnings (do not report as new)

- `SubscriptionManager.kt` SERVICE_TIMEOUT deprecation
- `UpdateHelper.kt` startUpdateFlowForResult deprecation
- `FuelHistoryViewModel.kt` ExperimentalCoroutinesApi opt-in
- `VehicleDetailViewModel.kt` unchecked casts (4×)

## Output

- Per-command result + duration if available.
- Warnings introduced by the current diff (vs. the known list above).
- Final gate verdict: GREEN / RED with blocking error.
