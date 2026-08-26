---
type: "query"
date: "2026-08-25T15:05:22.086400+00:00"
question: "What breaks if I add Room composite indexes to ServiceEntity?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["ServiceEntity", "ServiceDao", "ServiceMapper", "ManualBackupManager"]
---

# Q: What breaks if I add Room composite indexes to ServiceEntity?

## Answer

Blast radius is contained to the data layer. Every affected query lives in ServiceDao.kt (L10 getAllServices, L13 getAllServicesOnce, L16 getServicesForVehicle, L19 getServicesForVehicleOnce, L22 getServiceById, L25 insertService, L28 insertServices, L31 updateService, L34 deleteService). Mapping via ServiceMapper.kt:26 toEntity(). Backup path ManualBackupManager.kt:14,277. Two test files MUST be updated: ManualBackupManagerTest.kt:77 and ServiceRepositoryImplTest.kt:9. Index names must match Room's generated identity hash exactly (index_services_vehicleId_serviceDate) or first open throws IllegalStateException. Requires AppDatabase version 2->3, MIGRATION_2_3, and a committed app/schemas/.../3.json since exportSchema=true is pinned.

## Outcome

- Signal: useful

## Source Nodes

- ServiceEntity
- ServiceDao
- ServiceMapper
- ManualBackupManager