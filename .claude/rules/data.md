---
paths:
  - "app/src/main/kotlin/com/autominder/app/data/**/*.kt"
  - "app/src/main/kotlin/com/autominder/app/domain/**/*.kt"
  - "app/src/main/kotlin/com/autominder/app/worker/**/*.kt"
  - "app/schemas/**"
---

# AutoMinder Data & Domain Rules (loads when data/domain/worker files are touched)

## Reads & writes
- Reactive UI reads return Flow<T> — suspend reads for observed data are WRONG.
- One-shot reads (WorkManager, export, migrations) are suspend fun getXOnce()
  — the "Once" suffix is mandatory so the two paths never blur.
- ALL DB writes are suspend fun. Multi-table writes require @Transaction.
- Repository/UseCase failures return sealed Result<T, AppError> — no raw
  exceptions crossing that boundary.
- Default sharing for observable screen state backed by expensive flows:
  stateIn(WhileSubscribed(5_000L)). Don't wrap one-shot results or events in
  StateFlow to satisfy the pattern; events must never replay as state.

## Coroutines
- viewModelScope (Main) is correct for UI-state coordination.
- Blocking I/O never on Main; the blocking class is responsible for
  main-safety via an injected dispatcher (IO blocking, Default CPU).
- No unmanaged CoroutineScope; work outliving a screen uses an injected
  application scope or WorkManager.

## Room
- exportSchema = true always. NEVER fallbackToDestructiveMigration().
- Every schema change ships a Migration object + MigrationTest coverage from
  every shipped schema version.
- Check existing @Entity classes before adding one — never duplicate.
- @PrimaryKey(autoGenerate = true) is Long, never Int.

## Domain invariants
- Money is Int cents only — never Double/Float; display as cents / 100.0.
- StatusCalculator: OVERDUE always beats SNOOZED; raw status computed FIRST.
  StatusCalculatorTest is the authority — change it only with explicit
  approval. Algorithm prose: docs/GOVERNANCE_REFERENCE.md.
- Notification cooldown matrix: docs/GOVERNANCE_REFERENCE.md. Never notify
  GOOD/SNOOZED/DISABLED. Never duplicate a notification action because a
  process or screen was recreated.
