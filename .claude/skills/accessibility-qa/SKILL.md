---
description: Use to audit AutoMinder UI for Compose accessibility, TalkBack semantics, large text scaling, touch targets, status clarity, and reduced motion.
allowed-tools: Read, Grep, Glob, Bash
effort: high
---

# AutoMinder Accessibility QA

Read-only audit. Run after each screen redesign slice.

## Requirements

1. Touch targets:
   - Interactive items ≥ 48dp (56dp for primary action tiles).

2. Status:
   - Never color-only. Status must be visible in text/icon/chip + shape/accent rail.
   - Overdue / due soon / good must remain meaningful in grayscale.

3. Semantics:
   - Vehicle cards merge into one useful TalkBack sentence (`semantics(mergeDescendants = true)` + composed contentDescription).
   - Reminder cards announce service name, status, due condition, and actions.
   - Decorative icons: `contentDescription = null`.
   - Semantic icons/actions: explicit contentDescription from strings.xml (`cd_*` naming convention).
   - Section titles use `heading()` semantics.

4. Large text:
   - Test 1.5x and 2.0x on the AVD where safe (`settings put system font_scale` — record original, restore after, verify restore).
   - No clipped hero titles (maxLines + ellipsis over photos).
   - No fixed heights that break; prefer `IntrinsicSize` patterns.

5. Forms:
   - Error text adjacent to its field and visible after scroll.
   - Traversal order matches visual order.
   - SaveButton state (Idle/Saving/Success) clear to TalkBack.

6. Motion:
   - `Motion.reduceMotion` respected (collapses to `snap()`; parallax/slide amplitude drops to 0).
   - No bouncing/pulsing urgent warnings.

## Output

- Accessibility verdict (pass / fail with severity).
- TalkBack risks (file:line).
- Large text risks.
- Touch target risks.
- Fix plan.
- Screenshot paths if captured (to `D:\tmp\autominder-qa\`).
