package com.autominder.app.domain.model

/**
 * Status of a maintenance reminder — drives UI chip color & sort order.
 *
 * Business rules (PRD Section 7.1):
 *   OVERDUE check ALWAYS before SNOOZED check.
 *   Priority: OVERDUE > DUE_SOON > SNOOZED > OK > COMPLETED > UNKNOWN
 */
enum class ServiceStatus(val severity: Int) {
    OVERDUE(5),
    DUE_SOON(4),
    SNOOZED(3),
    OK(2),
    COMPLETED(1),
    UNKNOWN(0);
}
