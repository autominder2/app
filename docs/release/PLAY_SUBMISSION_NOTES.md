# 📝 Google Play Console Submission Notes & Data Safety Guide
**Application:** Milevora (`com.autominder.app`)  
**Store Title:** `Milevora: Car Maintenance`  
**Target Submission Track:** Google Play Internal Testing $\rightarrow$ Production  
**Merged Manifest Permissions:** 14 Total (4 App-Declared + 10 Library-Merged)  
**Date:** August 2026

---

## 1. Google Play Data Safety Form Answers (Verified Ground Truth)

To guarantee zero policy rejections during automated and manual Google Play Store reviews, declare the following exact responses based on our **14 merged permissions** and SDK footprint:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PLAY CONSOLE DATA SAFETY DECLARATION                     │
│                                                                             │
│  • Does your app collect or share user data?                                │
│    YES (Diagnostics, Crash Logs & Advertising ID)                           │
│                                                                             │
│  • DATA TYPES COLLECTED:                                                    │
│    1. Device or other IDs ➔ Advertising ID (Google Mobile Ads / AdMob SDK) │
│       • Purpose: Advertising or Marketing, Analytics                        │
│       • Ephemeral: No | Optional: Yes (User can reset/delete in OS settings)│
│    2. App Info and Performance ➔ Crash Logs (Firebase Crashlytics, Sentry)  │
│       • Purpose: App functionality, Analytics, Diagnostics                  │
│    3. App Info and Performance ➔ Diagnostics (Firebase Performance)         │
│       • Purpose: App functionality, Performance monitoring                  │
│                                                                             │
│  • DATA NOT COLLECTED (Answer "NO" to all):                                 │
│    ❌ Location (Precise or Approximate) ➔ NO                                 │
│    ❌ Personal Info (Name, Email, Phone, Address, User IDs) ➔ NO            │
│    ❌ Financial Info (Credit card, Bank account, Purchase history) ➔ NO     │
│    ❌ Photos / Videos (Managed locally via Android System Photo Picker) ➔ NO│
│    ❌ Contacts, SMS, Audio, Health / Fitness Data ➔ NO                      │
│                                                                             │
│  • DATA SHARING:                                                            │
│    • Shared with 3rd parties: YES (Advertising ID with Google AdMob)        │
│    • Personal user records shared: NO (Local SQLite database stays on-device)│
│                                                                             │
│  • SECURITY PRACTICES:                                                      │
│    • Is data encrypted in transit? ➔ YES (All network calls use HTTPS/TLS)  │
│    • Can users request data deletion? ➔ YES (App data reset / local clear)  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Reviewer Instructions & Demo Notes

**Paste into the "App Access" reviewer notes in Google Play Console:**

> **To the Google Play Review Team:**  
> Milevora is an **offline-first vehicle maintenance and expense tracking application**.  
> * **No Account Required:** The app does not require a login, phone number, or cloud account. Launching the app opens directly to the Dashboard.
> * **Sample Vehicle:** Tap "Add Vehicle", select a Make/Model, and set a current odometer to explore maintenance reminders, fuel tracking, and mileage calculations.
> * **Subscriptions:** Google Play Billing is integrated with test SKUs (`autominder_pro_monthly`, `autominder_pro_yearly`, `autominder_pro_lifetime`). Use standard Google Play License Tester accounts.

---

## 3. 5-Screen Store Screenshot Copy Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     GOOGLE PLAY STORE SCREENSHOT COPY                       │
│                                                                             │
│  SCREEN 1 (Hero / Dashboard):                                               │
│  "MILEVORA: YOUR CAR. REMEMBERED."                                          │
│  Subhead: Smart maintenance reminders calibrated by date and odometer.      │
│                                                                             │
│  SCREEN 2 (Garage / Multi-Vehicle):                                         │
│  "ALL VEHICLES IN ONE PLACE."                                               │
│  Subhead: Track maintenance schedules across your entire household garage.  │
│                                                                             │
│  SCREEN 3 (Vehicle Confidence / Wear):                                      │
│  "KNOW WHAT NEEDS ATTENTION."                                               │
│  Subhead: Daily driving pace analytics and proactive service countdowns.    │
│                                                                             │
│  SCREEN 4 (Fuel & Mileage Log):                                             │
│  "TRACK FUEL & MILEAGE EFFICIENCY."                                         │
│  Subhead: 15-second fast-path fill-ups, MPG trends, and cost-per-mile.      │
│                                                                             │
│  SCREEN 5 (Privacy & Resale Passport):                                      │
│  "100% PRIVATE. EXPORT RESALE PASSPORT."                                    │
│  Subhead: On-device data vault with certified PDF service history export.   │
└─────────────────────────────────────────────────────────────────────────────┘
```
