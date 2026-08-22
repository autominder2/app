# AutoMinder — US Google Play Launch Scorecard
Fill baselines from the first 20-tester beta. Do NOT invent target numbers before then.

## 1. Product quality (gate to every stage)
| Check | Status | Evidence |
|---|---|---|
| Release gate green (assemble, unit, lint, release compile) | ☐ | gate_result.txt |
| Migration tests pass on device | ☐ | connectedDebugAndroidTest |
| Crash-free users ≥ 99.5% in beta | ☐ | Crashlytics |
| ANR below Play bad-behavior threshold (0.47%) | ☐ | Play vitals |
| Cold start p50/p95 measured on mid-range device (record, then improve) | ☐ | Macrobenchmark |
| A11y pass: TalkBack, 200% font, 48dp, dark, color-independent status | ☐ | manual matrix |
| Android 16: edge-to-edge, predictive back, insets verified | ☐ | manual |
| Billing: none live OR PBL migration done before Aug 31 2026 | ☐ | build deps |

## 2. Store conversion
| Check | Status |
|---|---|
| Title ≤30 chars, natural: candidate "AutoMinder: Car Care" | ☐ |
| Short desc candidate: "Understand repair quotes, prepare for service, and track what your car needs." | ☐ |
| No "#1/best/save $X/testimonials/competitor names" anywhere in metadata | ☐ |
| 6 screenshots, each = real UI + one outcome (verdict, quote review, mechanic questions, invoice diff, evidence history, privacy/export) | ☐ |
| Listing experiment A/B running before judgment calls | ☐ |
| Data-safety form matches actual behavior (local-first, what analytics collects) | ☐ |

## 3. Retention & engagement (set targets after beta baseline)
| Metric | Baseline | Target |
|---|---|---|
| Install → onboarding complete | | |
| Time to first verdict (goal direction: <90s) | | |
| Seeded-plan interaction, first session | | |
| Notification delivered → action | | |
| Quote started → decision saved | | |
| D1 / D7 / D30 | | |
| Week-4 users who acted on ≥1 reminder | | |

## 4. Ratings & support
| Check | Status |
|---|---|
| In-app review prompt ONLY after: saved decision, reconciled invoice, or 5th completed reminder | ☐ |
| Never after onboarding, error, paywall, safety warning; never incentivized | ☐ |
| Reviews + crashes monitored daily during launch weeks | ☐ |
| Complaint classification sheet (bug / confusion / expectation / device) | ☐ |
| Weekly small-fix release cadence during rollout | ☐ |

## 5. Policy & privacy
| Check | Status |
|---|---|
| Analytics contain NO raw quotes, VINs, plates, receipts, free text | ☐ |
| EXIF GPS stripped from stored images (when receipts ship) | ☐ |
| Export + delete flows work and are documented in listing | ☐ |
| Ads absent from safety/reminder/quote/decision/evidence surfaces | ☐ |

## 6. Staged rollout
| Stage | Gate to advance |
|---|---|
| Internal QA (owner + emulators) | Scorecard §1 all green |
| 20 US testers, 3+ states | ≥70% complete onboarding unaided; no P0 bugs 7 days |
| 100-person closed beta (varied vehicles/states/devices) | Crash-free ≥99.5%, D7 baseline recorded |
| Limited US production (staged %) | Vitals green 14 days, review rate stable |
| National US rollout | All above + support load sustainable |
| Canada | USD/CAD, km toggle, tax wording, price-source behavior re-verified |
| UK / Australia | Full localization workstream (MOT/rego vocab, GBP/AUD, local data) — not translation |

iOS checkpoint: revisit only after US Android shows retention + paid demand.
