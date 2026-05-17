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
import javax.inject.Inject

class ExportServiceHistoryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceRepository: IServiceRepository,
    private val vehicleRepository: IVehicleRepository
) {
    suspend operator fun invoke(vehicleId: Long): Uri {
        val services = serviceRepository.getServicesForVehicle(vehicleId).first()
        val vehicle = vehicleRepository.getVehicleById(vehicleId).first()

        val safeMake = vehicle?.make?.replace(Regex("[^A-Za-z0-9]"), "_") ?: "Vehicle"
        val safeModel = vehicle?.model?.replace(Regex("[^A-Za-z0-9]"), "_") ?: "Model"
        val fileName = "${safeMake}_${safeModel}_service_history.csv"
        val file = File(context.cacheDir, fileName)

        file.bufferedWriter().use { writer ->
            writer.write("Date,Service Type,Odometer (km),Cost,Shop,Notes\n")
            services.forEach { service ->
                val cost = service.costCents?.let { "%.2f".format(it / 100.0) } ?: ""
                val shop = service.shopName?.replace(",", ";") ?: ""
                val notes = service.notes.replace(",", ";")
                val date = DateFormatUtil.formatDate(service.serviceDate)
                val label = service.customLabel ?: service.serviceType.label
                writer.write("$date,$label,${service.odometerAtService},$cost,$shop,$notes\n")
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
