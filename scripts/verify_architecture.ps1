# AutoMinder Architecture and Safety Verification Script
# Runs static checks and fast compilation to catch regressions before production.

Write-Host "Running AutoMinder Architecture and Safety Verification..." -ForegroundColor Cyan

$violations = @()

# 1. Check for forbidden kapt usage in Gradle files
$kaptMatches = Get-ChildItem -Path . -Filter "*.gradle.kts" -Recurse | Select-String "\bkapt\("
if ($kaptMatches) {
    $violations += "FORBIDDEN: Found kapt() in Gradle files. KSP is mandatory."
}

# 2. Check for destructive Room migrations (actual code invocation)
$destructiveMatches = Get-ChildItem -Path "app/src" -Filter "*.kt" -Recurse | Select-String "\.fallbackToDestructiveMigration\("
if ($destructiveMatches) {
    $violations += "FORBIDDEN: Found .fallbackToDestructiveMigration() call. Named migrations required."
}

# 3. Check for Int Primary Keys in Entities
$intPkMatches = Get-ChildItem -Path "app/src/main/kotlin/com/autominder/app/data/local/entity" -Filter "*Entity.kt" | Select-String "@PrimaryKey.*val id:\s*Int\b"
if ($intPkMatches) {
    $violations += "FORBIDDEN: Found Int primary key in Room Entity. Long PK is required."
}

if ($violations.Count -gt 0) {
    Write-Host ""
    Write-Host "ARCHITECTURE VIOLATIONS DETECTED:" -ForegroundColor Red
    foreach ($v in $violations) {
        Write-Host " - $v" -ForegroundColor Red
    }
    exit 1
}

Write-Host "Static architecture guardrails passed with 0 violations." -ForegroundColor Green

# 4. Fast compile check
Write-Host "Running fast quiet compilation check..." -ForegroundColor Cyan
& .\gradlew compileDebugKotlin --quiet --console=plain
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed." -ForegroundColor Red
    exit 1
}

Write-Host "All architecture gates and compilation checks passed green!" -ForegroundColor Green
exit 0
