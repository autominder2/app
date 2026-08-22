package com.autominder.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class ExportServiceHistoryUseCaseTest {

    private lateinit var context: Context
    private lateinit var serviceRepository: IServiceRepository
    private lateinit var vehicleRepository: IVehicleRepository
    private lateinit var useCase: ExportServiceHistoryUseCase
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "autominder_test_cache")
        tempDir.mkdirs()

        context = mockk(relaxed = true)
        every { context.cacheDir } returns tempDir
        every { context.packageName } returns "com.autominder.app"

        serviceRepository = mockk(relaxed = true)
        vehicleRepository = mockk(relaxed = true)

        mockkStatic(FileProvider::class)
        val mockUri = mockk<Uri>(relaxed = true)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockUri

        useCase = ExportServiceHistoryUseCase(context, serviceRepository, vehicleRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(FileProvider::class)
        tempDir.deleteRecursively()
    }

    @Test
    fun `invoke exports CSV for single vehicle with correct headers and rows`() = runTest {
        val vehicle = Vehicle(id = 1L, make = "Honda", model = "Civic", year = 2021, plateNumber = "HND-101", currentOdometer = 35000)
        val service = Service(
            id = 10L,
            vehicleId = 1L,
            serviceType = ServiceType.OIL_CHANGE,
            odometerAtService = 30000,
            serviceDate = 1700000000000L,
            costCents = 6500,
            shopName = "Honda Care",
            notes = "Full synthetic 0W-20"
        )

        every { vehicleRepository.getVehicleById(1L) } returns flowOf(vehicle)
        every { vehicleRepository.getAllVehiclesIncludingArchived() } returns flowOf(listOf(vehicle))
        every { serviceRepository.getServicesForVehicle(1L) } returns flowOf(listOf(service))

        val resultUri = useCase(vehicleId = 1L)
        assertNotNull(resultUri)

        val exportedFile = File(tempDir, "Honda_Civic_service_history.csv")
        assertTrue(exportedFile.exists())

        val content = exportedFile.readText()
        assertTrue(content.startsWith("Date,Service Type,Odometer (km),Cost,Shop,Notes"))
        assertTrue(content.contains("Oil Change"))
        assertTrue(content.contains("30000"))
        assertTrue(content.contains("65.00"))
        assertTrue(content.contains("Honda Care"))
        assertTrue(content.contains("Full synthetic 0W-20"))
    }

    @Test
    fun `exportPassport creates formatted Certified Vehicle Maintenance Passport`() = runTest {
        val vehicle = Vehicle(id = 1L, make = "Toyota", model = "RAV4", year = 2023, plateNumber = "RAV-777", vin = "4T3BF1EK5PU123456", currentOdometer = 25000)
        val service1 = Service(
            id = 10L,
            vehicleId = 1L,
            serviceType = ServiceType.OIL_CHANGE,
            odometerAtService = 10000,
            serviceDate = 1690000000000L,
            costCents = 8000,
            shopName = "Toyota Dealership",
            notes = "Scheduled 10k service"
        )
        val service2 = Service(
            id = 11L,
            vehicleId = 1L,
            serviceType = ServiceType.TIRE_ROTATION,
            odometerAtService = 20000,
            serviceDate = 1700000000000L,
            costCents = 4000,
            shopName = "Tire Shop",
            notes = "Cross rotation"
        )

        every { vehicleRepository.getVehicleById(1L) } returns flowOf(vehicle)
        every { vehicleRepository.getAllVehiclesIncludingArchived() } returns flowOf(listOf(vehicle))
        every { serviceRepository.getServicesForVehicle(1L) } returns flowOf(listOf(service1, service2))

        val resultUri = useCase.exportPassport(vehicleId = 1L)
        assertNotNull(resultUri)

        val exportedFile = File(tempDir, "Toyota_RAV4_Maintenance_Passport.txt")
        assertTrue(exportedFile.exists())

        val content = exportedFile.readText()
        assertTrue(content.contains("AUTOMINDER CERTIFIED VEHICLE MAINTENANCE PASSPORT"))
        assertTrue(content.contains("Toyota RAV4"))
        assertTrue(content.contains("4T3BF1EK5PU123456"))
        assertTrue(content.contains("Total Documented Services : 2"))
        assertTrue(content.contains("Oil Change"))
        assertTrue(content.contains("Tire Rotation"))
        assertTrue(content.contains("Toyota Dealership"))
    }
}
