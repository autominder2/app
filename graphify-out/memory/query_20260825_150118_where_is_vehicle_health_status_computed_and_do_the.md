---
type: "query"
date: "2026-08-25T15:01:18.445489+00:00"
question: "Where is vehicle health status computed and do the calculators agree?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["StatusCalculator", "GetDashboardDataUseCase", "DashboardViewModel", "VehicleListViewModel", "ServiceStatus"]
---

# Q: Where is vehicle health status computed and do the calculators agree?

## Answer

Two disagreeing paths. GetDashboardDataUseCase.kt:98 applies .take(5) to a FLEET-WIDE sorted reminder list BEFORE DashboardViewModel.kt:149 filters to activeVehicleId, so a vehicle's reminders can be starved out and computeOperationalStatus returns HEALTHY on the empty list. VehicleListViewModel.kt:71-92 computes uncapped from the same DAO and reports OVERDUE for the same vehicle. StatusCalculator.kt:26 .calculate() itself is sound. ServiceStatus.UNKNOWN has severity 0 so it never increments attentionCount.

## Outcome

- Signal: useful

## Source Nodes

- StatusCalculator
- GetDashboardDataUseCase
- DashboardViewModel
- VehicleListViewModel
- ServiceStatus