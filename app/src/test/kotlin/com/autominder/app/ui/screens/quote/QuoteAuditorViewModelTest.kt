package com.autominder.app.ui.screens.quote

import androidx.lifecycle.SavedStateHandle
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.QuoteAuditResult
import com.autominder.app.domain.model.QuoteItem
import com.autominder.app.domain.model.QuoteLineVerdict
import com.autominder.app.domain.model.QuoteVerdictStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import com.autominder.app.domain.usecase.AuditQuoteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteAuditorViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var vehicleRepo: IVehicleRepository
    private lateinit var serviceRepo: IServiceRepository
    private lateinit var auditQuoteUseCase: AuditQuoteUseCase
    private lateinit var userPrefs: UserPreferences
    private lateinit var savedStateHandle: SavedStateHandle

    private val vehicle = Vehicle(id = 1L, make = "Mazda", model = "CX-5", year = 2022, plateNumber = "MZD-555", currentOdometer = 60000)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vehicleRepo = mockk(relaxed = true)
        serviceRepo = mockk(relaxed = true)
        auditQuoteUseCase = mockk(relaxed = true)
        userPrefs = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to 1L))

        every { vehicleRepo.getAllVehicles() } returns flowOf(listOf(vehicle))
        every { userPrefs.distanceUnit } returns flowOf("km")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = QuoteAuditorViewModel(
        vehicleRepository = vehicleRepo,
        serviceRepository = serviceRepo,
        auditQuoteUseCase = auditQuoteUseCase,
        userPreferences = userPrefs,
        savedStateHandle = savedStateHandle
    )

    @Test
    fun `loadInitialData selects vehicle from initialVehicleId`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(vehicle, state.selectedVehicle)
        assertEquals("km", state.distanceUnit)
        assertEquals(0, state.items.size)
    }

    @Test
    fun `AddItem appends quote item and triggers audit analysis`() = runTest(dispatcher) {
        val auditResult = QuoteAuditResult(
            vehicleName = "Mazda CX-5",
            currentOdometer = 60000,
            lineVerdicts = listOf(
                QuoteLineVerdict(
                    item = QuoteItem(id = "1", serviceType = ServiceType.BRAKE_SERVICE, priceCents = 25000),
                    status = QuoteVerdictStatus.LEGITIMATE_DUE,
                    reason = "Due at 60k km",
                    questionToAsk = "Ask about pad thickness",
                    fairPriceRangeCents = 18000..28000
                )
            ),
            totalQuotedCents = 25000,
            fairPriceMinCents = 18000,
            fairPriceMaxCents = 28000,
            potentialSavingsCents = 0,
            legitimateItemsCount = 1,
            upsellItemsCount = 0,
            verifyItemsCount = 0,
            mechanicTalkingPoints = listOf("Ask for brake pad measurements")
        )

        coEvery { auditQuoteUseCase(1L, any()) } returns auditResult

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(QuoteAuditorUiEvent.AddItem(ServiceType.BRAKE_SERVICE, 25000))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(ServiceType.BRAKE_SERVICE, state.items.first().serviceType)
        assertNotNull(state.auditResult)
        assertEquals(1, state.auditResult!!.legitimateItemsCount)
    }

    @Test
    fun `ResetQuote clears all items and resets audit result`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(QuoteAuditorUiEvent.AddItem(ServiceType.OIL_CHANGE, 7000))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.items.size)

        viewModel.onEvent(QuoteAuditorUiEvent.ResetQuote)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals(null, state.auditResult)
    }

    @Test
    fun `SaveApprovedServices saves only legitimate and verify services into repository`() = runTest(dispatcher) {
        val legitItem = QuoteItem(id = "1", serviceType = ServiceType.OIL_CHANGE, priceCents = 6000)
        val upsellItem = QuoteItem(id = "2", serviceType = ServiceType.TRANSMISSION, priceCents = 35000)

        val auditResult = QuoteAuditResult(
            vehicleName = "Mazda CX-5",
            currentOdometer = 60000,
            lineVerdicts = listOf(
                QuoteLineVerdict(
                    item = legitItem,
                    status = QuoteVerdictStatus.LEGITIMATE_DUE,
                    reason = "Oil due",
                    questionToAsk = "",
                    fairPriceRangeCents = 5000..8000
                ),
                QuoteLineVerdict(
                    item = upsellItem,
                    status = QuoteVerdictStatus.LIKELY_UPSELL,
                    reason = "Transmission fluid was done 10k km ago",
                    questionToAsk = "",
                    fairPriceRangeCents = 20000..30000
                )
            ),
            totalQuotedCents = 41000,
            fairPriceMinCents = 25000,
            fairPriceMaxCents = 38000,
            potentialSavingsCents = 35000,
            legitimateItemsCount = 1,
            upsellItemsCount = 1,
            verifyItemsCount = 0,
            mechanicTalkingPoints = emptyList()
        )

        coEvery { auditQuoteUseCase(1L, any()) } returns auditResult
        coEvery { serviceRepo.insertService(any()) } returns 10L

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(QuoteAuditorUiEvent.AddItem(ServiceType.OIL_CHANGE, 6000))
        viewModel.onEvent(QuoteAuditorUiEvent.AddItem(ServiceType.TRANSMISSION, 35000))
        advanceUntilIdle()

        viewModel.onEvent(QuoteAuditorUiEvent.SaveApprovedServices)
        advanceUntilIdle()

        // Only the legitimate service should be inserted, not the upsell!
        coVerify(exactly = 1) {
            serviceRepo.insertService(match {
                it.serviceType == ServiceType.OIL_CHANGE && it.vehicleId == 1L
            })
        }
        assertNotNull(viewModel.uiState.value.savedSuccessMessage)
    }
}
