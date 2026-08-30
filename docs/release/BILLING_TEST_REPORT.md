# 💳 Play Billing 9.1.0 Hardware Validation & Test Report
**Application:** AutoMinder (`com.autominder.app`)  
**Billing Library:** Google Play Billing 9.1.0  
**Products Configured:** `autominder_pro_monthly`, `autominder_pro_yearly`, `autominder_pro_lifetime`  
**Date:** August 2026

---

## 1. Billing Architecture & Implementation Highlights

AutoMinder's [`SubscriptionManager.kt`](file:///d:/Autominder/app/src/main/kotlin/com/autominder/app/billing/SubscriptionManager.kt) enforces 4 core resilience mechanisms:
1. **Separate Querying for Subs & In-App:** Queries `ProductType.SUBS` and `ProductType.INAPP` separately to prevent Billing 7+ query rejection.
2. **Immediate Purchase Acknowledgment:** Automatically invokes `acknowledgePurchase()` on successful transaction and during startup reconciliation to prevent Google Play's 3-day automatic refund rule.
3. **Safe Anti-Downgrade Entitlement Protection:** Never downgrades a cached Pro user if a network error occurs during startup. Downgrades occur ONLY when both queries return `OK` with zero active purchases.
4. **Offline Cold-Start Grace:** Reads `isProCached` on startup so offline users retain Pro capabilities while in dead zones or airplane mode.

---

## 2. Real-Device Hardware Test Matrix

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          REAL HARDWARE BILLING PROTOCOL                     │
│                                                                             │
│  [ TEST 1: NEW PURCHASE FLOW ]                                              │
│  Free User ➔ Tap "Unlock Pro" ➔ Select Yearly ➔ Google Play Sheet ➔         │
│  Confirm Purchase ➔ Instant Pro Entitlement ➔ Kill App ➔ Reopen ➔ Remains Pro│
│                                                                             │
│  [ TEST 2: RESTORE PURCHASES ON SECOND DEVICE ]                             │
│  Fresh Install on Device B ➔ Open Settings ➔ Tap "Restore Purchases" ➔      │
│  Query Play Store ➔ Active Entitlement Confirmed ➔ Pro Unlocked             │
│                                                                             │
│  [ TEST 3: AIRPLANE MODE & OFFLINE COLD START ]                             │
│  Active Pro User ➔ Enable Airplane Mode ➔ Force Kill App ➔ Cold Launch ➔    │
│  Cached Entitlement Honored ➔ Full Pro Features Accessible Offline          │
│                                                                             │
│  [ TEST 4: PENDING / SLOW PAYMENT FLOW ]                                    │
│  Select Bank Transfer / Cash ➔ State: PENDING ➔ User notified in UI ➔       │
│  No Pro Granted until Play confirms PURCHASED state                         │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Product Details & SKU Mapping

| SKU ID | Type | Pricing Tier | Purpose |
|---|---|---|---|
| `autominder_pro_monthly` | Subscription | $2.99 / month | Low commitment road-trip / short-term usage. |
| `autominder_pro_yearly` | Subscription | $19.99 / year | **Primary Value Driver** (44% discount anchor). |
| `autominder_pro_lifetime` | One-Time In-App | $39.99 one-time | **Founder Edition** (Zero subscription fatigue). |
