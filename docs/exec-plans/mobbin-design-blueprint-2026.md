# AutoMinder 2026 Design Blueprint — Mobbin-Informed

Status: **RESEARCH / ASK PASS. No production code was modified.**

> **Authority note (2026-08-16).** This blueprint **supersedes** the
> "Autominder Technical Handoff: Production-Ready Jetpack Compose
> Implementation" document for all conflicting guidance. That document is
> retained only where it does not conflict — specifically its M3 tonal-depth
> guidance (dark-mode depth via `surfaceContainer*` tokens, not shadows),
> JetBrains Mono for numerics, and the `StickyHeader` / `derivedStateOf` /
> `Modifier.graphicsLayer` performance notes, all of which agree with §11
> and §14 here.
>
> It is **overridden** on: the `CircularHealthIndicator` / health-ring
> (fabricated data — see §15 and the first issue in §17), the vehicle PNG
> render with `#7AB4FF` radial glow (violates the no-photo requirement and
> the no-glow anti-pattern in §3), `OCRReceiptScanner` via ML Kit + CameraX
> (receipt OCR is deferred to v1.1+ by CLAUDE.md and would add a CAMERA
> permission plus two dependencies), `SpendTrendChart` via the Vico library
> (Fuel Intelligence is v1.1+; Vico is absent from `libs.versions.toml`),
> the "Home / Garage / History / Settings" navigation (locked as Home /
> Vehicles / Records / Settings; CLAUDE.md forbids renaming Records), and
> `NetworkBoundResource` (AutoMinder is offline-first and accountless —
> there is no network source to reconcile against).
>
> **A caution about that document's sourcing.** It attributes to Mobbin a
> "Bento Grid" automotive layout pattern and a "'Solar White'
> High-Contrast Light Theme". The live Mobbin research in §1/§2 found the
> opposite: Tesla's [service
> history](https://mobbin.com/screens/a1ea4590-a32c-4634-94b0-5dd3e669e2af)
> uses **no cards at all**, and BMW's [NEXT
> SERVICES](https://mobbin.com/screens/fbbd81ca-7614-4e75-8d50-94907c1cc870)
> — the most relevant screen found — is a flat list. Its Mobbin
> attributions did not survive verification, so it should not be treated as
> a research artifact.

## READ THIS FIRST — evidence provenance

This blueprint was assembled in two passes, and it matters which is which:

- **Pass 1 (repository + Stitch):** §3–§11 and §13–§17 were written against
  the actual AutoMinder repository — theme files, all ~30 components,
  screens, `strings.xml`, and governance docs read directly, not assumed —
  plus an existing in-house Stitch exploration (see below). During this pass
  the `mobbin` connector was **not yet authorized**, so §1/§2 were left
  explicitly BLOCKED rather than filled with invented citations.
- **Pass 2 (Mobbin, after authorization):** the connector was authorized and
  §1/§2 were filled with **real** searches — every screen cited below was
  returned by a live `search_screens` call and visually examined as an inline
  image before being described. Nothing in §1/§2 is remembered or inferred
  from app knowledge; each carries its `mobbin_url`.

**Consequence worth stating plainly:** §4/§5 (the Home direction) were
*decided* in Pass 1 from repository evidence, then *tested* against Mobbin in
Pass 2. The Mobbin evidence corroborated the direction (see §2 — BMW's "NEXT
SERVICES" is structurally the same answer) rather than producing it. That is
a weaker claim than "derived from competitive research," and it is stated
this way deliberately.

Still open: §12's per-screen `MOBBIN EVIDENCE` fields carry the Pass-1
BLOCKED marker on screens whose specific pattern wasn't among the eight
searches run in Pass 2. §2 now covers Home, Records, Vehicles, Add Service,
Pro, and Onboarding-permission with real citations; Reminder Detail, Mileage,
Fuel, About, and Add/Edit Vehicle remain repository-grounded only.

### A second evidence source WAS used: an existing Stitch project

### A second evidence source WAS used: an existing Stitch project

Mid-pass, a prior in-house design exploration surfaced: Google Stitch
project `5215641449031779482`, containing 46 entries — AutoMinder-specific
mobile mockups, three "Autominder Case Study" desktop presentation decks,
and four markdown documents (`DESIGN.md`, "Autominder Production Audit",
"Autominder Technical Handoff Document", and a case-study document whose
Sync/Backup, Family-Access and "Production Ready" sections could not be
fetched under the "Production Handoff Documentation" title verbatim — see
below). This **was** used as real evidence: `list_screens` was called
against the full project, all four markdown documents were fetched and
read in full, and 11 representative screenshots were downloaded and
visually examined (not described from filename/title alone) — the three
Main Dashboard variants, Dashboard Active/Empty states, both "My Garage" /
"Premium Vehicle Dashboard" fleet-list mockups, the combined Service/Fuel
timeline ("Insights"), and three screens that turned out to directly
contradict AutoMinder's accountless/offline-first product law: Login
Screen Variant 1, Sync & Backup Center, and Family Access.

This Stitch evidence is folded into §2 (as "STITCH PRIOR-EXPLORATION
FINDINGS", since brand-vs-brand Mobbin evidence itself is still blocked),
§6, §15, and cited throughout §4/§5 where it directly informed the Home
direction decision. Citations use the format `Stitch project
5215641449031779482 — "<screen title>"` since these are private
per-project assets with no public URL equivalent to Mobbin's `mobbin_url`.

One fetch note for transparency: the document catalogued in `list_screens`
under the title **"Production Handoff Documentation"** returned, when
fetched, content describing itself as a UX case study (problem space,
personas "The Enthusiast"/"The Fleet Manager", "Production Ready" section)
rather than a literal handoff spec — its exact prose could not be
extracted verbatim through the fetch tool used (it returned a description
of the document rather than the document itself). This is stated plainly
rather than papered over: what's used below from that file is limited to
what was reliably confirmed, not invented to fill the gap.

---

## 1. MOBBIN EVIDENCE

**Platform constraint, stated up front:** Mobbin indexes **iOS and Web only** —
there is no Android corpus. Every reference below was studied for its
*principle* and is translated to Material 3 / Compose in §12's ANDROID
ADAPTATION fields. iOS chrome (sheet grabbers, top-right ✓/✕ confirm pairs,
swipe-back-only navigation, SF Symbols) is **not** carried over.

### Searches actually run (Pass 2, all `platform: ios`)

| # | Query | Mode | Returned |
|---|---|---|---|
| 1 | car maintenance reminder dashboard showing overdue service items | standard | Affirm, Life Reset, Breathwrk — *off-target, discarded* |
| 2 | Mercedes-Benz app vehicle status and garage home screen | deep | 4 × Mercedes-Benz |
| 3 | My BMW app vehicle status with service and inspection due details | deep | 4 × My BMW |
| 4 | Tesla app vehicle home screen with controls and service history | deep | 4 × Tesla |
| 5 | service records history timeline grouped by date with cost | deep | Gojek, Setel, Rivian, GoPay |
| 6 | subscription paywall comparing monthly yearly and lifetime plans with restore purchase | deep | one year, Moonly, Liven, Finch |
| 7 | enable notifications permission education screen explaining why reminders matter | deep | Tempo, Revolut Business, DICK'S, Peanut |
| 8 | empty state with icon short explanation and single call to action button | deep | Swiggy, Shangri-La Circle, Vivid, Qonto |
| 9 | switch between multiple saved vehicles or cars in a garage list | deep | My BMW, DoorDash Dasher, Amazon Alexa, Transit |
| 10 | log a new entry form with date field amount field and optional notes | deep | Commons, Apple Health, Klarna, Withings Health Mate |

**Honest note on coverage:** this is 10 searches, not the ~30 the original
brief budgeted. Search 1 is recorded as a *failure* — a natural-language
query for "car maintenance dashboard" returned a BNPL app, a habit tracker,
and a breathing app, because Mobbin has no maintenance-app category to match.
Brand-name queries and pattern-shaped queries both work well; category
queries phrased around AutoMinder's domain do not. Categories from the brief
**not** covered by a run search: bottom navigation, bottom sheets,
data-heavy detail screens, status/warning badge systems, and all `web`-platform
and `search_flows` multi-step research. Those remain unresearched — not
silently folded into recommendations.

### References examined, tagged and classified

Every screen below was returned as an inline image and read visually.

**Mercedes-Benz**
- [Car status tile grid](https://mobbin.com/screens/56a361b4-84db-48cf-a4f4-58e398914361) — *IA / VISUAL.* 2-col tile grid; each tile = label + value ("Tyre pressure / 215–250 kPa"). The one abnormal tile reads "Vehicle status / **Open**" in red; every normal tile is neutral gray. **ADOPT** — the quiet-normal/loud-exception rule, exactly what §8 demands.
- [Vehicle status detail](https://mobbin.com/screens/e61abea0-c042-4d7f-ba46-91feec27f290) — *VISUAL / CONTENT.* Flat label→value list: "Windows **One window open**" red with chevron; "Doors Closed", "Bonnet Closed", "Parking brake Engaged" all quiet gray. Only actionable rows get a chevron. **ADOPT.**
- [My Garage](https://mobbin.com/screens/b0532ba1-f006-4796-954a-b07bb64f6f02) — *IA.* Vehicle carousel + "✓ active" marker, then vehicle name, a compact metadata row (🔒 Locked · ⛽ 53%), then plain nav rows. **ADAPT** — carousel→list for Android; keep the metadata-row idea.
- [Home with hero](https://mobbin.com/screens/c246430f-e5af-42dc-9fb3-3ae72f2931dc) — *VISUAL / BRAND-SPECIFIC.* Dark hero, 3D car render, fuel bar, **"Updated on 10/12/2024 – 16:15"** staleness stamp, then an attention chip "⚠ Vehicle unlocked +1 more ›". **REFERENCE ONLY** for the render (AutoMinder has no renders — §"no-photo requirement"); **ADOPT** the staleness timestamp and the "+N more" attention collapse.

**My BMW**
- [NEXT SERVICES list](https://mobbin.com/screens/fbbd81ca-7614-4e75-8d50-94907c1cc870) — *the single most relevant screen found.* Flat rows, no cards: "Vehicle inspection / **Overdue since November 2024**" (red) above "Engine oil / Due in October 2025 **or** 11,000 km", "Brake fluid / Due in June 2026", "Rear brake pad change / Due in 3,400 km" (all gray). **ADOPT wholesale** — dual-basis date-**or**-distance phrasing is precisely AutoMinder's reminder model, sorted by urgency, urgency carried by *text colour + icon tint only* — no chips, no filled badges, no radius change. This independently validates §8's shape rule.
- [Vehicle issues grouping](https://mobbin.com/screens/d89a443b-889e-4a24-a206-0d633bdd2069) — *IA.* A "VEHICLE ISSUES / ATTENTION REQUIRED" block sits **above** normal status sections. **ADOPT** — attention gets its own region at top, not colour-coded rows scattered through a list.
- [Service detail](https://mobbin.com/screens/f0ca2791-8aeb-40f4-a71e-876e525838eb) — *INTERACTION.* Icon, name, one plain-language explainer, the red status line, a large empty middle, one full-width action anchored at the bottom. **ADOPT** — one primary action per screen; whitespace is allowed.
- [Vehicle status w/ services](https://mobbin.com/screens/eff7caad-efbf-47f6-ae14-6215c3f0b242) — *CONTENT.* Mileage row carries its own provenance: "Updated from vehicle 11/12/2024 2:30 pm". **ADAPT** — AutoMinder's equivalent is "you last entered", never "from vehicle" (no telemetry).
- [Garage](https://mobbin.com/screens/6624d0e9-337c-43f6-b254-7d5e2fd5c287) — *INTERACTION / ANDROID-CONCERN.* Swipe-to-reveal red "Remove". **REJECT as sole affordance** — destructive-by-swipe with no visible alternative fails TalkBack and switch access; Android needs an explicit menu path.

**Tesla**
- [Service History](https://mobbin.com/screens/a1ea4590-a32c-4634-94b0-5dd3e669e2af) — *IA / VISUAL.* Flat, card-less rows: glyph + title + right-aligned date + location subtitle + status word ("Completed", "Resolved Remotely"). Spacing alone does the grouping. **ADOPT** — the cleanest records pattern found; directly applicable to Records.
- [Service hub](https://mobbin.com/screens/863d9293-508b-4d6c-b102-33567304c660) — *INTERACTION.* One restrained full-width primary ("Request Service"), then a plain icon+label+chevron list. **ADOPT** the restraint; note the primary is *tonal*, not a shouting accent fill.
- [Controls](https://mobbin.com/screens/4192225f-7091-4300-95e6-d9cf953072e1) — *VISUAL.* Near-black, zero cards, values placed spatially on a car diagram, each with a "2 hours ago" staleness note; 4 icon+label quick actions across the bottom. **REFERENCE ONLY** — the spatial diagram implies telemetry AutoMinder must not imply; **ADOPT** the per-value staleness note.
- [Charging detail](https://mobbin.com/screens/64059d3f-9316-4844-bc80-9607fed6a85d) — *CONTENT.* Settings-style rows carry a summary value *under* the label ("Climate / Interior 29 °C"). **ADOPT** for §7's Settings row (value-under-label variant).

**Records / history (cross-category)**
- [Rivian — Past invoices](https://mobbin.com/screens/dc963124-a006-4c13-9dcc-076f01f8aaf5) — *CONTENT.* The screen explains itself: "These signed documents are your records of the service you've had done." **ADOPT** — a one-line purpose statement on Records.
- [Setel — Payment history](https://mobbin.com/screens/6542aa77-a643-4969-a9f4-515f1adefd65) — *IA.* Month header in small-caps gray outside the rows; flat rows; range control ("Last 180 days ∨") + Filter. **ADOPT** the sticky month header + range control.
- [GoPay — Transaction history](https://mobbin.com/screens/8eb2537b-01eb-4788-81c2-ad8ee59fc42a) — *VISUAL.* A voided row renders struck-through with "Cancelled & refunded". **ADAPT** — precedent for showing a *corrected/deleted* service record without erasing history.
- [Gojek — Transaction history](https://mobbin.com/screens/cd60a87a-81cd-49d3-aca0-bf006ac0c8c2) — *ANDROID-CONCERN.* A promo banner sits inline in the list between date groups. **ADAPT** — this is the one monetisation placement consistent with §"AdMob visual policy": passive, in history, never in a trust-critical flow.

**Paywall**
- [one year](https://mobbin.com/screens/df0d769a-48b0-46b0-beb4-9f8225d4ce5a) — *CONTENT / VISUAL.* Two plain full-width plan buttons ("S$ 3.98 monthly" / "S$ 59.98 lifetime"), an "already purchased?" affordance at top, and "restore · terms · privacy" in the footer. No countdown, no fake scarcity. **ADOPT** the restraint and the always-visible restore.
- [Moonly](https://mobbin.com/screens/b2a7f4f6-e03e-4875-97ec-79c299a244c7) — *CONTENT.* Every plan normalised to a common unit ("SGD 0.60 **per week**") so plans are actually comparable. **ADOPT** — but derive strictly from Play-supplied prices, never a hardcoded arithmetic claim.
- [Finch](https://mobbin.com/screens/981c04ab-3348-492a-b506-7e456db8b1ae) — *CONTENT.* "Restore purchase" and "Cancel anytime in App Store" as plain text under the plans. **ADOPT** (Play equivalent). The struck-through "was $179.76" is **REJECT** unless Play returns a real reference price.
- [Liven](https://mobbin.com/screens/a612cffd-939d-471f-9b89-bf734058ab1a) — *REJECT.* Leads with a 5-star review quote as social proof. AutoMinder has no verified reviews to cite; §"paywall copy must be based on verified entitlements only" forbids it. Its "$99.99, one-time payment" phrasing for lifetime is however **ADOPT**-worthy plain language.

**Notification permission education**
- [Tempo](https://mobbin.com/screens/28a98c51-2efc-4489-8d14-2f31cd4db383) — *CONTENT — best in class.* States exactly when notifications fire ("one hour before, and another 15 minutes before"), previews a real notification, then triggers the OS dialog, with Skip available. **ADOPT** — AutoMinder's analogue: "We'll remind you when a service is due — and again if it becomes overdue."
- [Revolut Business](https://mobbin.com/screens/3bde8924-ee2e-407d-9781-e1418ded6b74) — *INTERACTION.* Primary "Enable push notifications" + secondary "Not now", both real buttons. **ADOPT** — never dead-end; "Not now" must be equally reachable.
- [DICK'S](https://mobbin.com/screens/9b556c34-d8cd-4126-98f1-d9ff47c0ff05) — *VISUAL.* Realistic notification preview + three icon+benefit rows. **ADAPT.**
- [Peanut](https://mobbin.com/screens/7106f214-002f-41b0-bb93-7e8fd213ae49) — *REJECT.* "You're **4x** more likely to…" — an unsubstantiated statistic. AutoMinder cannot make an equivalent claim truthfully.

**Empty states**
- [Vivid](https://mobbin.com/screens/f2a5e37f-17b2-43db-9544-f0491aea6977) — one sentence ("Your statement will be stored here"), no illustration, primary CTA anchored bottom. **ADOPT as the default** — cheapest, most honest, scales to 200% font.
- [Qonto](https://mobbin.com/screens/059fd5ca-4900-49b5-a204-977a213c4cb7) — illustration + headline + explanation + CTA. **ADAPT** — reserve illustration for first-run only.
- [Shangri-La Circle](https://mobbin.com/screens/f8748b39-506c-4727-82b9-9067049515f3) — muted glyph + "No invoice header for now" + bottom CTA. **ADOPT** the muted-glyph weight (never a loud accent-coloured illustration).
- [Swiggy](https://mobbin.com/screens/a7da275e-b167-4c6a-814a-fc7b5b1c81fe) — **REFERENCE ONLY**; two-line copy is good, illustration is heavier than AutoMinder's register.

**Multi-vehicle selection**
- [Amazon Alexa — vehicle list](https://mobbin.com/screens/ce3bce8b-6366-47d8-9a9b-df4a704dfbc2) — *the no-photo proof point.* A list of five vehicles rendered as a **generic car glyph + "2022 BMW 330e"** text, and it reads perfectly well. **ADOPT** — direct evidence that AutoMinder's no-photo requirement costs nothing when year+make+model carries identity.
- [DoorDash Dasher](https://mobbin.com/screens/099ab600-0e0c-4b9f-8ed8-3fedb249bd89) — *IA.* Type-glyph + label ("Car", "Motorcycle") + explicit trailing delete icon; "Add new vehicle" anchored bottom. **ADOPT** the *explicit* delete affordance (contrast with BMW's swipe-only, rejected above).
- [Transit](https://mobbin.com/screens/8601e019-b60f-4210-9ff5-70551b4e9ce1) — **REJECT.** Brand-coloured filled rows make every item look like a button — precisely the failure §8 prohibits.

**Add / log entry forms**
- [Apple Health](https://mobbin.com/screens/189a93c5-8cd4-483b-bb6f-488902425ef5) — *INTERACTION — best in class for AutoMinder's ~15 s target.* Date and Time arrive **prefilled** with sensible defaults; only the one genuinely unknown value needs input, and the numeric keypad is already up. **ADOPT** — this is the pattern that makes "prefill everything known" concrete.
- [Withings](https://mobbin.com/screens/626918b3-c3f9-46a2-8785-bf5e957bc3a5) — *INTERACTION.* Inline wheel for the value; date/time reduced to tappable chips with defaults. **ADAPT** — Android equivalent is a text field + Material date picker, not a wheel.
- [Klarna](https://mobbin.com/screens/0e69592a-4b35-48a8-b975-7e5737af4887) — *IA.* Bottom sheet, rows of icon+label+value, calendar expands **inline** rather than pushing a new screen, single "Save" anchored bottom. **ADOPT** the inline-expansion idea.
- [Commons](https://mobbin.com/screens/d29cd54b-b9cd-4629-94de-a526d556b26d) — **REFERENCE ONLY.** Every field individually boxed with caps micro-labels; heavier and slower than the prefilled model above.

---

## 2. CROSS-BRAND FINDINGS

### Mercedes vs BMW vs Tesla — what actually separates them

All three were examined visually (§1). They are not three flavours of the
same design; they resolve the same problem three different ways, and only
one of those ways is available to AutoMinder.

| | Mercedes-Benz | My BMW | Tesla |
|---|---|---|---|
| **Home organising idea** | Tile grid of *state* | Grouped status sections, attention first | Vehicle object + direct manipulation |
| **How urgency reads** | One red value in a neutral grid | Red text + red icon, in a dedicated "ATTENTION REQUIRED" block | Barely present — Tesla has little to be overdue about |
| **Container strategy** | Cards everywhere | Almost no cards — flat rows + section headers | No cards at all; spacing only |
| **Maintenance depth** | Shallow (a "Services" entry point) | **Deep — a real due-list with date-or-distance** | Shallow (service = a request form) |
| **Relevance to AutoMinder** | Moderate | **Highest** | High for restraint, low for structure |

**The finding that matters:** BMW is the only one of the three that treats
maintenance as a first-class list rather than a doorway, and its [NEXT
SERVICES](https://mobbin.com/screens/fbbd81ca-7614-4e75-8d50-94907c1cc870)
screen expresses due-ness exactly the way AutoMinder's domain model does —
*"Due in October 2025 or 11,000 km"*, one overdue item raised to red at the
top, everything else quiet. AutoMinder's whole product is that one screen,
expanded. Mercedes and Tesla are worth studying for **register** (tonal
restraint, staleness stamps, one primary action, generous whitespace) but
their information architecture assumes live telemetry AutoMinder does not
and must not have.

**The trap all three set, which AutoMinder must not fall into:** every one of
them can afford a vehicle render, a lock state, a tyre pressure, a battery
percentage — because a car is streaming them data. AutoMinder knows only what
its owner typed. Copying the *look* of these homes without the data behind it
is precisely how you end up fabricating a health score or a 0 km reading —
which is exactly what the Stitch exploration below did, and what §15 flags.

**Cross-category corroboration of the Home decision (§5):** the three
strongest non-automotive references — Tesla's flat [service
history](https://mobbin.com/screens/a1ea4590-a32c-4634-94b0-5dd3e669e2af),
BMW's attention-first
[grouping](https://mobbin.com/screens/d89a443b-889e-4a24-a206-0d633bdd2069),
and Apple Health's [prefilled
logging](https://mobbin.com/screens/189a93c5-8cd4-483b-bb6f-488902425ef5) —
all reach the same conclusion independently: **state the verdict in words,
put the exception on top, and never make the user re-enter what you already
know.** None of them use a score ring. That is corroboration for §5's
"sentence-first, remove the fabricated 0–100 score" direction, arrived at
before this research was run.

### STITCH PRIOR-EXPLORATION FINDINGS (real evidence — see banner above)

This is a different kind of cross-reference than the brief originally
asked for: not "what do competitor apps do," but "what did an earlier pass
at this exact app explore, and does it match or contradict the identity
CLAUDE.md and DESIGN_SYSTEM_2026.md later established?" The answer, after
reading all four docs and visually examining 11 screens, is **a genuine
split**: some screens are close cousins of what's actually shipped and
worth learning from; several others describe a materially different,
connected-car/account-based product that the real codebase has since
explicitly and deliberately rejected.

**Screens that align with (or improve on) the shipped product — ADOPT/ADAPT:**

- **["Dashboard - Active Status"](Stitch project 5215641449031779482 — "Dashboard - Active Status")**
  — structurally the closest Stitch mockup to the real Dashboard: a hero
  card, a single "Priority Action" card ("Synthetic Oil Change... Due
  Soon... 450 mi remaining... + Log Service") functioning exactly like
  `ProactiveAttentionCard`, then an "Upcoming" section (Tire Rotation,
  Registration Renewal — both real `ServiceType` values). **ADAPT**: the
  priority-action framing and upcoming list are good IA precedent, already
  effectively shipped; its copy ("System Normal," "Vehicle Health
  Optimal") is the banned phrase pattern and should not be adopted (INFORMATION
  ARCHITECTURE: adopt; CONTENT STRATEGY: reject).
- **["Insights" / Service History & Timeline](Stitch project 5215641449031779482 — "Service History & Timeline")**
  — sticky month-year headers, a connector line down the timeline, and
  rows carrying title/shop/cost/odometer/receipt-chip. This is materially
  the same shape as the shipped `RecordsTimelineCard`, and arguably better
  than the app's *current* hand-rolled `ServiceHistoryCard` in
  `ServiceHistoryScreen.kt` (§6/§7's consolidation target) because it
  keeps a receipt-attachment affordance visible inline. **ADAPT**: worth
  reusing the "receipt chip on the row" idea when `ServiceHistoryScreen`
  is migrated onto `RecordsTimelineCard` — the domain model already has
  `receiptPhotoUri` (per `CODEX_HANDOFF.md` §7), so a receipt-attached
  indicator is not fabricated, only under-surfaced today (VISUAL
  TREATMENT: adopt; the mockup's implied OCR text-extraction behind it is
  a separate, out-of-scope idea — see below).
- **["Premium Vehicle Dashboard" / "My Corolla"](Stitch project 5215641449031779482 — "Premium Vehicle Dashboard")**
  and **["My Garage"](Stitch project 5215641449031779482 — "My Garage")**
  — both use a photo-hero + stat-tile + quick-action layout genuinely
  close to `VehicleHeroCard`(Expanded) + `PremiumActionGrid`. The
  `+Fuel / +Service / +Odo` quick-action row **is** essentially
  `PremiumActionGrid`'s tile set. **ADAPT** the quick-action tile set and
  the "Monthly Summary" cost card (a real, non-fabricated Pro feature that
  already exists as `costByType`/`monthlySpending`); **REJECT** the
  "Insurance: 28 Days / Expiring" tile framed as a sensor-style gauge
  (insurance IS a real reminder type, but the gauge/dial visual treatment
  implies live telemetry it doesn't have — a status card, not a dial, is
  the honest equivalent already shipped as `StatusReminderCard`).

**Screens that describe a different, since-rejected product direction — REJECT, flag as out-of-scope:**

- **Fabricated sensor telemetry, all three ["Main Dashboard Variant 1/2/3"](Stitch project 5215641449031779482)**
  mockups and **"Premium Vehicle Dashboard"**/**"My Garage"**: "Tire PSI
  35/35," "Pressure 34," "Fuel 45%/Low," "Battery 98%," "Oil Life 87%/82%"
  rendered as live percentage dials. AutoMinder has no OBD-II/TPMS
  integration and never will in v1.0 — `CLAUDE.md`'s scope table says this
  explicitly: *"OBD-II gauges (oil life, tyre pressure, battery %) — No
  sensor data exists. A gauge would be fabricated."* VISUAL TREATMENT /
  BRAND-SPECIFIC ELEMENT: **REJECT outright**, not merely "adapt with
  caution" — this is the single clearest match between a documented
  AutoMinder anti-pattern and an actual mockup in the prior exploration.
- **"SYNCED" / "CLOUD SYNC ACTIVE" badges** on "Main Dashboard Variant 2"
  and **3**, and the entire **["Sync & Backup Center"](Stitch project 5215641449031779482 — "Sync & Backup Center")**
  screen (auto-backup timestamp, "Manual Backup," "Export JSON," a
  2.4GB/5GB cloud-storage meter with "Upgrade," and a "Cloud History" list
  naming specific devices — "Dad's iPhone," "Mom's iPad" — each with a
  Restore button): AutoMinder is offline-first with no backend.
  `CLAUDE.md`'s scope table: *"Cloud sync / 'Synced' badges — Offline-
  first. There is no server to sync with."* **REJECT.** Flagged in §15 as
  a design risk, not adopted anywhere in this blueprint.
- **["Login Screen Variant 1"](Stitch project 5215641449031779482 — "Login Screen Variant 1")**
  (and by extension variants 2–3, titles-only reviewed): full email/
  password + Google/Apple SSO + "Create Account" flow, "Sign in to access
  your digital garage." AutoMinder has no accounts. `CLAUDE.md`: *"Login /
  account / security screens — The app has no accounts."* **REJECT.**
- **["Family Access"](Stitch project 5215641449031779482 — "Family Access")**:
  multi-user roles (Owner/Admin/"Restricted"), per-driver "Curfew" and
  "Speed Limit" fields implying GPS/geofencing telemetry, invite-by-email
  flow. `CLAUDE.md`'s deferred-to-v1.1+ list names both **family sharing**
  and **GPS/geofencing** explicitly. **REJECT for v1.0**; flag as v1.1+
  exploration at most, and flag the speed-limit/curfew concept
  specifically as a parental-monitoring feature that would need its own
  privacy/consent review before ever being built, independent of the
  timing question.
- **"Dashboard - Empty State"**: a full-bleed stock supercar photograph
  behind "Your Digital Garage is Ready," plus an "Explore as Guest →"
  link (implying the login flow above is the default path). This
  directly contradicts `docs/DESIGN_SYSTEM_2026.md` §11b's explicit rule:
  *"Never substitute: manufacturer press imagery, stock photography
  standing in for the user's car... The no-photo state is a required
  proof state... it is proven first, not last."* **REJECT** the stock-photo
  empty-state treatment; the shipped `EmptyState` component (icon + text +
  action, no photography) is the correct pattern and should not be
  replaced by this mockup's approach.
- **"Autominder Technical Handoff Document"**: recommends ML Kit
  `TextRecognition` + CameraX **OCR receipt scanning**, a Vico chart
  library (not the Canvas-based charts actually shipped), and **dynamic
  color for Android 12+** ("Midnight Cobalt as fallback"). Two of these
  directly conflict with current decisions: receipt OCR is explicitly
  listed in `CLAUDE.md` as deferred to v1.1+ ("STOP and confirm before
  touching"), and dynamic color is explicitly rejected in
  `AutoMinderTheme.kt`'s own doc comment (*"Wallpaper-driven dynamic
  colour is deliberately absent... a system-colour opt-in is a post-launch
  appearance setting, not a default"*). **REJECT** both for this pass;
  the Vico-vs-custom-Canvas-chart question is lower stakes (§6 already
  deferred the charts to a follow-up performance review) but should not be
  read as settled in Vico's favor by this old document.
- **The "Autominder Production Audit" document**, independently of any
  screenshot, is worth taking seriously on one specific point: it names
  **"Consolidate (Reconcile health score logic)"** as a P0 action item for
  the Main Dashboard, before this research pass ever inspected the actual
  Kotlin source. That is an independent, prior confirmation of the exact
  same finding this document reaches from reading `FleetHealthScore.kt`,
  `DashboardScreen.kt`, and `VehicleDetailScreen.kt` directly in §6/§15:
  **three different health-score formulas exist in this codebase for one
  concept, and they've never been reconciled.** Two independent passes
  reaching the same conclusion by different methods (a prior design audit
  vs. this session's direct code read) is stronger evidence than either
  alone — this raises the priority of Implementation Queue item 3, not
  just item 1.

**Net effect on this blueprint:** none of the rejected screens change any
recommendation already made in §3–§17 — if anything, seeing the rejected
direction in concrete pixel form *strengthens* the case for Option A in
§4/§5 (no fabricated ring, no photo-dependent hero) and sharpens exactly
which future feature requests (family sharing, cloud sync, OCR, accounts)
need to be recognized and declined at the design stage, not just the
engineering stage, if they resurface.

---

## 3. AUTOMINDER BRAND SYSTEM (repository-grounded)

### Personality

Confirmed from `docs/DESIGN_SYSTEM_2026.md` §1 and `CLAUDE.md`'s Product
Experience Law: **a calm, precise, private co-pilot — never an alarm clock,
never a dashboard of gauges, never a connected-car product.** The one rule
the whole system serves: *the accent color is the healthy state*. Cobalt
means "this is fine," amber means "soon," red means "now" — nothing else in
the interface may be a saturated hue, so when a color speaks, it means
something.

Voice, from the same doc: short sentences, no jargon, no exclamation marks.
Confirmed in shipped strings — `"Nothing needs attention right now."`,
`"Add your first vehicle to start tracking maintenance."`,
`"We'll set up your maintenance reminders automatically."` None of the 600
strings in `strings.xml` read as hype copy.

### Principles (evidenced, not aspirational)

1. **Truthful absence over fabricated data.** `vehicle_list_mileage_not_added`
   ("Mileage not added") instead of "0 km" — implemented in
   `VehicleListScreen.kt:182` with an explicit code comment explaining the
   `Int` column can't distinguish "never entered" from a real zero, so the
   UI compensates. This is the single clearest evidence the "truth before
   beauty" rule is real engineering practice, not a slogan.
2. **Status is never color alone.** Every status surface in
   `PremiumStatusStyle.kt` carries color + corner radius + rail (or no rail)
   + text label (`StatusChip`) — four channels, verified in
   `ProactiveAttentionCard.kt`, `StatusReminderCard.kt`, `VehicleHeroCard.kt`.
3. **One verdict sentence, not a number.** `HealthCockpitCard`'s own doc
   comment: *"Never renders a lone giant number — the headline is always
   scalable Text supplied by the caller."* The score ring is explicitly
   "demoted."
4. **No-photo is the default, not the edge case.** §11b of the design system
   doc states this as law, and it is backed by real code:
   `VehicleHeroCard`'s `VehicleAvatar` fallback (branded icon tile on
   `primaryContainer`), never stock photography or AI-generated cars.
5. **One primary CTA per screen**, optional fields folded behind
   progressive disclosure (`FormSectionCard`, the `showMore` pattern in
   `AddServiceScreen.kt`), sticky Save button pinned above the keyboard.
6. **Offline-first, no invented capability.** `docs/DESIGN_SYSTEM_2026.md` §12
   is an explicit "do not build" list: no OBD-II gauges, no "Synced" badges,
   no login/account UI, no vehicle valuation — because none of that data
   exists. This directly matches the task brief's anti-AI-mechanic /
   anti-dealer-clone constraint; the repo already enforces it.

### Anti-patterns (explicitly rejected, evidenced)

- Fake carbon fiber / neon / OBD gauges: never present in any `.kt` file,
  and explicitly reserved for v1.1+ under a "why there is no component" table.
- Generic "AI dashboard" card walls: `PremiumActionGrid`'s own doc comment
  calls itself "the cure for the equal-weight button wall."
- A single giant health number as the app's face: rejected in doc comments,
  **but not fully rejected in shipped code** — see the Design Risk in §15;
  `HealthCockpitCard.score` is still wired to a fabricated 0–100 composite
  on both Dashboard and Vehicle Detail, and `FleetHealthScore.kt` plus five
  `vehicle_health_*` strings use the literally banned phrase "Vehicle
  Health" / "Health score," which `CLAUDE.md`'s Product Experience Law
  forbids by name. This is real, present-tense drift, not a hypothetical —
  see §6 and §17.

---

## 4. HOME BEST-OF-3

All three options are built from AutoMinder's actual data model
(`VehicleWithStatus`, `ReminderWithStatus`, `ServiceStatus`) and actual
shipped components — no invented primitives.

### Option A — Sentence-First, No Ring (attention list, zero fabricated metrics)

`HealthCockpitCard` renders **only** the verdict sentence
(`"3 services need attention"` / `"All clear"`) and supporting line — the
`score`/`scoreDescription` parameters are passed `null` (already supported
by the component's signature; no new code needed). Below it: attention cards
(`ProactiveAttentionCard`), then the vehicle roster
(`VehicleHeroCard` × N). No numeric instrument anywhere above the fold.

- **Comprehension (~3s):** Fastest possible — one sentence, no number to
  interpret or distrust.
- **One vehicle:** Verdict + attention cards (if any) + one hero card. No
  wasted "fleet" framing for a single-car household (majority case for v1.0).
- **Multi-vehicle:** Verdict aggregates correctly; attention cards already
  carry `vehicle` context and a per-item CTA, so a 5-car household isn't
  penalized for lacking a fleet ring.
- **No-photo:** Unaffected — `VehicleHeroCard`'s avatar fallback already
  handles this; nothing here depends on photography.
- **No-reminder:** Vehicles with zero reminders don't appear in the
  attention list; `VehicleListScreen` already renders "No reminders yet" as
  a truthful concern line rather than a false "all clear" — Home should
  match that, not silently omit the caveat.
- **Overdue / due-soon:** `worstStatus` already drives `HealthCockpitCard`'s
  and `ProactiveAttentionCard`'s color/rail; unaffected by removing the ring.
- **Long copy:** Sentence-only headline scales cleanly at 2.0× font (already
  true — it's real `Text`, not baked into a fixed-size arc).
- **Accessibility:** Strictly better than B — one less thing to give a
  `contentDescription` to, one less color-coded arc that must also pass as
  a redundant channel.
- **Scalability:** Best of the three — no composite score to keep
  "meaningful" as the fleet grows past a handful of vehicles.
- **Implementation complexity:** **Lowest.** `score`/`scoreDescription` are
  already nullable parameters on `HealthCockpitCard`; this is a call-site
  change in `DashboardScreen.kt` and `VehicleDetailScreen.kt`, not new
  component work.

### Option B — Cockpit Sentence + Demoted Score Ring (current shipped state)

Exactly what `DashboardScreen.kt` and `VehicleDetailScreen.kt` ship today:
sentence-led `HealthCockpitCard` with a small arc + number
(`fleetScore` / `computeHealthScore`), then metric row (overdue/due-soon
counts), then attention cards, then vehicle roster.

- **Comprehension:** Still ~3s for the sentence, but the ring adds a second
  number a user must reconcile against the sentence ("3 need attention" next
  to "82") — mild redundant cognitive load, and the two numbers can feel
  like they disagree (a fleet score of 82 reads as "good," while "3 need
  attention" reads as "not good").
- **No-reminder / one-vehicle:** Ring still renders (defaults to 100 for
  zero reminders in `VehicleDetail`'s `computeHealthScore`, which returns a
  sentinel `-1` there — but Dashboard's inline `fleetScore` formula has no
  such guard and would show 100 for a brand-new fleet with no data at all,
  which is a **truthful-absence violation**: 100 implies "verified healthy,"
  not "no data yet.")
- **Accessibility:** Requires a `scoreDescription` merged into the card's
  content description — done, but it currently reads "Health score out of
  100: 82," which repeats the banned phrase to TalkBack users specifically
  (§6 flags this as a fix regardless of which Home direction is chosen).
- **Scalability:** A single composite score gets less meaningful as vehicle
  count grows (3 overdue on 1 car reads very differently from 3 overdue
  spread across 8 cars, but both can produce the same score).
- **Implementation complexity:** Zero — already shipped.

### Option C — Vehicle-First Accordion (no separate global attention section)

Each `VehicleHeroCard` becomes the row header for its own vehicle's
attention items, rendered inline/expanded rather than pooling all attention
items from every vehicle into one global list above the roster.

- **Comprehension:** Weaker for the "what do I need to do *today*, across
  everything I own" question — a fleet owner has to open or scan every
  vehicle to build their own mental attention list. This is the opposite of
  what CLAUDE.md's Product Experience Law asks for (health understandable
  ~3s).
- **One-vehicle case:** Degenerates into something close to Option A/B
  anyway (one card, expanded) — no benefit for the majority single-car case,
  only cost (accordion chrome for nothing to accord).
- **Multi-vehicle:** The only case where this has a real advantage —
  reduces duplicate vehicle-name context between a global attention item
  and its own roster card. But AutoMinder's median expected user is 1–2
  vehicles (personal, not fleet/rental), so this optimizes the minority case
  at the expense of the majority one.
- **Accessibility:** Worse — expand/collapse state adds a fourth semantic
  state per vehicle card, and TalkBack users lose the single flat "here's
  everything urgent" list order.
- **Scalability:** Best for very large fleets (10+ vehicles), worst for 1–2.
- **Implementation complexity:** **Highest** — new expand/collapse state
  machine per card, new merged-semantics contract, and it duplicates data
  already computed for the attention list (`ReminderWithStatus` per
  vehicle), so `DashboardViewModel` would need reshaping too.

---

## 5. FINAL HOME DIRECTION

**Option A — sentence-first, ring removed.** First viewport, top to bottom:

1. Top bar: app name + time-of-day greeting (unchanged — already correct
   and already localized via `dashboard_greeting_morning/afternoon/evening`).
2. **Verdict sentence** (`HealthCockpitCard`, `score = null`): "N services
   need attention" (plural-aware) or "All clear" — this is the single
   thing a user must read to answer "do I need to do anything right now."
   No numeric score anywhere in this card.
3. **Attention section** (only rendered when non-empty): `PremiumSectionHeader`
   + `ProactiveAttentionCard` per item, worst-first, each with vehicle
   context and a one-tap CTA into that vehicle.
4. **Vehicle roster**: `PremiumSectionHeader` + `VehicleHeroCard` per
   vehicle (Compact variant), each carrying its own status chip and
   one-line concern text — this stays even when the attention section is
   empty, because "which car is this and what's its mileage" is itself the
   secondary but real Home job.
5. FAB: unchanged — quick actions (Log Service / Add Fuel / Add Vehicle).

**Reasoning:** the brief's mandate — *"first viewport must prioritize
maintenance attention over analytics"* — is best satisfied by removing the
one remaining analytics-shaped element (a 0–100 composite score) from the
first viewport, not by rearranging sections that are already
attention-first. `InsightMetricRow` (overdue/due-soon counts) is a
borderline case: it's not fabricated (both numbers come directly from the
domain model, no composite math), so it can stay as a secondary confirmation
row directly under the verdict sentence — but it should never render before
the sentence, and it already doesn't.

This direction requires no new component. It is a **parameter change at two
call sites** (`DashboardScreen.kt`'s `HealthCockpitCard` call, and
`VehicleDetailScreen.kt`'s equivalent), a **string fix** for
`cd_health_score` to remove "Health score," and a **deletion** of the
unused `FleetHealthScore.kt` composable plus its five `vehicle_health_*`
banned-language strings (still referenced by `HealthSetupCard`'s icon
content-description string `cd_vehicle_health`, which needs the same
rename). See §17 for the bounded issue.

**Stitch cross-reference (§2):** all three "Main Dashboard" variants in the
prior Stitch exploration lead with exactly the pattern this direction
rejects — a large percentage ring (86%, "Health"/"System Normal") stacked
on fabricated OBD-II-style tiles (tire PSI, fuel %, battery %) that
AutoMinder has no sensor to back. Seeing that direction in concrete pixel
form is independent confirmation that Option A (sentence-first, ring
removed) is the right call, not just a theoretical preference — the
alternative was actually explored, by a different design pass, and it
leads straight into the anti-pattern CLAUDE.md and DESIGN_SYSTEM_2026.md
were later written to rule out.

---

## 6. DESIGN-SYSTEM AUDIT

Every file actually found under `ui/components/` and `ui/components/premium/`,
read directly (not inferred).

| Component | File | Verdict | Why |
|---|---|---|---|
| `StatusChip` | `StatusChip.kt` | **KEEP** | Correct 6-state mapping to domain `ServiceStatus`, uses `MaterialTheme.colorScheme` containers only, localized labels. |
| `VehicleHeroCard` | `premium/VehicleHeroCard.kt` | **KEEP** | Compact/Expanded variants, Precision Rail via `PremiumStatusStyle`, no-photo fallback, truthful year/odometer nullability. This is the canonical vehicle card — do not fork a second one. |
| `HealthCockpitCard` | `premium/HealthCockpitCard.kt` | **REFINE** | Structure (sentence-led, score optional) is correct and already supports Option A with `score = null`. Refine: stop passing a score at all from call sites (§5); nothing in the component itself needs to change. |
| `PremiumStatusStyle` | `premium/PremiumStatusStyle.kt` | **KEEP, flag for policy review** | Single source of truth for status → color/rail/corner is exactly right architecturally. The corner-radius-as-status-channel policy itself is the one open question in §8 — the component's *shape* is fine either way. |
| `ProactiveAttentionCard` | `premium/ProactiveAttentionCard.kt` | **KEEP** | Canonical "attention rail" item — four-channel status, single CTA, no photo dependency. |
| `StatusReminderCard` | `premium/StatusReminderCard.kt` | **KEEP** | Canonical reminder row for Vehicle Detail's Needs Attention / Upcoming lists. Correctly separates Done vs Snooze, mono timing text. |
| `RecordsTimelineCard` | `premium/RecordsTimelineCard.kt` | **KEEP** | Canonical record row — but currently unused by `ServiceHistoryScreen.kt`, which hand-rolls `ServiceHistoryCard` locally instead (see below). |
| `PremiumActionGrid` / `PremiumAction` | `premium/PremiumActionGrid.kt` | **KEEP** | Canonical quick-action tile family, already used correctly on Vehicle Detail. |
| `InsightMetricCard` / `InsightMetricRow` | `premium/InsightMetricCard.kt` | **KEEP** | Canonical metric display; correctly caller-formatted, no internal formatting logic. |
| `PremiumSectionHeader` | `premium/PremiumSectionHeader.kt` | **KEEP** | Canonical section header with count badge + optional action; heading semantics present. |
| `FormSectionCard` | `premium/FormSectionCard.kt` | **KEEP** | Canonical form-section wrapper — but not actually used by `AddServiceScreen.kt` (which hand-rolls its own "What was done?" / optional-details disclosure instead of wrapping it in `FormSectionCard`). Worth reconciling: either adopt `FormSectionCard` in `AddServiceScreen`, or accept the divergence and document why forms don't use it. |
| `PremiumPaywallPlanCard` + `PremiumPriceDisplay` | `premium/*.kt` | **KEEP** | Correctly closed price state machine (Loading/Available/Unavailable, never a raw nullable String) — this is exactly the pattern the Billing migration in `CODEX_HANDOFF.md` needs to keep working under PBL 9. |
| `StatusChip` reuse in `HealthCockpitCard`/`StatusReminderCard`/`ProactiveAttentionCard`/`ReminderDetailSheet` | n/a | **KEEP** | Single status-chip implementation reused everywhere — no forked "badge" variants found. |
| `EmptyState` | `EmptyState.kt` | **KEEP** | Icon + title + subtitle + optional hint + optional action — matches the design system's "illustration + one sentence + one action" state-matrix law (no illustration asset exists; icon substitutes, which is acceptable given no illustration system exists yet). |
| `ErrorState` | `ErrorState.kt` | **REFINE** | Functionally correct (message + retry) but its own icon/title are hardcoded to `Icons.Default.Info` and `R.string.error_title` regardless of severity — fine for v1.0's uniform error handling, but note it never varies visual weight for a data-loss-risk error vs a transient network error. Low priority. |
| `LoadingState` | `LoadingState.kt` | **REPLACE with skeletons where a skeleton exists** | Bare `CircularProgressIndicator` — the design system explicitly bans this ("Never a bare spinner") in favor of `DashboardSkeleton`/`ListSkeleton`. It's still used in a few places (e.g., `ServiceHistoryScreen` imports it though the actual state machine there uses `ListSkeleton` — worth a final grep pass to confirm `LoadingState` has zero remaining call sites and can be deleted). |
| `Skeleton.kt` (`shimmer`, `SkeletonBar`, `DashboardSkeleton`, `VehicleCardSkeleton`, `ListSkeleton`) | `Skeleton.kt` | **KEEP** | Exactly matches the documented skeleton spec (opacity-pulse in spirit, though the actual implementation is a translating gradient — see conflict below). |
| **Conflict:** Skeleton implementation vs its own doc | `Skeleton.kt` vs `DESIGN_SYSTEM_2026.md` §9 | **REFINE — real drift** | The design doc is explicit: *"No shimmer sweep. A translating gradient costs frames... use an opacity pulse 0.40→0.70."* The shipped `Modifier.shimmer()` **is** a translating linear-gradient sweep (`Brush.linearGradient` animated via `startX`), the exact pattern the doc says not to use. It does correctly collapse to a static tint under `Motion.reduceMotion`. This is a real, fixable conflict — not a hypothetical — see §15. |
| `BottomNavBar` | `BottomNavBar.kt` | **KEEP** | Correct 4-tab, type-safe route matching, no center FAB (matches the design doc's explicit rejection of a nav-bar FAB). |
| `ServiceChoicePicker` | `ServiceChoicePicker.kt` | **KEEP** | Excellent recent/common/all-services fast path with capacity math; border-width as a non-color selection channel is a genuinely good a11y pattern worth generalizing (see §7). |
| `ServiceTypeGrid` | `ServiceTypeGrid.kt` | **CONSOLIDATE** | A second, width-driven grid variant of the same selection concept as `ServiceChoicePicker`'s `ChoiceFlow`. Confirm which screens still use the plain grid vs the picker; if `ServiceChoicePicker` has fully superseded it (its own doc comment suggests it's the newer, smarter replacement), delete the grid rather than maintaining two selection idioms. |
| `ProPaywall` | `ProPaywall.kt` | **KEEP** | Correctly conversion-first (selectable plan cards, one Continue CTA, restore as quiet text action) — matches "canonical paywall plan" needs in §7. |
| `ProFeatureGate` | `ProFeatureGate.kt` | **KEEP** | Blur + lock overlay pattern is a reasonable, honest "this is Pro" treatment — doesn't fake data, just obscures it. |
| `FleetHealthScore` | `FleetHealthScore.kt` | **DELETE** | Dead-language component: computes its own separate 0–100 score (different formula from `VehicleDetailScreen`'s `computeHealthScore` and Dashboard's inline `fleetScore` — **three different scoring formulas exist in the codebase for the same concept**), uses the literally banned `vehicle_health_title`/`great`/`good`/`fair`/`needs_attention` strings, and appears to be dead code: `DashboardScreen.kt` imports it but the actual `DashboardContent` composable never calls it. Confirm zero call sites, then delete the component and its five strings, or migrate any real call site to `HealthCockpitCard`. |
| `QuickMileageSheet` | `QuickMileageSheet.kt` | **KEEP** | Correct increment-chip pattern (+10/+50/+100/+500), validates against regression below current odometer. |
| `ReminderDetailSheet` | `ReminderDetailSheet.kt` | **KEEP** | Canonical "consumer-grade issue detail" (severity → timing → what it means → can it wait → actions) — matches the FIXD-style pattern the codebase's own comments cite as inspiration; good instinct, no Mobbin verification possible this pass. |
| `SwipeToDeleteContainer` | `SwipeToDeleteContainer.kt` | **KEEP** | Correct non-visual-dismiss pattern (list-driven removal via Flow re-emit, not the swipe itself) avoids the ghost-row bug class. |
| `DiscardChangesDialog` | `DiscardChangesDialog.kt` | **KEEP** | Simple, correct, reused across every form screen. |
| `SaveButton` | `SaveButton.kt` | **KEEP** | Idle→Saving→Success morph is the canonical primary-CTA state machine — reuse everywhere a save action exists (confirm `AddVehicleScreen`/`AddReminderScreen`/`AddFuelScreen` all use it; `AddServiceScreen` does). |
| `PressScale` | `PressScale.kt` | **KEEP** | Small tactile utility, correctly reduced-motion aware. |
| `FormAnimations.kt` (`FormField`) | `FormAnimations.kt` | **KEEP, rename risk** | This is a *staggered-entrance motion wrapper*, not a text-field component — the name is misleading (a design-system newcomer will expect a labeled input wrapper). Low-priority rename candidate (`StaggeredFormEntrance`?). |
| **No canonical text-field wrapper exists.** | n/a | **CREATE (see §7)** | Every screen (`AddServiceScreen`, `OnboardingScreen`, `QuickMileageSheet`) calls raw M3 `OutlinedTextField` directly with hand-repeated label/placeholder/keyboard-type/mono-styling logic. This is the single largest real gap between the shipped kit and the "canonical component system" the brief asks for in §7. |
| Charts (`CostByTypeDonut`, `FuelEfficiencyChart`, `SpendingTrendChart`) | `charts/*.kt` | **DEFER** | Not reviewed line-by-line this pass (Pro-gated, secondary to the Home/attention work this blueprint prioritizes) — flag for a follow-up compose-performance pass given Canvas-based charts are a common recomposition/perf risk area. |

---

## 7. CANONICAL COMPONENT SYSTEM

Each entry states current status against the repository (not aspirational).

### Vehicle context / header
**Exists as `VehicleHeroCard`.** Purpose: identify which car; Compact
(list row) and Expanded (detail hero) variants. Hierarchy: title (make +
model) → year/odometer meta line (JetBrains Mono) → one-line concern text →
status chip. States: photo / no-photo (icon-tile fallback, required proof
state per §11b of the design doc), with-rail / without-rail (only OVERDUE/
DUE_SOON get a rail). Accessibility: `mergedContentDescription` collapses
the whole card to one TalkBack stop in list contexts, stays unmerged in
Detail for granular exploration (both patterns are correctly present in the
two call sites). Content limits: title `maxLines = 2` with ellipsis.
Responsive: `Expanded` photo header is a fixed 200dp — should be audited at
tablet/landscape widths (not verified this pass; flag for §14).

### Maintenance summary / verdict
**Exists as `HealthCockpitCard`.** Purpose: the single "what do I need to
know right now" sentence. Hierarchy: optional `StatusChip` → headline
sentence → supporting sentence → optional demoted score ring → optional
text-button action. Recommended canonical variant per §5: **score always
null** on Home/Vehicle-Detail-entry use; the ring stays in the component's
API for a future context where a real, non-fabricated number exists (e.g.,
"3 of 3 up to date" ratios), but is not wired today.

### Item row / card
**Exists as two siblings, not one:** `ProactiveAttentionCard` (attention
context: title + reason + status + one CTA) and `StatusReminderCard`
(routine list context: title + timing lines + Done/Snooze/Edit). This
split is correct, not duplication — attention and routine reminders have
different action sets (one CTA vs three) and different urgency framing
(reason-text vs timing-text). Keep both; do not merge.

### Status indicator
**Exists as `StatusChip`** (text badge) **+ `PremiumStatusStyle`** (rail +
corner + container/content color, applied by the card, not the chip).
Together these are the canonical 4-channel status system. Six states map
1:1 to the domain `ServiceStatus` enum — no invented states.

### Attention rail (dashboard section)
**Exists**: `PremiumSectionHeader` (title="Needs attention", count) +
`ProactiveAttentionCard` list, worst-status-first. Already correctly
omitted when empty (falls through to the calm "All clear" verdict instead).

### Upcoming section
**Exists**: same `PremiumSectionHeader` + `StatusReminderCard` pattern,
used on Vehicle Detail for non-attention reminders. Not currently present
on Home (Home only shows attention + roster) — intentionally, since Home's
job is "what needs attention," not a full reminder ledger; Vehicle Detail
owns the complete list.

### Vehicle card
Same component as "Vehicle context/header" above — `VehicleHeroCard`. Do
not create a second "vehicle card" for the roster; Compact variant already
serves this.

### Record timeline item
**Exists as `RecordsTimelineCard`**, canonical shape correct (icon tile +
title/vehicle/odometer + cost/date column) — **but not wired into**
`ServiceHistoryScreen.kt`, which hand-rolls an equivalent `ServiceHistoryCard`
locally with a slightly different layout (no icon tile, currency formatted
inline with `NumberFormat` rather than the caller-formats-nothing contract
`RecordsTimelineCard` documents). Consolidate: adopt `RecordsTimelineCard`
in `ServiceHistoryScreen`, move icon resolution through the existing
`ui/util/icon()` extension already used elsewhere (`ServiceChoicePicker`,
`ServiceTypeGrid`).

### Metric display
**Exists as `InsightMetricCard`/`InsightMetricRow`.** Canonical for
overdue/due-soon counts and any future Pro cost/efficiency summary tiles.

### Quick action
**Exists as `PremiumActionGrid`/`PremiumAction`.** Canonical 2-column tile
grid, one optionally emphasized. Dashboard's FAB quick-action menu
(`QuickActionRow` in `DashboardScreen.kt`) is a **different, unreconciled
pattern** — small-FAB + label chip inside an expanding FAB menu, not the
tile grid. This is a legitimate divergence (FAB menus and in-page action
grids solve different placement problems) but should be named as
intentional rather than accidental in `DESIGN_SYSTEM_2026.md`.

### Primary / secondary button
**No dedicated wrapper beyond `SaveButton`** (which is specifically a
save-state-machine button, not a generic primary button). Plain M3
`Button`/`OutlinedButton`/`TextButton` are used directly and consistently
styled through `MaterialTheme` — acceptable, since M3's own components
already satisfy the 56dp/pill/one-per-screen rules when used with the
theme's shapes. No action needed beyond confirming shape consistency
(§8 flags one shape question).

### Text field
**Gap — recommend CREATE.** No `AutoMinderTextField` wrapper exists; every
screen repeats the same `OutlinedTextField` boilerplate (label, keyboard
type, mono styling for numeric fields, error text placement). A thin
wrapper standardizing radius (14dp per the design doc), leading-icon slot,
numeric-mono variant, and inline error text would remove real, observed
duplication across `AddServiceScreen`, `OnboardingScreen`,
`QuickMileageSheet`, and (by strings-evidence) `AddVehicleScreen`/
`AddReminderScreen`/`AddFuelScreen`.

### Service picker
**Exists as `ServiceChoicePicker`** (recent/common/all-services fast path)
and **`ServiceTypeGrid`** (plain responsive grid) — see §6 consolidation
note. Recommend `ServiceChoicePicker` as canonical; retire the grid unless
a screen genuinely needs the simpler variant with no "recent" concept.

### Empty state / Error state / Skeleton
**All exist** (`EmptyState`, `ErrorState`, `Skeleton.kt`) and are used
consistently across every screen read this pass. Skeleton has the one real
implementation conflict noted in §6 (shimmer sweep vs documented opacity
pulse).

### Info banner
**Exists inline, not componentized** — `AllClearBanner` is a private
composable inside `VehicleDetailScreen.kt`, and `ReminderPromptCard` inside
`AddServiceScreen.kt` is a second, differently-styled "banner" (rounded
`secondaryContainer` box with icon + text + switch). Recommend extracting a
shared `InfoBanner` (icon + message, optional trailing control) so future
screens don't hand-roll a third variant.

### Bottom sheet
**No wrapper beyond M3 `ModalBottomSheet`**, used directly and consistently
(`QuickMileageSheet`, `ReminderDetailSheet`, `ProPaywall`,
`ServiceChoicePicker`'s "all services" sheet). Consistent `surfaceContainer`
background in most; `ReminderDetailSheet` omits the explicit
`containerColor` override others use — minor, low-risk inconsistency.

### Confirmation dialog
**Exists as `DiscardChangesDialog`** for the unsaved-changes case; archive
and delete confirmations are hand-rolled `AlertDialog` calls inline in
`VehicleDetailScreen.kt` and `EditReminderScreen` (per strings evidence:
`edit_reminder_delete_title`/`_message`). Recommend a generic
`ConfirmationDialog(title, message, confirmLabel, destructive: Boolean)` to
replace all three call sites with one component and one destructive-color
rule (`error` for confirm text when destructive, matching the archive
dialog's existing `MaterialTheme.colorScheme.error` usage).

### Settings row
**No component — hand-rolled per row** in `SettingsScreen.kt` (icon +
label + trailing chevron/switch/segmented-control, repeated five times
with copy-pasted `Row`/`Icon`/`Text` boilerplate). Recommend a
`SettingsRow(icon, label, trailingContent)` component; this is the most
repetitive un-componentized pattern found in the whole codebase (five
near-identical 15-line blocks in one file).

### Paywall plan
**Exists as `PremiumPaywallPlanCard` + `PremiumPriceDisplay`.** Already
correct and Billing-migration-safe (closed price state, never a raw
nullable String) — no changes recommended.

---

## 8. COLOR SYSTEM

Source of truth: `ui/theme/Color.kt` + `AutoMinderTheme.kt`, confirmed
against `docs/DESIGN_SYSTEM_2026.md`. **Preserve Midnight Cobalt** — nothing
in the repository argues against it; it is well-reasoned (176° hue
separation from the caution band, WCAG-computed contrast at every token,
dark-as-primary-with-light-as-first-class-parity) and already fully
implemented in both `DarkColorScheme` and `LightColorScheme`.

**Brand/action vs maintenance-state semantics** are already correctly
separated in principle (`docs/DESIGN_SYSTEM_2026.md` §3's "semantic
independence" table: Action = `brand/accent`/`action/primary`/
`navigation/selected`; Maintenance state = `status/good`/`status/due`/
`status/overdue`/`status/nodata`) even though both groups currently alias
the same Material `primary` token. This is documented as intentional, with
an explicit warning not to let a future brand-color retune silently change
what "up to date" means — a good practice, worth preserving verbatim in
whatever document becomes canonical going forward.

**Status must never use button-like shape — open decision, not a silent
override.** The brief's instruction is explicit: status should carry
color/icon/label/rail/border/tone/copy, and **never** a status-dependent
corner radius. The shipped system does the opposite by design:
`PremiumStatusStyle.cornerRadius()` morphs OVERDUE=8dp (sharp) → DUE_SOON=16dp
→ everything calm=28dp (soft), documented at length in
`docs/DESIGN_SYSTEM_2026.md` §3 as a deliberate "brand shape language" and
defended as one of four simultaneous status channels (never color alone).
This is a genuine, reasoned tension between the brief and the shipped,
already-accessibility-reviewed system — not something to resolve by
silently picking a side. Recommendation: **keep corner-morph** for this
version, because (a) it is not the *only* channel — color, rail, and text
label are always present too, so it never becomes "shape-only status" the
way a button's shape can be, and (b) removing it would be a same-session
regression of a feature the repo's own accessibility reasoning already
defends. But this should be flagged explicitly to the human owner as an
open brand-direction question before the next major visual pass, since the
brief's instruction came from an explicit design mandate and may reflect a
newer decision than the repository's Aug-2026 doc.

**Confirmed accessibility exemptions** (from the design doc, verified
against the actual hex values in `Color.kt`): `border/subtle` intentionally
sits below 3:1 (decorative dividers, WCAG 1.4.11 doesn't apply);
`disabled-content` intentionally sits below 4.5:1 (WCAG 1.4.3 exempts
incidental disabled states). Both are correctly documented as deliberate,
not oversights — preserve that documentation pattern in any successor doc.

---

## 9. TYPOGRAPHY DECISION

**Verdict: MIGRATE — but currently unimplemented; treat as an open item,
not a completed decision.**

**Current shipped state** (`ui/theme/Type.kt`, `res/font/`): three families.
- **Exo 2** (Bold/ExtraBold only) — display/headline roles.
- **Nunito Sans** (Regular/Medium/SemiBold/Bold) — title/body/label roles.
- **JetBrains Mono** (Medium/SemiBold) — data roles (odometer, cost, dates).

Font files on disk: `exo2_bold.ttf`, `exo2_extrabold.ttf`,
`nunitosans_{regular,medium,semibold,bold}.ttf`,
`jetbrainsmono_{medium,semibold}.ttf`. **No Manrope files exist anywhere in
`res/font/`.**

**Proposed state** (`docs/DESIGN_SYSTEM_2026.md` §4, written same session
as the current theme, evidently not yet implemented back into `Type.kt`):
retire Exo 2, consolidate display+headline+title+body+label into **Manrope**
(400/500/700/800), keep JetBrains Mono unchanged for data. The doc's own
reasoning is sound and is reproduced here as evidence, not invented:

- *"Exo 2 appeared only on vehicle names and hero numbers — a whole font
  download for two roles a heavier weight can carry."* — true; grep
  confirms Exo2 is referenced only by `displayLarge/Medium/Small` and
  `headlineLarge/Medium/Small` in `Type.kt`, all bold-or-heavier weights
  that Manrope's 700/800 weights can carry without a second family.
- APK weight: dropping two Exo2 `.ttf` files and one font-loading pass
  directly serves `CLAUDE.md`'s "routine service log ~15s" and general
  performance-conscious mandate — a measurable, not cosmetic, win.
- Automotive relevance: Exo 2 is literally "racing-inspired" and was the
  more overtly automotive choice; Manrope is a neutral geometric
  humanist sans. This is the one place the migration **trades away** a
  brand signature, in exchange for a lighter binary and one fewer family
  to keep visually reconciled with Nunito Sans (the doc frames this as
  "premium through restraint," consistent with the anti-neon/anti-gauge
  brief).
- Readability / 2.0× scaling: Manrope has well-documented broad Latin
  coverage (Spanish/Portuguese diacritics — `á é í ó ú ñ ã õ ç` — are
  standard in its character set; not independently verified against the
  literal font file since none exists yet, so treat this as expected-but-
  unverified until the actual variable font is added).
- Tabular figures: the doc claims Manrope "has genuinely good tabular
  figures at heavy weights," but AutoMinder's numeric-alignment-critical
  surfaces (odometer, cost, dates) are explicitly **carved out to JetBrains
  Mono** regardless — Manrope's own numeral quality is therefore lower-
  stakes than the doc implies; it only needs to look right in body/heading
  contexts, which is a much easier bar.
- Licensing: Manrope ships under SIL Open Font License (same license
  family as Exo 2 and Nunito Sans already in the repo) — no licensing risk
  expected, though this must be re-confirmed against whichever exact
  Manrope release/version file is actually added (weight axis / variable
  vs static).
- Migration cost: **Low-to-medium.** No `Color(0xFF..)`-style scattered
  usage to hunt down — `Type.kt` is the single source of truth for every
  text style, so the change is: add Manrope `.ttf` files, swap
  `Exo2`/`NunitoSans` `FontFamily` declarations in `Type.kt` to the single
  `Manrope` family (respecting the weight-per-role table in the design
  doc), delete the two Exo2 files, and re-run the full visual QA matrix
  (2.0× font scale, es/pt-BR string lengths, dark/light) since every screen
  in the app inherits from `Typography`.

**Why MIGRATE and not HYBRID:** a hybrid (keep Exo2 for vehicle
name/hero-number roles, Manrope everywhere else) reintroduces the exact
problem the design doc identifies — a third family carried for two roles.
There is no repository evidence that Exo2's racing character is load-
bearing for the brand (no user research, no A/B result, no PRD requirement
citing it); it is simply the incumbent. Given the brief's own instruction
to prefer restraint over decoration, full migration is the more defensible
default. **This migration should not be implemented in this pass** (no
production code was touched per this task's scope) — it is queued in §16
as its own design-system-foundation issue.

---

## 10. ICON SYSTEM

Current shipped state: `androidx.compose.material.icons.filled.*` /
`.automirrored.filled.*` (classic Material Icons Extended), not yet Material
Symbols Rounded as `docs/DESIGN_SYSTEM_2026.md` §6 specifies. This is
another documented-but-unimplemented item, evidenced by every screen file
read this pass importing from `androidx.compose.material.icons.filled`.

**Canonical strategy going forward** (adopting the doc's own spec, which is
reasonable and consistent with Manrope's geometric warmth): Material
Symbols Rounded, weight 400, optical size 24, grade 0 light / grade −25
dark. Filled = active/selected (already correctly implemented in
`BottomNavBar` and `ServiceChoicePicker`'s selected-state icon tint
swapping), outlined = inactive.

**48dp targets:** Verified in code — `ServiceChoicePicker`'s
`defaultMinSize(minHeight = 56.dp)`, `PremiumActionGrid`'s
`heightIn(min = 56.dp)`, both clear the 48dp floor with margin.
`SettingsScreen`'s icon-only rows do not currently enforce a minimum touch
target explicitly (relies on Row padding) — worth an explicit accessibility
sweep pass (§13).

**TalkBack labeling:** Correctly split in the codebase — decorative icons
pass `contentDescription = null` (verified in `RecordsTimelineCard`,
`InsightMetricCard`, `PremiumActionGrid`'s tile icon, all with an explicit
comment "label text carries the meaning"); meaningful icons carry a real
description (verified in `ProPaywall`'s `FeatureCheck`, which has an
explicit code comment citing WCAG 1.1.1/4.1.2 by number). This distinction
is being made correctly and deliberately, not accidentally — preserve it as
a written rule in whatever succeeds `.claude/rules/ui.md`.

---

## 11. MOTION SYSTEM

`ui/theme/Motion.kt` is a strong, already-correct canonical strategy:
shared springs (`springDefault`/`springGentle`/`springSnappy`), M3
emphasized easing curves, a single `reduceMotion` volatile flag read by
every animation in the app (verified: `Skeleton.kt`, `PremiumStatusStyle.kt`,
`ProPaywall.kt`'s scale animation, `OnboardingScreen.kt`'s step transitions
all gate through `Motion.reduceMotion`/`Motion.amplitude`), and a
`staggerDelay` helper that collapses to zero under reduced motion. This is
better-than-typical engineering for an indie app and should not be
re-architected.

**One real conflict, not hypothetical:** `Skeleton.kt`'s `shimmer()`
modifier is an `infiniteRepeatable` translating linear-gradient — the exact
"shimmer sweep" pattern `docs/DESIGN_SYSTEM_2026.md` §9 explicitly rejects
in favor of a 0.40→0.70 opacity pulse. It does correctly drop to a static
tint under reduced motion (no infinite animation runs when the user has
asked for none — the one hard rule that matters most is honored), but the
frame-cost and "reads as dated" critique the doc makes about sweeps applies
to the actual shipped code, not just to a rejected alternative design. Fix
recommended in §15/§16.

No infinite animation without genuine ongoing activity is otherwise
honored everywhere checked (score-ring arc uses `animateFloatAsState`
toward a target, not an infinite loop; FAB rotation is state-driven, not
infinite).

---

## 12. SCREEN-BY-SCREEN DESIGN CONTRACT

`MOBBIN EVIDENCE` is marked BLOCKED on every entry (see §1). Everything
else reflects the actual shipped screen where the screen file was read in
full this pass, or shipped strings/established patterns where it was not
(noted per screen).

### Home (Dashboard) — screen file read in full
- **USER GOAL:** "Do I need to do anything with my car(s) right now?"
- **PRIMARY HIERARCHY:** Verdict sentence → (optional) attention cards →
  vehicle roster.
- **PRIMARY ACTION:** FAB → Log Service / Add Fuel / Add Vehicle
  (context-aware: skips the vehicle picker sheet when there's exactly one
  vehicle).
- **SECONDARY ACTION:** Tap a vehicle card → Vehicle Detail. Tap an
  attention card's CTA → Vehicle Detail (same destination, different entry
  reason).
- **COMPONENTS:** `HealthCockpitCard` (score=null, §5), `InsightMetricRow`
  (unfabricated overdue/due-soon counts), `PremiumSectionHeader`,
  `ProactiveAttentionCard`, `VehicleHeroCard` (Compact).
- **STATES:** Loading (`DashboardSkeleton`) / Empty (`EmptyState`, "No
  vehicles yet") / Error (`ErrorState`) / Success (as above). All four
  verified present in the actual `when (uiState)` block.
- **EMPTY STATE:** "No vehicles yet" / "Add your first vehicle to start
  tracking maintenance." + CTA + hint ("Most users add their first vehicle
  in under 30 seconds").
- **ERROR BEHAVIOR:** `ErrorState` + Retry, calling `viewModel.retry()`.
- **MOBBIN EVIDENCE:** BLOCKED.
- **ANDROID ADAPTATION:** System back exits the app from Home (no
  in-progress form to intercept, correctly); predictive back not
  specifically wired here since there's nothing to protect, which is
  correct per the design doc's "predictive back on any in-progress form"
  scope.

### Vehicles (list) — screen file read in full
- **USER GOAL:** "Show me every car I own and its status at a glance."
- **PRIMARY HIERARCHY:** Verdict sentence (`VerdictSentence`, reusing
  Dashboard's exact copy) → vehicle rows, first row promoted to Expanded
  hero variant.
- **PRIMARY ACTION:** FAB → Add Vehicle.
- **SECONDARY ACTION:** Tap a row → Vehicle Detail.
- **COMPONENTS:** `VehicleHeroCard` (Expanded for index 0, Compact
  otherwise — a deliberate "first car gets more presence" hierarchy choice,
  worth confirming intentional rather than accidental since nothing marks
  which vehicle is "primary" in the domain model itself — it's positional,
  not a flagged favorite).
- **STATES:** Loading (`ListSkeleton`) / Empty / Error / Success — all
  four present.
- **EMPTY STATE:** "No vehicles yet" / "Tap the + button to add your first
  vehicle."
- **ERROR BEHAVIOR:** `ErrorState` + Retry.
- **MOBBIN EVIDENCE:** BLOCKED.
- **ANDROID ADAPTATION:** Standard list scroll behavior
  (`enterAlwaysScrollBehavior` on the top bar), consistent with Home.

### Vehicle Detail — screen file read in full
- **USER GOAL:** "Everything about this one car: status, odometer, history,
  what's due."
- **PRIMARY HIERARCHY:** Hero (name, photo/fallback, status chip) →
  odometer instrument (tappable) → diagnosis card (`HealthCockpitCard` or
  `HealthSetupCard` if zero reminders) → [Pro] cost/efficiency cards →
  action grid → Needs Attention (triage, 3-visible-then-expand) → Upcoming.
- **PRIMARY ACTION:** FAB → Add Reminder. Action grid's emphasized tile →
  Log Service.
- **SECONDARY ACTION:** Odometer tap → `QuickMileageSheet`; reminder card
  tap → `ReminderDetailSheet`; overflow menu → Export / Archive.
- **COMPONENTS:** `VehicleHeroCard` (Expanded), `HealthCockpitCard`,
  `HealthSetupCard` (private, empty-reminders variant), `ProFeatureGate`
  wrapping cost/efficiency `ElevatedCard`s, `PremiumActionGrid`,
  `PremiumSectionHeader`, `StatusReminderCard`, `AllClearBanner` (private).
- **STATES:** Loading (`ListSkeleton`) / Empty (vehicle not found) / Error
  / Success — all four present via `ScreenState.fromUiState`.
- **EMPTY STATE:** "Vehicle Detail" not-found title + "This vehicle may
  have been deleted."
- **ERROR BEHAVIOR:** `ErrorState` with dynamic `errorRes`+`errorArgs`.
- **MOBBIN EVIDENCE:** BLOCKED.
- **ANDROID ADAPTATION:** Predictive-back-safe (no in-progress form on this
  screen); top-bar title appears only after the hero scrolls away
  (deliberate anti-duplicate-announcement pattern, confirmed via
  `showBarTitle` derived state) — a good Android-native pattern (iOS large-
  title collapse equivalents don't map 1:1, so this is already correctly
  Android-idiomatic rather than ported).

### Add/Edit Vehicle — inferred from strings + established patterns (screen file not read this pass)
- **USER GOAL:** Register a car with minimum typing.
- **PRIMARY HIERARCHY** (from `add_vehicle_headline`/`add_vehicle_subhead`
  strings): "What are you driving?" / "Just the make and model — you can
  add the rest anytime," then progressive-disclosure "Add more details."
- **PRIMARY ACTION:** "Add Car" (`add_vehicle_cta`).
- **SECONDARY ACTION:** "Change" photo (`action_change_photo`).
- **COMPONENTS (expected, per established pattern):** `SaveButton`,
  `DiscardChangesDialog`, raw `OutlinedTextField`s (candidate for the
  proposed text-field wrapper, §7).
- **STATES:** Not independently verified this pass — flag for direct
  confirmation before implementation work.
- **EMPTY STATE:** N/A (creation form).
- **ERROR BEHAVIOR:** `error_brand_model_required`/`error_make_model_required`
  strings confirm inline validation exists.
- **MOBBIN EVIDENCE:** BLOCKED.
- **ANDROID ADAPTATION:** Not verified this pass.

### Add/Edit Reminder — inferred from strings (screen file not read this pass)
- **USER GOAL:** Set a maintenance reminder by date, mileage, or both.
- **COMPONENTS (expected):** Service picker, date field, interval fields
  (`label_interval_km`/`label_interval_days`), `SaveButton`.
- **ERROR BEHAVIOR:** `error_custom_reminder_name_required`,
  `error_reminder_due_required` confirm dual-trigger validation.
- **EDIT-SPECIFIC:** Delete confirmation dialog
  (`edit_reminder_delete_title`/`_message`) — candidate for the proposed
  generic `ConfirmationDialog` (§7).
- **MOBBIN EVIDENCE:** BLOCKED.

### Reminder Detail — `ReminderDetailSheet.kt` read in full (presented as a sheet, not a full screen route)
- **USER GOAL:** "Can this wait, and what should I do about it."
- **PRIMARY HIERARCHY:** Title + severity badge → personalized timing →
  what it means → can it wait → actions (Edit / Snooze / Done).
- **COMPONENTS:** Private `SeverityBadge`, `SheetSection`; consumes
  `Reminder`, `ServiceStatus`, `DuePrediction`.
- **MOBBIN EVIDENCE:** BLOCKED.

### Add Service — screen file read in full
- **USER GOAL:** Log a completed service in ~15 seconds (CLAUDE.md's stated
  target).
- **PRIMARY HIERARCHY:** "What was done?" (`ServiceChoicePicker`) →
  odometer → date → "Remind me for the next one" prompt → optional-details
  disclosure (cost/shop/notes).
- **PRIMARY ACTION:** Sticky bottom `SaveButton`.
- **SECONDARY ACTION:** "Add more details" disclosure toggle.
- **COMPONENTS:** `ServiceChoicePicker`, `FormField` (stagger-entrance
  wrapper), `ReminderPromptCard` (private — candidate for the proposed
  shared `InfoBanner`, §7), `DiscardChangesDialog`, `SaveButton`.
- **STATES:** Idle/Saving/Success via `SaveButtonState`; discard-guard via
  `hasUnsavedChanges` (correctly excludes prefilled defaults from counting
  as edits).
- **ERROR BEHAVIOR:** Inline error text below the form, haptic reject
  feedback on error.
- **MOBBIN EVIDENCE:** BLOCKED.
- **ANDROID ADAPTATION:** `imePadding()` applied at the Scaffold level (one
  owner for IME insets, explicit code comment explaining why) — correct
  Android-specific keyboard handling, no iOS-ported assumption.

### Service Detail — inferred from strings (screen file not read this pass)
- **COMPONENTS (expected):** Detail rows for odometer/cost/shop/notes,
  delete confirmation (`service_detail_delete_title`/`_message`).
- **MOBBIN EVIDENCE:** BLOCKED.

### Records (Service History) — screen file read in full
- **USER GOAL:** Cross-vehicle chronological service ledger.
- **PRIMARY HIERARCHY:** Sticky month/year headers → service cards
  (swipe-to-delete with undo snackbar).
- **COMPONENTS:** Locally-defined `ServiceHistoryCard` — **should migrate
  to the canonical `RecordsTimelineCard`** (§6/§7 consolidation).
  `SwipeToDeleteContainer`.
- **STATES:** Loading/Empty/Error/Success all present.
- **EMPTY STATE:** "No service records" / hint about 10-second logging.
- **ERROR BEHAVIOR:** `ErrorState` + Retry.
- **MOBBIN EVIDENCE:** BLOCKED.

### Mileage — inferred from strings (screen file not read this pass)
- **COMPONENTS (expected):** New-reading form + history list,
  `mileage_log_empty` empty state.
- **MOBBIN EVIDENCE:** BLOCKED.

### Fuel — inferred from strings (screen file not read this pass)
- **COMPONENTS (expected):** Volume/cost/odometer/date form, efficiency
  unit switching (km/L, L/100km, MPG US/UK — all four confirmed in
  strings), empty state hint about the 3-fill-up threshold for trend charts.
- **MOBBIN EVIDENCE:** BLOCKED.

### Settings — screen file read in full
- **USER GOAL:** Notifications, theme, units, upgrade, legal.
- **PRIMARY HIERARCHY:** Pro upsell/status card → Preferences (notifications,
  theme, distance unit) → About & Legal (About, Privacy, Ad privacy).
- **COMPONENTS:** Hand-rolled rows throughout — **primary consolidation
  target**, recommend the proposed `SettingsRow` (§7). `ProPaywall` sheet
  triggered from the upsell card.
- **STATES:** Purchase/restore snackbar feedback wired through
  `PurchaseState`/`RestoreState` sealed types — correctly distinguishes
  Success/Cancelled/Pending/Error, no silent failure path.
- **ANDROID ADAPTATION:** Correct `POST_NOTIFICATIONS` runtime-permission
  gate on API 33+, UMP "Ad privacy options" row correctly conditional on
  `PrivacyOptionsRequirementStatus.REQUIRED` (EEA-only, not shown otherwise)
  — this is the kind of region-aware, honest UI the brief's "truthful
  states" principle demands, and it's already correct.
- **MOBBIN EVIDENCE:** BLOCKED.

### Pro (paywall) — `ProPaywall.kt` read in full (presented as a sheet from Settings, no dedicated route)
- **USER GOAL:** Understand what Pro adds, pick a plan, subscribe or
  restore.
- **PRIMARY HIERARCHY:** Feature comparison table (Free vs Pro columns) →
  selectable plan cards (Yearly pre-selected, badged "Best value") →
  Continue → Restore Purchases (quiet text action).
- **STATES:** Price Loading/Available/Unavailable per plan (never a
  spinner-forever or silently blank price).
- **MOBBIN EVIDENCE:** BLOCKED.

### Onboarding — screen file read in full
- **USER GOAL:** Get one car registered and reminders scheduled with the
  least possible friction, seeing value before being asked for a
  permission.
- **PRIMARY HIERARCHY (4 steps):** Welcome → Add my car (one-tap popular
  makes + brand/model/odometer + driving-amount chips) → **Plan reveal**
  (first reminder + why + honesty disclaimers) → Notification permission
  ask.
- **KEY SEQUENCING DECISION (already correct and worth preserving):** the
  plan is computed and shown, and the vehicle is saved, **before** the
  notification permission is requested — so declining notifications can
  never erase the user's work. This is exactly the "entered data survives"
  law applied to a permission-gate, not just to process death.
- **CONTENT STRATEGY:** Explicit honesty strings —
  `onboarding_plan_honesty` ("These are editable general reminders, not
  {make}'s official schedule or proof that service is due"),
  `onboarding_plan_missing` ("We don't know this car's service history...")
  — a genuinely good anti-overclaim pattern, matching the brief's
  "never advertise capabilities that don't exist" mandate.
- **COMPONENTS:** `FilterChip` (make selection, driving-amount selection),
  `OutlinedTextField`s, custom segmented progress dots, `GlowHero` (radial
  icon treatment for Welcome/Notify steps).
- **STATES:** Saving spinner on the Plan step's CTA; inline error text for
  invalid odometer.
- **MOBBIN EVIDENCE:** BLOCKED.
- **ANDROID ADAPTATION:** `BackHandler` correctly scoped to only the
  Add-Car/Plan steps (back is disabled once the vehicle is saved and the
  Notify step is reached, since "going back" to a step whose Save button no
  longer does anything would be a dead-end control) — a deliberate,
  reasoned exception to blanket predictive-back support.

### About — inferred from strings (screen file not read this pass)
- **COMPONENTS (expected):** App name/version, developer label, Privacy
  Policy link, rate-us prompt, feedback-email prompt, copyright line — all
  confirmed present in strings, standard About-screen shape.
- **MOBBIN EVIDENCE:** BLOCKED.

---

## 13. ACCESSIBILITY CONTRACT

Baseline already largely met, confirmed against actual code (not assumed):

- **2.0× font scale:** `ServiceTypeGrid` derives column count from
  `LocalDensity.current.fontScale` rather than a hardcoded breakpoint —
  genuinely good, reflow-not-threshold design. `AddServiceScreen`'s
  `ReminderPromptCard` explicitly top-aligns its icon/switch row "at large
  text sizes the subtitle wraps to several lines" per its own code comment.
- **Status never color-only:** confirmed structurally throughout (§3, §6).
- **Merged semantics:** `VehicleHeroCard`, `HealthCockpitCard`,
  `ProactiveAttentionCard`, `StatusReminderCard`, `RecordsTimelineCard` all
  use `Modifier.semantics(mergeDescendants = true)` correctly.
- **Meaningful vs decorative icon labeling:** confirmed correct split (§10).
- **Touch targets:** 56dp+ confirmed on action tiles/choice chips; Settings
  rows and some icon-only buttons not independently re-verified against the
  48dp floor this pass — recommend a targeted sweep (the
  `accessibility-qa`/`accessibility-reviewer` tooling already in this repo's
  `.claude/` setup is the right tool for that, not this research pass).
- **Known, real gap:** `cd_health_score` string literally says "Health
  score out of 100," which is simultaneously (a) the exact phrase
  `CLAUDE.md` bans and (b) read aloud to every TalkBack user on Home and
  Vehicle Detail today. Fixing this is accessibility work, not just brand
  copy — it's the string TalkBack users specifically hear.
- **Reduced motion:** `Motion.reduceMotion` gates every animation spec
  checked this pass; the one gap is the shimmer sweep's frame cost while
  motion *is* enabled (§11) — not a reduced-motion violation, but worth
  fixing for users who haven't opted out of motion but are on lower-end
  devices, which is a real chunk of AutoMinder's likely Android install
  base (minSdk 26).
- **Not verified this pass (requires device, not code reading):** actual
  TalkBack traversal order end-to-end, actual behavior at 200% system font
  on real layouts, actual contrast rendering on a physical OLED panel.
  State this explicitly rather than implying it was tested — per
  `CLAUDE.md`'s Definition of Done, "state plainly what was and wasn't
  verified."

---

## 14. RESPONSIVE CONTRACT

- **Small/large phone:** `ServiceTypeGrid`'s width-driven column math and
  `ServiceChoicePicker`'s `FlowRow` both correctly reflow by available width
  rather than a fixed breakpoint — good precedent to require for any new
  grid-shaped component.
- **Landscape / split-screen / tablet:** No dedicated `WindowSizeClass`
  handling was found in any screen read this pass; `CLAUDE.md`'s Architecture
  Law names `NavigationSuiteScaffold`/Material3 Adaptive APIs as the
  required pattern for window info, but the actual `BottomNavBar` uses a
  plain `NavigationBar`, not an adaptive suite scaffold — this is a real,
  unverified gap for tablet/large-screen Play requirements, not confirmed
  fixed or broken this pass. `VehicleHeroCard`'s fixed 200dp photo header
  is a concrete point that will look wrong on a wide tablet layout without
  a size-class-aware max-width.
- **200% font:** Addressed structurally per §13; not device-verified.
- **TalkBack:** Addressed structurally per §13; not device-verified.
- **Reduced motion:** Fully addressed via `Motion.reduceMotion`, verified
  in code across every animated component read.
- **Dark/light:** Both color schemes are fully defined with parity
  (`Color.kt`), and the design doc computes contrast for both explicitly —
  no light-mode-as-afterthought pattern found.
- **es / pt-BR:** `strings.xml` plurals are correctly used for count-driven
  strings (`dashboard_attention_headline`, `digest_summary`,
  `onboarding_plan_why`) — the exact places English's singular/plural
  binary breaks down in Spanish and Portuguese. One documented exception:
  `digest_summary` in `WeeklyDigestWorker.kt` is called out in a
  `tools:ignore` comment as accepting non-real-pluralization because
  WorkManager files are out of scope for the pass that added it — a
  correctly-labeled known compromise, not a hidden one.

---

## 15. DESIGN RISKS

1. **"Health score" language is currently live and TalkBack-audible**,
   directly contradicting `CLAUDE.md`'s named product-language ban. Not
   hypothetical — `cd_health_score`, `vehicle_health_title`, and four
   sibling strings exist and are wired into shipped screens today. This is
   the single highest-priority fix in this entire document because it is
   both a governance-rule violation and (per §13) an accessibility-audible
   one.
2. **Three different "health score" formulas exist simultaneously**
   (`FleetHealthScore.calculateHealthScore`, `DashboardScreen`'s inline
   `fleetScore`, `VehicleDetailScreen`'s `computeHealthScore`) with three
   different weightings and no shared source of truth. Even if the ring is
   kept rather than removed, three formulas for one concept is a
   maintainability and consistency risk on its own.
3. **Skeleton shimmer contradicts its own governing document** (§6/§11) —
   low user-visible severity, but a real drift between documented intent
   and shipped frames-per-second behavior.
4. **Typography and iconography migrations are documented as decided but
   not implemented** (§9/§10) — a risk only if someone assumes
   `docs/DESIGN_SYSTEM_2026.md` describes current behavior rather than
   target behavior. Treat the doc as a spec, the code as ground truth,
   exactly as the doc itself instructs in its own first line.
5. **`.claude/rules/ui.md` is stale relative to `docs/DESIGN_SYSTEM_2026.md`**
   — it still documents "Racing Teal #006B5F primary" and "Exo 2 (display)"
   as current brand tokens, and describes GOOD status as getting
   `secondaryContainer` (the shipped code uses `surfaceContainer` via
   `PremiumStatusStyle.containerColor`'s `else` branch, not
   `secondaryContainer`). Since `ui.md` auto-loads on every UI file touch
   per `CLAUDE.md`'s routing table, **a future session could act on stale
   brand tokens by default** unless this file is reconciled. This is an
   operational risk to the governance system itself, not just a cosmetic
   doc-drift issue.
6. **Status corner-radius-as-shape-channel vs the task brief's explicit
   "no status-dependent corner radii" instruction** (§8) is an open brand
   decision this document deliberately did not resolve unilaterally.
   Left unresolved, a future session might "fix" it in either direction
   without realizing it's a known, previously-reasoned tension.
7. **No adaptive/window-size-class handling found** (§14) against an
   Architecture Law that names it as required — a real gap for Play's
   large-screen/tablet requirements at submission time, independent of and
   parallel to the Billing migration deadline already tracked in
   `CODEX_HANDOFF.md`.
8. **Mobbin competitive evidence is entirely missing** (§1/§2) — every
   recommendation in this document is repository-internal reasoning, not
   validated against how Mercedes/BMW/Tesla or category-leading maintenance
   apps actually solve these same problems on shipped surfaces. Treat §4/§5
   (Home direction) as a reasonable default, not a competitively-verified
   best practice, until the Mobbin pass can run.
9. **A prior in-house Stitch exploration (project `5215641449031779482`)
   contains ~15 screens and one full "Family Access" flow that directly
   contradict AutoMinder's accountless, offline-first, no-telemetry
   identity** (§2): a complete login/account system with Google/Apple SSO,
   a "Sync & Backup Center" with named-device cloud restore, fabricated
   OBD-II-style gauges (tire PSI, fuel %, battery %) on nearly every
   dashboard variant, and a multi-user family-sharing flow with per-driver
   curfew/speed-limit fields implying GPS telemetry. None of this is built,
   and CLAUDE.md explicitly defers or rejects every one of these
   directions — but because these are real, polished, high-fidelity mockups
   sitting in an active Stitch project titled after this app, there is a
   concrete, non-hypothetical risk that a future session (or a
   non-engineering stakeholder skimming the project) mistakes them for
   approved direction rather than superseded exploration. Recommend
   labeling or archiving the out-of-scope screens in Stitch itself (not
   just in this document) so the two evidence sources don't silently
   diverge again.
10. **Two independent audits — this session's direct code read, and the
    pre-existing "Autominder Production Audit" document found in the
    Stitch project — reached the same conclusion about the health-score
    logic without cross-checking each other**, which raises confidence
    that "three unreconciled health-score formulas" (§6, §15 item 1) is a
    real, load-bearing problem rather than a one-session misreading, and
    argues for treating Implementation Queue item 3 (consolidate the
    formulas) as equally urgent to item 1 (fix the banned language),
    not a lower-priority follow-on.

---

## 16. IMPLEMENTATION QUEUE (GitHub-issue-sized slices)

1. **Fix banned "health score" language + remove fabricated fleet-score
   ring from Home/Vehicle-Detail cockpit cards.** (Exact scope in §17 — the
   recommended first issue.)
2. **Delete `FleetHealthScore.kt` and its five `vehicle_health_*` strings**
   once confirmed dead (or fold its one real call site, if any exists, into
   `HealthCockpitCard`). Small, mechanical, dependent on #1's audit.
3. **Consolidate the three health-score formulas** into one, or remove the
   concept entirely per #1 — decide alongside #1, don't ship #1 without
   resolving this.
4. **Reconcile `.claude/rules/ui.md`** with `docs/DESIGN_SYSTEM_2026.md`
   (Racing Teal → Midnight Cobalt, corner/container mapping, Exo2 status).
   Governance-file fix, no app code touched, unblocks every future UI
   session from acting on stale tokens.
5. **Fix the skeleton shimmer** to the documented opacity-pulse
   (0.40→0.70, 1000ms ease-in-out), matching `docs/DESIGN_SYSTEM_2026.md`
   §9 exactly. Isolated to `Skeleton.kt`.
6. **`RecordsTimelineCard` consolidation** — migrate `ServiceHistoryScreen`
   off its locally hand-rolled `ServiceHistoryCard`.
7. **`ServiceChoicePicker`/`ServiceTypeGrid` consolidation** — confirm
   remaining call sites of the plain grid, retire it if `ServiceChoicePicker`
   has fully superseded it.
8. **New shared components:** `SettingsRow`, `ConfirmationDialog`,
   `InfoBanner`, `AutoMinderTextField` — four small, independent component
   additions, each usable standalone (§7). Recommend as four separate small
   issues, not one "build the missing components" mega-issue.
9. **Typography migration (design-system foundation): Manrope.** Larger,
   cross-cutting, requires new font assets, full visual QA matrix across
   every screen at 2.0× scale and both locales with non-Latin diacritics.
   Should be its own branch per `CLAUDE.md`'s git discipline, separate from
   any Home-direction work.
10. **Icon system migration: Material Symbols Rounded.** Similarly
    cross-cutting; can follow or run parallel to #9 since both touch every
    screen but are otherwise independent (fonts vs icon set).
11. **Adaptive/window-size-class audit** — confirm whether
    `NavigationSuiteScaffold` (named as required by the Architecture Law)
    is actually wired anywhere, and close the gap if it's the plain
    `NavigationBar` found in `BottomNavBar.kt` today. Relevant to Play
    large-screen requirements independent of the Billing deadline.
12. **Resolve the corner-radius-as-status-channel question** (§8/§15,
    item 6) as an explicit decision record before any further visual work
    touches status-bearing cards — a decision, not an implementation slice.

None of these are "redesign the app." Each is independently shippable,
independently testable, and scoped to a small, named file set.

---

## 17. EXACT FIRST UI ISSUE

### TITLE
Remove the fabricated fleet-score ring and banned "Health score" language
from the Home and Vehicle Detail cockpit cards

### GOAL
Ship the Final Home Direction from §5 (sentence-first verdict, no numeric
score in the first viewport) and simultaneously fix the one concrete,
present-tense violation of `CLAUDE.md`'s Product Experience Law found in
this pass: the literal phrase "Health score" is read to every TalkBack user
on both Home and Vehicle Detail today, and `FleetHealthScore.kt` plus five
`vehicle_health_*` strings use the explicitly-banned "Vehicle Health"
phrase.

### WHY NOW
- It is the smallest change that satisfies the brief's explicit mandate
  that Home's first viewport prioritize maintenance attention over
  analytics — `HealthCockpitCard.score` is already a nullable parameter;
  this is a call-site change, not new component work.
- It fixes a named governance violation (`CLAUDE.md`: *"NEVER: Vehicle
  health, Car health, Health score..."*), which by the project's own
  Definition of Done ("no protected file touched... required UI states +
  accessibility... considered") should not sit unresolved.
- It requires zero new assets (unlike the typography/icon migrations in
  §16), zero font work, and touches a small, named set of files — a true
  vertical slice, not a foundation-level change.
- It is independent of and does not conflict with the mandatory Billing
  migration (`migration/play-billing-9`) tracked in `docs/CODEX_HANDOFF.md`
  — different files, different branch, safe to run in parallel per
  `CLAUDE.md`'s git discipline.

### FILES
- `app/src/main/kotlin/com/autominder/app/ui/screens/dashboard/DashboardScreen.kt`
  (`DashboardContent`'s `HealthCockpitCard` call — drop `score`/
  `scoreDescription` arguments)
- `app/src/main/kotlin/com/autominder/app/ui/screens/vehicle/VehicleDetailScreen.kt`
  (`VehicleDetailContent`'s `HealthCockpitCard` call — same change;
  `computeHealthScore` becomes unused and should be removed alongside it,
  or repurposed only if a genuine non-fabricated use is found)
- `app/src/main/kotlin/com/autominder/app/ui/components/FleetHealthScore.kt`
  (delete, pending the zero-call-site confirmation below)
- `app/src/main/res/values/strings.xml`
  (remove or rename `vehicle_health_title`, `vehicle_health_great`,
  `vehicle_health_good`, `vehicle_health_fair`,
  `vehicle_health_needs_attention`; rewrite `cd_health_score` if any
  residual score-adjacent accessibility string remains needed; rename
  `cd_vehicle_health` used by `HealthSetupCard`'s icon description to
  something that doesn't say "health," e.g. reuse
  `vehicle_detail_needs_attention` context or a new
  `cd_maintenance_setup_icon`)
- `app/src/main/res/values-es/strings.xml`,
  `app/src/main/res/values-pt-rBR/strings.xml` (mirror the same string
  removals/renames — confirm these locale files exist and carry the same
  keys before editing)

### NON-GOALS
- No typography migration (§16 item 9 — separate issue).
- No icon-system migration (§16 item 10 — separate issue).
- No `RecordsTimelineCard`/`ServiceChoicePicker` consolidation (§16 items
  6–7 — separate issues).
- No change to the corner-radius-as-status-channel question (§8/§15 item
  6 — an open decision, not part of this slice).
- No change to `InsightMetricRow` (overdue/due-soon counts) — those are
  real, non-fabricated numbers and stay exactly where they are.
- No Billing-related file touched.

### IMPLEMENTATION
1. Grep the whole repo for `FleetHealthScore(` call sites to confirm it is
   genuinely dead code before deleting (stated as an assumption in §6/§17,
   not yet grep-verified in this research pass — verify before deleting).
2. In `DashboardScreen.kt`, remove the `score`/`scoreDescription` arguments
   from the `HealthCockpitCard(...)` call inside `DashboardContent`; remove
   the now-unused `fleetScore` local val if nothing else reads it.
3. In `VehicleDetailScreen.kt`, remove the `score`/`scoreDescription`
   arguments from the equivalent call; remove `computeHealthScore` if it
   becomes unused (confirm `HealthSetupCard`'s empty-reminders branch,
   which uses the `-1` sentinel from the same function, still needs its own
   `reminders.isEmpty()` check — it does, independent of the score value).
4. Delete `FleetHealthScore.kt` and its now-dead import in
   `DashboardScreen.kt`.
5. Remove/rename the strings listed above across `values/`, `values-es/`,
   `values-pt-rBR/` strings.xml.
6. Re-run `PremiumStatusStyle`/`HealthCockpitCard` usage grep to confirm no
   other screen still passes a score.

### ACCEPTANCE CRITERIA
1. Home's first viewport contains no numeric composite score — only the
   verdict sentence, optional attention cards, and the vehicle roster.
2. Vehicle Detail's diagnosis card contains no numeric composite score.
3. No string resource anywhere contains the literal phrase "Health score"
   or "Vehicle Health" (grep-verifiable).
4. `FleetHealthScore.kt` no longer exists, or its one legitimate remaining
   use (if grep finds one) has been migrated to `HealthCockpitCard` instead
   of deleted outright.
5. `gradlew compileDebugKotlin` passes (QUICK gate per `CLAUDE.md`); this
   is a UI-behavior change, so the HARDENING gate
   (`assembleDebug` + `testDebugUnitTest` + `lintDebug`) is also required
   before merge.
6. No test is weakened or deleted to make this pass; if any existing test
   asserts on the removed score/strings, it is updated to assert on the
   new sentence-only contract, not removed.

### SCREENSHOT MATRIX
- Home: 0 vehicles (empty state) / 1 vehicle, all-clear / 1 vehicle, 1
  overdue / multi-vehicle, mixed overdue+due-soon / multi-vehicle, all
  due-soon (no overdue) — each in light and dark.
- Vehicle Detail: zero reminders (setup card, not diagnosis card) / all
  reminders OK / some overdue / some due-soon only — each in light and
  dark.
- No-photo case for at least one vehicle in each Home screenshot (already
  the realistic majority case per §11b of the design doc — do not stage
  only photographed vehicles).

### ACCESSIBILITY MATRIX
- TalkBack pass over Home and Vehicle Detail confirming the cockpit card's
  merged announcement no longer contains "Health score" or any numeric
  score, in both the attention and all-clear states.
- 2.0× font scale on both screens' cockpit card (sentence-only layout
  should be strictly easier to keep on-screen than the previous
  sentence+ring layout — regression-check, not just confirm-still-works).
- Reduced-motion: confirm no leftover animation spec references the
  removed score (`animateFloatAsState` inside `ScoreInstrument` becomes
  entirely unused code, not just unreachable).

### LOCALIZATION MATRIX
- Confirm `values-es/strings.xml` and `values-pt-rBR/strings.xml` are
  edited in lockstep with `values/strings.xml` for every removed/renamed
  key — a partial edit that leaves a stale Spanish or Portuguese "Health
  score" string behind would be a worse outcome than not starting, since it
  would silently reintroduce the exact violation this issue exists to fix,
  invisible to an English-only reviewer.
- Confirm no plural resource (`dashboard_attention_headline`, etc.) is
  accidentally touched — this issue's string changes are additive/
  subtractive on the health-score family only.

### TESTS
- Update or add a ViewModel/UI-state test asserting `DashboardUiState`/
  `VehicleDetailUiState` no longer need to carry (or, if they never
  computed the score at the state layer, confirm they still don't) any
  score-shaped field for this purpose — the scoring math was UI-layer
  (`DashboardScreen.kt`/`VehicleDetailScreen.kt`), so this is likely a
  Compose-level test update rather than a ViewModel test change, but verify
  against actual test files before assuming which layer needs the change.
- If any existing test in `app/src/test/` references `FleetHealthScore` or
  `computeHealthScore`, update it to match; do not delete a test's
  assertions to make it pass per `CLAUDE.md`'s Testing Law — replace the
  assertion with the new correct behavior instead.

### DEFINITION OF DONE
Per `CLAUDE.md` §"Definition of Done": acceptance criteria above met
within this exact file scope; HARDENING gate passed; no test
weakened; accessibility and localization matrices above completed and
their results stated explicitly (not just "should be fine"); this document
does not claim "production-ready" — it states what was verified and what
was not, exactly as this research document itself has done throughout.
