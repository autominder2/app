package com.autominder.app.domain.util

import com.autominder.app.domain.model.VehicleBodyType
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleBodyTypeResolverTest {

    @Test
    fun `resolves pickup trucks accurately`() {
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Ford", "F-150"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Ford", "F150 Lariat"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Chevrolet", "Silverado 1500"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Toyota", "Tacoma TRD"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Toyota", "Tundra"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Ram", "1500"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Tesla", "Cybertruck"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Nissan", "Frontier"))
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("Honda", "Ridgeline"))
    }

    @Test
    fun `resolves SUVs and crossovers accurately`() {
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Toyota", "RAV4 Hybrid"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Honda", "CR-V"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Tesla", "Model Y"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("BMW", "X5 xDrive40i"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Ford", "Explorer"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Subaru", "Forester"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Jeep", "Grand Cherokee"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Hyundai", "Palisade"))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("Mazda", "CX-5"))
    }

    @Test
    fun `resolves convertibles and roadsters accurately`() {
        assertEquals(VehicleBodyType.CONVERTIBLE, VehicleBodyTypeResolver.resolve("Mazda", "Miata"))
        assertEquals(VehicleBodyType.CONVERTIBLE, VehicleBodyTypeResolver.resolve("Mazda", "MX-5"))
        assertEquals(VehicleBodyType.CONVERTIBLE, VehicleBodyTypeResolver.resolve("Porsche", "Boxster"))
        assertEquals(VehicleBodyType.CONVERTIBLE, VehicleBodyTypeResolver.resolve("BMW", "Z4"))
        assertEquals(VehicleBodyType.CONVERTIBLE, VehicleBodyTypeResolver.resolve("Porsche", "911 Cabriolet"))
        assertEquals(VehicleBodyType.CONVERTIBLE, VehicleBodyTypeResolver.resolve("Alfa Romeo", "Spider"))
    }

    @Test
    fun `resolves coupes accurately`() {
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("Ford", "Mustang GT"))
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("Dodge", "Challenger"))
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("Chevrolet", "Camaro SS"))
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("Subaru", "BRZ"))
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("Toyota", "GR86"))
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("Toyota", "Supra"))
        assertEquals(VehicleBodyType.COUPE, VehicleBodyTypeResolver.resolve("BMW", "M4"))
    }

    @Test
    fun `resolves hatchbacks accurately`() {
        assertEquals(VehicleBodyType.HATCHBACK, VehicleBodyTypeResolver.resolve("Volkswagen", "Golf GTI"))
        assertEquals(VehicleBodyType.HATCHBACK, VehicleBodyTypeResolver.resolve("Honda", "Fit"))
        assertEquals(VehicleBodyType.HATCHBACK, VehicleBodyTypeResolver.resolve("Toyota", "Yaris"))
        assertEquals(VehicleBodyType.HATCHBACK, VehicleBodyTypeResolver.resolve("Nissan", "Leaf"))
        assertEquals(VehicleBodyType.HATCHBACK, VehicleBodyTypeResolver.resolve("Fiat", "500"))
        assertEquals(VehicleBodyType.HATCHBACK, VehicleBodyTypeResolver.resolve("Hyundai", "Ioniq 5"))
    }

    @Test
    fun `resolves minivans accurately`() {
        assertEquals(VehicleBodyType.MINIVAN, VehicleBodyTypeResolver.resolve("Toyota", "Sienna"))
        assertEquals(VehicleBodyType.MINIVAN, VehicleBodyTypeResolver.resolve("Honda", "Odyssey"))
        assertEquals(VehicleBodyType.MINIVAN, VehicleBodyTypeResolver.resolve("Chrysler", "Pacifica"))
        assertEquals(VehicleBodyType.MINIVAN, VehicleBodyTypeResolver.resolve("Kia", "Carnival"))
    }

    @Test
    fun `resolves motorcycles accurately`() {
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("Kawasaki", "Ninja 400"))
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("Honda", "CBR600RR"))
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("Harley-Davidson", "Sportster S"))
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("Ducati", "Panigale V4"))
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("BMW", "R1250 GS"))
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("Suzuki", "Hayabusa"))
    }

    @Test
    fun `defaults to sedan for standard cars, unknown models, and blank inputs`() {
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("Toyota", "Camry"))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("Honda", "Civic"))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("Tesla", "Model 3"))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("BMW", "330i"))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("Audi", "A4"))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("", ""))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve(null, null))
        assertEquals(VehicleBodyType.SEDAN, VehicleBodyTypeResolver.resolve("Custom", "SpecialProject"))
    }

    @Test
    fun `is case insensitive and trims input`() {
        assertEquals(VehicleBodyType.TRUCK, VehicleBodyTypeResolver.resolve("  FORD  ", "  f-150  "))
        assertEquals(VehicleBodyType.SUV, VehicleBodyTypeResolver.resolve("TOYOTA", "rav4"))
        assertEquals(VehicleBodyType.MOTORCYCLE, VehicleBodyTypeResolver.resolve("KAWASAKI", "NINJA"))
    }
}
