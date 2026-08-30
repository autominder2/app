# 💾 Auto-Backup & Data Sovereignty Test Report
**Application:** AutoMinder (`com.autominder.app`)  
**Target:** Android 11 and below (`backup_rules.xml`) + Android 12+ (`data_extraction_rules.xml`)  
**Test Subject:** `AutoMinderBackupAgent` + SQLite WAL Checkpoint + Entitlement Isolation  
**Date:** August 2026

---

## 1. Executive Summary

AutoMinder provides **seamless data retention across device upgrades** while maintaining **airtight entitlement security**. User vehicle logs, service records, fuel logs, and reminders survive phone transfers, but pirated Pro status replication is strictly blocked.

---

## 2. Backup Rules Configuration Matrix

```xml
<!-- Full Backup & Data Extraction Invariants -->
<full-backup-content>
    <!-- User Data Included -->
    <include domain="database" path="." />
    <include domain="file" path="datastore/" />
    <include domain="sharedpref" path="." />
    
    <!-- Volatile Database Cache Excluded -->
    <exclude domain="database" path="autominder.db-wal" />
    <exclude domain="database" path="autominder.db-shm" />
    
    <!-- Pro Entitlement Cache Excluded (Prevents Pirated Restore) -->
    <exclude domain="file" path="datastore/entitlement_cache.preferences_pb" />
</full-backup-content>
```

---

## 3. Operational Proof & Verification

| Test Scenario | Procedure | Expected Outcome | Verified Status |
|---|---|---|---|
| **1. Database WAL Checkpoint** | `AutoMinderBackupAgent.onFullBackup()` invoked. | Triggers `PRAGMA wal_checkpoint(FULL)`, consolidating all uncommitted writes into `autominder.db` before cloud sync. | ✅ **PASSED** |
| **2. Device Upgrade Restore** | Execute `bmgr backupnow` $\rightarrow$ `pm clear` $\rightarrow$ `bmgr restore`. | All vehicles, reminders, service logs, and fuel entries restored with 100% fidelity. | ✅ **PASSED** |
| **3. Entitlement Bypass Prevention** | Backup Pro account $\rightarrow$ restore to secondary device without internet. | `entitlement_cache.preferences_pb` is omitted; secondary device initializes in standard Free tier until Play confirms purchase. | ✅ **PASSED** |
| **4. Manifest Invariant** | Inspect `AndroidManifest.xml` declaration. | `android:fullBackupOnly="true"` is declared to force full backup mode and prevent key/value fallback. | ✅ **PASSED** |
