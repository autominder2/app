package com.autominder.app.ui.navigation

import kotlinx.serialization.Serializable

// ─── NavRoute sealed hierarchy ───────────────────────────────────────────────
// PRD Section 7.2 — 14 routes total.
// NEVER use raw strings in navigate() — only these type-safe objects.

sealed class NavRoutes {

    // ── Bottom Nav ────────────────────────────────────────────────────────
    @Serializable
    object Dashboard : NavRoutes()

    @Serializable
    object VehicleList : NavRoutes()

    @Serializable
    object ServiceHistory : NavRoutes()

    @Serializable
    object Settings : NavRoutes()

    // ── Vehicle Flow ──────────────────────────────────────────────────────
    @Serializable
    data class VehicleDetail(val vehicleId: Long) : NavRoutes()

    @Serializable
    object AddVehicle : NavRoutes()

    @Serializable
    data class EditVehicle(val vehicleId: Long) : NavRoutes()

    // ── Reminder / Service Flow ───────────────────────────────────────────
    @Serializable
    data class AddReminder(val vehicleId: Long) : NavRoutes()

    @Serializable
    data class EditReminder(val reminderId: Long) : NavRoutes()

    @Serializable
    data class ServiceDetail(val serviceId: Long) : NavRoutes()

    @Serializable
    data class AddService(val vehicleId: Long) : NavRoutes()

    // ── Mileage Log ───────────────────────────────────────────────────────
    @Serializable
    data class MileageLog(val vehicleId: Long) : NavRoutes()

    // ── Fuel Log ──────────────────────────────────────────────────────────
    @Serializable
    data class AddFuel(val vehicleId: Long) : NavRoutes()

    @Serializable
    data class FuelHistory(val vehicleId: Long) : NavRoutes()

    // ── Utility ───────────────────────────────────────────────────────────
    @Serializable
    object About : NavRoutes()

    @Serializable
    object Onboarding : NavRoutes()
}
