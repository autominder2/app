package com.autominder.app.core.di

import com.autominder.app.data.repository.FuelRepositoryImpl
import com.autominder.app.data.repository.MileageLogRepositoryImpl
import com.autominder.app.data.repository.ReminderRepositoryImpl
import com.autominder.app.data.repository.ServiceRepositoryImpl
import com.autominder.app.data.repository.VehicleRepositoryImpl
import com.autominder.app.domain.repository.IFuelRepository
import com.autominder.app.domain.repository.IMileageLogRepository
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IServiceRepository
import com.autominder.app.domain.repository.IVehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): IVehicleRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        reminderRepositoryImpl: ReminderRepositoryImpl
    ): IReminderRepository

    @Binds
    @Singleton
    abstract fun bindServiceRepository(
        serviceRepositoryImpl: ServiceRepositoryImpl
    ): IServiceRepository

    @Binds
    @Singleton
    abstract fun bindMileageLogRepository(
        mileageLogRepositoryImpl: MileageLogRepositoryImpl
    ): IMileageLogRepository

    @Binds
    @Singleton
    abstract fun bindFuelRepository(
        fuelRepositoryImpl: FuelRepositoryImpl
    ): IFuelRepository
}
