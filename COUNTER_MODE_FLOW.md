# Counter Mode — Flow & State Specification (Gate B design)
Status: DESIGN ONLY — implementation blocked until Gate A is green.
Skills applied: design-critique, design-system, accessibility-review (loaded this session).
Previews: Figma MCP is rate-limited on the current plan; visual previews will be produced either at the next rate-limit window or as real screenshots from the implemented UI at Gate B. No previews are claimed to exist yet.

## Flow (offline-complete, no OCR, no LLM)

ENTRY (2 doors)
├─ Home → "At the workshop?" action (FAB menu item + persistent quiet entry on Dashboard)
└─ Reminder detail → "Getting this serviced? Check the quote"

STEP 1 — CAPTURE            state: empty/manual-input
  Paste text | Add line items manually | (Android share-sheet text intake)
  Demo chip: "Try with a sample quote" (zero-history users)
  Each line: description · qty · unit price · type (diagnosis fee / labor hrs+rate /
  parts / shop supplies / tax / warranty / recommended service)
  Draft auto-saved on every keystroke (process-death safe).

STEP 2 — TRIAGE             state: parsing/progress → triage (<250ms rules budget)
  Per-line label (exactly one):
   · SAFETY REVIEW      "Brake-related. We don't judge safety — worth a professional's answer."
   · DUE FROM HISTORY   "Matches your interval — last done 14,200 mi ago."
   · RECENTLY DONE      "You logged this 2,400 mi ago. Ask why it's needed again."
   · NEEDS EVIDENCE     "No history for this item. Ask what they found."
   · DISCUSS            "Commonly optional at this mileage. Your call."
  Neutral language everywhere. Never: scam, fraud, rip-off, safe-to-ignore.
  Low-evidence state: if history is empty → banner "Your history is empty, so most
  items show as Needs evidence. That's honest, not alarming."

STEP 3 — SHOW THE MECHANIC  state: one-question-at-a-time, large type, offline
  One card per flagged line: the question, the evidence line ("You have a receipt
  from Mar 12, 41,300 mi"), and 4 actions:
   [Asked] [Answer recorded] [Approved] [Deferred] (+ overflow: Get second opinion)
  Safety-review items: Approve / Ask only — NO defer action rendered. Ever.
  48dp+ targets, font scales to 200%, works with screen off/on interruptions.

STEP 4 — DECISION CONFIRM   state: decision confirmation
  Summary: N approved · N deferred · N second-opinion.
  "Approved items become a DRAFT service record you review before it enters
  your history. Deferred items become follow-up reminders with a date."
  Nothing writes final history silently.

RECOVERY STATES
  · interrupted → reopening app lands back on the exact step, banner "Picking up
    where you left off"
  · offline → full function (rules are local); banner only if a future AI tier is on
  · error/parse failure → "Couldn't read that — add the lines by hand" + manual editor
  · process death → draft restored from Room, zero loss

## Data (Gate C schema, designed now, built later)
quotes(id, vehicleId, createdAt, status[draft|triaged|decided|reconciled], rawTextHash)
quote_lines(id, quoteId, kind[diag|labor|part|supplies|tax|warranty|recommended],
            description, qty, unitCents, label[safety|due|duplicate|evidence|discuss])
decisions(id, quoteLineId, choice[approved|deferred|second_opinion|asked],
          mechanicAnswer?, decidedAt)  — explicit state transitions only

## Navigation
No 5th tab. Entry via Dashboard FAB + reminder detail. Back = predictive-back safe,
draft preserved. Route: NavRoutes.CounterMode(vehicleId, quoteId?) — @Serializable.

## Copy principles
Calm, second person, consequence-first, no exclamation marks, no jargon without a
plain-word gloss, every claim traceable to the user's own data or a labeled default.
