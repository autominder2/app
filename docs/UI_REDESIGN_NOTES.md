# UI Redesign Notes — feeds Slices 5-7, not a standalone audit
Captured 2026-08-04. Verified findings only — see each item's evidence.
Apply within existing brand tokens (Racing Teal / Exo 2 / Nunito Sans /
JetBrains Mono) and existing premium components. No new design system.

## PRD.md vs shipped reality — VERIFIED conflicts (grep'd directly)
- PRD.md:143 says "Bottom Navigation: 3 tabs ONLY — Dashboard | Vehicles |
  Settings. ServiceHistory lives inside VehicleDetail." Shipped app has 4
  tabs (Home/Vehicles/Records/Settings), Records is top-level. CLAUDE.md is
  authoritative here — PRD.md is stale on this point.
- PRD.md:384 lists Fuel Intelligence as v1.0 scope ("P9 ... YES"). CLAUDE.md
  defers it to v1.1. CLAUDE.md wins.
- PRD.md:20,459 carry unsourced market stats (91.8% neglect, 69M
  breakdowns/$44B). No primary source found. Don't cite these in store
  copy or Data Safety materials without a real source; treat as unverified
  marketing color, not product requirements.
PRD.md itself was not restructured — this file is a pointer to the drift,
not a fix. Reconcile PRD.md against CLAUDE.md when someone is actually
touching that document, not as a separate task.

## Screen diagnosis — from screens actually rendered this session (AVD)
Not inferred from a description — I drove the live app and looked.

- **Vehicle Detail health ring**: showed a bare "0" beside "7 services need
  attention" with no legible relationship between the two numbers. The
  ring communicates worse than the plain-language headline next to it.
  Matches HANDOFF.md Slice 8 nit list already (demote/replace the ring).
- **Demo data credibility**: "Overdue by 173,000 km" is real, rendered
  data, not hypothetical — already tracked as HANDOFF.md Slice 8 nit #7
  (reseed before store screenshots). This should probably move earlier,
  not stay a final-polish item, since it undermines every screenshot taken
  for QA in the meantime.
- **Vehicle identity is generic**: a stock car icon stands in for the
  actual vehicle on both Vehicles list and Vehicle Detail. No photo,
  silhouette, or distinguishing mark — every vehicle looks the same card.
- **Uniform card weight**: most surfaces are pale rounded rectangles of
  similar visual weight — status, actions, and metadata don't compete
  for attention the way they should. Consistent with CLAUDE.md's own
  "no equal-weight layouts" rule not yet being fully applied.

## Direction for Slices 5-7 (apply, don't re-audit)
- Vehicle identity: add a real distinguishing element per vehicle (photo
  if set, else a stronger silhouette/initial treatment) — both list and
  detail.
- Vehicle Detail: demote or remove the bare health-ring number; lead with
  the plain-language diagnosis headline (already partly done — verify against
  current `HealthCockpitCard` usage before adding anything new).
- Reduce card-background repetition: not every section needs a filled
  rounded surface — plain grouped sections / dividers / tonal bands for
  secondary content, reserve strong card treatment for the one hero per
  screen (already the stated rule; this is an enforcement gap, not a new
  rule).
- Log Service: 16 equal-weight service tiles is real scanning friction —
  worth a "recently used + common 6, rest behind View all" pattern in
  Slice 6 (guided forms), consistent with the low-typing product law
  already in CLAUDE.md.

## Explicitly out of scope for v1.0
Vehicle marketplace-style imagery, a second component library, any
palette/font change, gamification (streaks/eco-score), and anything from
the two Behance references beyond hierarchy/whitespace principles — no
booking flows, no marketplace patterns, no new brand identity.
