package com.autominder.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.ui.util.DateFormatUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

class ExportServiceHistoryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceRepository: IServiceRepository,
    private val vehicleRepository: IVehicleRepository
) {
    /**
     * Exports raw CSV structured data for spreadsheet analysis or backup.
     */
    suspend operator fun invoke(vehicleId: Long? = null): Uri {
        val (services, fileName, includeVehicleCol) = if (vehicleId != null) {
            val s = serviceRepository.getServicesForVehicle(vehicleId).first()
            val v = vehicleRepository.getVehicleById(vehicleId).first()
            val safeMake = v?.make?.replace(Regex("[^A-Za-z0-9]"), "_") ?: "Vehicle"
            val safeModel = v?.model?.replace(Regex("[^A-Za-z0-9]"), "_") ?: "Model"
            Triple(s, "${safeMake}_${safeModel}_service_history.csv", false)
        } else {
            val s = serviceRepository.getAllServices().first()
            Triple(s, "AutoMinder_Fleet_service_history.csv", true)
        }

        val allVehicles = vehicleRepository.getAllVehiclesIncludingArchived().first()
        val vehicleNameMap = allVehicles.associate { it.id to "${it.make} ${it.model}" }

        val file = File(context.cacheDir, fileName)

        file.bufferedWriter().use { writer ->
            if (includeVehicleCol) {
                writer.write("Date,Vehicle,Service Type,Odometer (km),Cost,Shop,Notes\n")
            } else {
                writer.write("Date,Service Type,Odometer (km),Cost,Shop,Notes\n")
            }

            services.forEach { service ->
                val cost = service.costCents?.let { "%.2f".format(it / 100.0) } ?: ""
                val shop = service.shopName?.replace(",", ";") ?: ""
                val notes = service.notes.replace(",", ";")
                val date = DateFormatUtil.formatDate(service.serviceDate)
                val label = service.customLabel ?: service.serviceType.label
                if (includeVehicleCol) {
                    val vehName = vehicleNameMap[service.vehicleId] ?: "Unknown Vehicle"
                    writer.write("$date,$vehName,$label,${service.odometerAtService},$cost,$shop,$notes\n")
                } else {
                    writer.write("$date,$label,${service.odometerAtService},$cost,$shop,$notes\n")
                }
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    /**
     * Generates an official Certified Vehicle Maintenance Passport.
     * Used by vehicle owners to prove meticulous maintenance history for resale or insurance certification.
     */
    suspend fun exportPassport(vehicleId: Long? = null): Uri {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val (services, vehicle, fileName) = if (vehicleId != null) {
            val s = serviceRepository.getServicesForVehicle(vehicleId).first()
            val v = vehicleRepository.getVehicleById(vehicleId).first()
            val safeMake = v?.make?.replace(Regex("[^A-Za-z0-9]"), "_") ?: "Vehicle"
            val safeModel = v?.model?.replace(Regex("[^A-Za-z0-9]"), "_") ?: "Model"
            Triple(s, v, "${safeMake}_${safeModel}_Maintenance_Passport.txt")
        } else {
            val s = serviceRepository.getAllServices().first()
            Triple(s, null, "AutoMinder_Fleet_Maintenance_Passport.txt")
        }

        val allVehicles = vehicleRepository.getAllVehiclesIncludingArchived().first()
        val vehicleNameMap = allVehicles.associate { it.id to "${it.make} ${it.model}" }

        val file = File(context.cacheDir, fileName)
        val sortedServices = services.sortedByDescending { it.serviceDate }
        val totalCostCents = services.mapNotNull { it.costCents }.sum()
        val categoryBreakdown = services.groupBy { it.serviceType }

        file.bufferedWriter().use { writer ->
            writer.write("================================================================================\n")
            writer.write("           AUTOMINDER CERTIFIED VEHICLE MAINTENANCE PASSPORT\n")
            writer.write("================================================================================\n\n")

            if (vehicle != null) {
                writer.write("--- VEHICLE SPECIFICATIONS ---\n")
                writer.write("Make & Model    : ${vehicle.make} ${vehicle.model}\n")
                writer.write("Year            : ${if (vehicle.year > 0) vehicle.year.toString() else "N/A"}\n")
                writer.write("License Plate   : ${if (vehicle.plateNumber.isNotBlank()) vehicle.plateNumber else "N/A"}\n")
                if (!vehicle.vin.isNullOrBlank()) {
                    writer.write("VIN             : ${vehicle.vin}\n")
                }
                writer.write("Current Odometer: %,d km\n\n".format(vehicle.currentOdometer))
            } else {
                writer.write("--- FLEET OVERVIEW ---\n")
                writer.write("Total Vehicles  : ${allVehicles.size}\n\n")
            }

            writer.write("--- MAINTENANCE INVESTMENT SUMMARY ---\n")
            writer.write("Total Documented Services : ${services.size}\n")
            writer.write("Total Documented Spend    : ${currencyFormat.format(totalCostCents / 100.0)}\n")
            if (services.isNotEmpty() && totalCostCents > 0) {
                writer.write("Average Cost per Service  : ${currencyFormat.format((totalCostCents / services.size) / 100.0)}\n")
            }
            writer.write("\nCategory Breakdown:\n")
            categoryBreakdown.forEach { (type, list) ->
                val typeSpend = list.mapNotNull { it.costCents }.sum()
                writer.write("  • %-18s : %2d records  (%s)\n".format(
                    type.label,
                    list.size,
                    currencyFormat.format(typeSpend / 100.0)
                ))
            }
            writer.write("\n")

            writer.write("================================================================================\n")
            writer.write("                       CHRONOLOGICAL SERVICE RECORDS\n")
            writer.write("================================================================================\n\n")

            if (sortedServices.isEmpty()) {
                writer.write("No service records logged yet.\n\n")
            } else {
                sortedServices.forEachIndexed { index, service ->
                    val date = DateFormatUtil.formatDate(service.serviceDate)
                    val label = service.customLabel ?: service.serviceType.label
                    val cost = service.costCents?.let { currencyFormat.format(it / 100.0) } ?: "Not recorded"
                    val vehName = vehicleNameMap[service.vehicleId] ?: "Vehicle"

                    writer.write("[Record #${services.size - index}] $date — $label\n")
                    if (vehicle == null) {
                        writer.write("  Vehicle   : $vehName\n")
                    }
                    writer.write("  Odometer  : %,d km\n".format(service.odometerAtService))
                    writer.write("  Cost      : $cost\n")
                    if (!service.shopName.isNullOrBlank()) {
                        writer.write("  Workshop  : ${service.shopName}\n")
                    }
                    if (service.notes.isNotBlank()) {
                        writer.write("  Notes     : ${service.notes}\n")
                    }
                    writer.write("--------------------------------------------------------------------------------\n")
                }
            }

            writer.write("\nGenerated by AutoMinder — Verified Local Offline Records\n")
            writer.write("Timestamp: ${DateFormatUtil.formatDate(System.currentTimeMillis())}\n")
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
