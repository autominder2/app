/**
 * Maintenance schedule engine — a direct port of AutoMinder's
 * CreateDefaultRemindersUseCase.buildPlan() (Kotlin, app/src/main/kotlin/
 * com/autominder/app/domain/usecase/CreateDefaultRemindersUseCase.kt).
 *
 * Runs entirely in the browser. No network call is made with the odometer
 * value or anything the visitor types — that claim on the page is only true
 * because this file never calls fetch/XHR. Keep it that way.
 */
(function () {
    'use strict';

    var DAY_MS = 86400000;
    var KM_PER_MILE = 1.609344;

    var DRIVING_AMOUNTS = {
        LOW: { annualKm: 8000, label: 'Low', hint: '~5,000 mi/yr' },
        TYPICAL: { annualKm: 16000, label: 'Typical', hint: '~10,000 mi/yr' },
        HIGH: { annualKm: 28000, label: 'High', hint: '~17,500 mi/yr' }
    };

    var STANDARD_TEMPLATES = [
        { type: 'OIL_CHANGE', label: 'Oil Change', intervalKm: 8000, intervalDays: 180 },
        { type: 'TIRE_ROTATION', label: 'Tire Rotation', intervalKm: 12000, intervalDays: 365 },
        { type: 'AIR_FILTER', label: 'Air Filter', intervalKm: 20000, intervalDays: 365 },
        { type: 'CABIN_FILTER', label: 'Cabin Filter', intervalKm: 20000, intervalDays: 365 },
        { type: 'BRAKE_SERVICE', label: 'Brake Service', intervalKm: 40000, intervalDays: 730 },
        { type: 'WIPER_BLADES', label: 'Wiper Blades', intervalKm: 15000, intervalDays: 365 }
    ];

    var COOLANT_TEMPLATE = { type: 'COOLANT', label: 'Coolant', intervalKm: 40000, intervalDays: 730 };
    var TRANSMISSION_TEMPLATE = { type: 'TRANSMISSION', label: 'Transmission', intervalKm: 60000, intervalDays: 1460 };
    var COOLANT_FROM_KM = 80000;
    var TRANSMISSION_FROM_KM = 100000;

    /**
     * @param {number} currentOdometerKm - 0 means "no reading yet" (same
     *   sentinel the app uses) — the plan is date-driven only in that case.
     * @param {string} drivingAmountKey - LOW | TYPICAL | HIGH
     * @param {number} nowMillis
     * @returns {Array} planned reminders, soonest first
     */
    function buildPlan(currentOdometerKm, drivingAmountKey, nowMillis) {
        var driving = DRIVING_AMOUNTS[drivingAmountKey] || DRIVING_AMOUNTS.TYPICAL;
        var hasOdometerReading = currentOdometerKm > 0;

        var templates = STANDARD_TEMPLATES.slice();
        if (currentOdometerKm >= COOLANT_FROM_KM) templates.push(COOLANT_TEMPLATE);
        if (currentOdometerKm >= TRANSMISSION_FROM_KM) templates.push(TRANSMISSION_TEMPLATE);

        var dailyKm = driving.annualKm / 365.0;

        var plan = templates.map(function (t) {
            var daysToReachKm = Math.max(1, Math.floor(t.intervalKm / dailyKm));
            var effectiveDays = Math.min(t.intervalDays, daysToReachKm);
            return {
                type: t.type,
                label: t.label,
                intervalKm: t.intervalKm,
                intervalDays: t.intervalDays,
                nextDueOdometerKm: hasOdometerReading ? currentOdometerKm + t.intervalKm : null,
                nextDueDate: nowMillis + effectiveDays * DAY_MS
            };
        });

        plan.sort(function (a, b) {
            if (a.nextDueDate !== b.nextDueDate) return a.nextDueDate - b.nextDueDate;
            var aOdo = a.nextDueOdometerKm === null ? Infinity : a.nextDueOdometerKm;
            var bOdo = b.nextDueOdometerKm === null ? Infinity : b.nextDueOdometerKm;
            return aOdo - bOdo;
        });

        return plan;
    }

    window.AutoMinderSchedule = {
        buildPlan: buildPlan,
        DRIVING_AMOUNTS: DRIVING_AMOUNTS,
        KM_PER_MILE: KM_PER_MILE
    };
})();
