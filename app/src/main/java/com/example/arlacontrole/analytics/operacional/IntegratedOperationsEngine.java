package com.example.arlacontrole.analytics.operacional;

import com.example.arlacontrole.analytics.integrado.OperationalAlertEngine;
import com.example.arlacontrole.analytics.seguranca.SafetyAnalyticsEngine;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.DashboardSnapshot;
import com.example.arlacontrole.model.FinancialDashboardSnapshot;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.IndicatorSnapshot;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyDashboardSnapshot;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetyRankingItem;
import com.example.arlacontrole.model.VehicleRankingItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IntegratedOperationsEngine {

    private final SafetyAnalyticsEngine safetyAnalyticsEngine = new SafetyAnalyticsEngine();
    private final FinancialIndicatorCalculator financialIndicatorCalculator = new FinancialIndicatorCalculator();
    private final OperationalAlertEngine operationalAlertEngine = new OperationalAlertEngine();

    public DashboardSnapshot buildDashboard(List<RefuelEntity> refuels, List<SafetyEventEntity> safetyEvents, int pendingCount) {
        DashboardSnapshot item = new DashboardSnapshot();
        SafetyDashboardSnapshot safetySnapshot = safetyAnalyticsEngine.buildDashboard(safetyEvents);
        FinancialDashboardSnapshot financialSnapshot = financialIndicatorCalculator.build(refuels);
        LocalDate today = LocalDate.now();

        Map<String, Integer> refuelsPerVehicle = new HashMap<>();
        Map<String, Integer> safetyPerVehicle = new HashMap<>();
        Map<String, Integer> refuelsPerDriver = new HashMap<>();
        Map<String, Integer> safetyPerDriver = new HashMap<>();

        if (refuels != null) {
            for (RefuelEntity entity : refuels) {
                LocalDate date = parseDate(entity.suppliedAtIso);
                if (date != null && date.equals(today)) {
                    item.totalRefuelsToday++;
                }
                if (date != null && date.getYear() == today.getYear() && date.getMonthValue() == today.getMonthValue()) {
                    if (FuelType.DIESEL.equals(entity.fuelType)) {
                        item.dieselLitersPeriod += entity.liters;
                    } else {
                        item.arlaLitersPeriod += entity.liters;
                    }
                    refuelsPerVehicle.put(entity.vehiclePlate, refuelsPerVehicle.getOrDefault(entity.vehiclePlate, 0) + 1);
                    refuelsPerDriver.put(entity.driverName, refuelsPerDriver.getOrDefault(entity.driverName, 0) + 1);
                }
                if (!RefuelStatus.NORMAL.equals(entity.statusLevel)) {
                    item.activeAlerts++;
                }
            }
        }

        if (safetyEvents != null) {
            for (SafetyEventEntity event : safetyEvents) {
                LocalDate date = parseDate(event.occurredAtIso);
                if (date != null && date.getYear() == today.getYear() && date.getMonthValue() == today.getMonthValue()) {
                    item.totalSafetyEventsPeriod++;
                    safetyPerVehicle.put(event.vehiclePlate, safetyPerVehicle.getOrDefault(event.vehiclePlate, 0) + 1);
                    safetyPerDriver.put(event.driverName, safetyPerDriver.getOrDefault(event.driverName, 0) + 1);
                    if (!SafetyAnalysisStatus.isResolved(event.analysisStatus)
                        || SafetyEventType.ACCIDENT.equals(event.eventType)) {
                        item.activeAlerts++;
                    }
                }
            }
        }

        item.pendingSync = pendingCount;
        item.totalVolumePeriod = item.arlaLitersPeriod + item.dieselLitersPeriod;
        item.totalCostToday = financialSnapshot.totalCostToday;
        item.totalCostPeriod = financialSnapshot.totalCostPeriod;
        item.totalArlaCostPeriod = financialSnapshot.totalArlaCostPeriod;
        item.totalDieselCostPeriod = financialSnapshot.totalDieselCostPeriod;
        item.averageCostPerKm = financialSnapshot.averageCostPerKm;
        item.costVariationPercent = financialSnapshot.costVariationPercent;
        item.unresolvedSafetyEvents = safetySnapshot.unresolvedEvents;
        item.evidenceCoveragePercent = safetySnapshot.evidenceCoveragePercent;
        item.resolutionRatePercent = safetySnapshot.resolutionRatePercent;
        item.safetyRiskLevel = safetySnapshot.riskLevel;
        item.highRiskAssets = countHighRiskAssets(safetyPerVehicle);
        List<PriorityAlertItem> combinedAlerts = new ArrayList<>();
        combinedAlerts.addAll(financialSnapshot.alerts);
        combinedAlerts.addAll(safetySnapshot.priorityAlerts);
        combinedAlerts.addAll(operationalAlertEngine.buildAlerts(refuels, safetyEvents, financialSnapshot));
        item.activeAlerts = combinedAlerts.size();
        item.executiveSummary = buildExecutiveSummary(item, safetySnapshot, financialSnapshot);
        item.priorityAlerts.addAll(limitAlerts(combinedAlerts, 5));
        item.integratedInsights.addAll(operationalAlertEngine.buildInsights(refuels, safetyEvents, financialSnapshot));
        item.integratedInsights.addAll(buildIntegratedInsights(refuelsPerVehicle, safetyPerVehicle, refuelsPerDriver, safetyPerDriver));
        if (item.integratedInsights.isEmpty()) {
            item.integratedInsights.add("Operacao e seguranca seguem sem correlacao critica no periodo atual.");
        }
        if (item.integratedInsights.size() > 4) {
            item.integratedInsights.subList(4, item.integratedInsights.size()).clear();
        }
        return item;
    }

    public IndicatorSnapshot buildIndicators(List<RefuelEntity> refuels, List<SafetyEventEntity> safetyEvents) {
        IndicatorSnapshot item = new IndicatorSnapshot();
        SafetyDashboardSnapshot safetySnapshot = safetyAnalyticsEngine.buildDashboard(safetyEvents);
        FinancialDashboardSnapshot financialSnapshot = financialIndicatorCalculator.build(refuels);
        LocalDate now = LocalDate.now();
        Map<String, VehicleRankingItem> dieselMap = new HashMap<>();
        Map<String, VehicleRankingItem> arlaMap = new HashMap<>();
        Map<String, Integer> dieselCounter = new HashMap<>();
        Map<String, Integer> arlaCounter = new HashMap<>();
        Set<String> monthVehicles = new HashSet<>();

        if (refuels != null) {
            for (RefuelEntity entity : refuels) {
                LocalDate date = parseDate(entity.suppliedAtIso);
                if (date == null || date.getYear() != now.getYear() || date.getMonthValue() != now.getMonthValue()) {
                    continue;
                }
                item.totalRecords++;
                monthVehicles.add(entity.vehiclePlate);

                if (FuelType.DIESEL.equals(entity.fuelType)) {
                    item.dieselMonthlyTotal += entity.liters;
                    updateRanking(dieselMap, dieselCounter, entity, true);
                } else {
                    item.arlaMonthlyTotal += entity.liters;
                    updateRanking(arlaMap, arlaCounter, entity, false);
                }

                if (!RefuelStatus.NORMAL.equals(entity.statusLevel)) {
                    item.alerts.add(entity);
                }
            }
        }

        finalizeVehicleRanking(arlaMap, arlaCounter, item.arlaRanking);
        finalizeVehicleRanking(dieselMap, dieselCounter, item.dieselRanking);
        item.totalMonthlyVolume = item.arlaMonthlyTotal + item.dieselMonthlyTotal;
        item.totalCostPeriod = financialSnapshot.totalCostPeriod;
        item.totalArlaCostPeriod = financialSnapshot.totalArlaCostPeriod;
        item.totalDieselCostPeriod = financialSnapshot.totalDieselCostPeriod;
        item.averageCostPerKm = financialSnapshot.averageCostPerKm;
        item.costVariationPercent = financialSnapshot.costVariationPercent;
        item.monitoredVehicles = monthVehicles.size();
        item.averagePerVehicle = monthVehicles.isEmpty() ? 0d : (item.arlaMonthlyTotal + item.dieselMonthlyTotal) / monthVehicles.size();
        item.refuelAlertCount = item.alerts.size();

        item.totalSafetyEvents = safetySnapshot.totalEvents;
        item.totalAccidents = safetySnapshot.accidents;
        item.totalIncidents = safetySnapshot.incidents;
        item.totalNearMisses = safetySnapshot.nearMisses;
        item.unresolvedSafetyEvents = safetySnapshot.unresolvedEvents;
        item.priorityAlertCount = safetySnapshot.priorityAlerts.size();
        item.evidenceCoveragePercent = safetySnapshot.evidenceCoveragePercent;
        item.resolutionRatePercent = safetySnapshot.resolutionRatePercent;
        item.safetyRiskLevel = safetySnapshot.riskLevel;
        item.safetyRiskIndex = safetySnapshot.riskIndex;
        item.vehicleSafetyRanking.addAll(safetySnapshot.vehicleRanking);
        item.driverSafetyRanking.addAll(safetySnapshot.driverRanking);
        item.vehicleCostRanking.addAll(financialSnapshot.vehicleRanking);
        item.driverCostRanking.addAll(financialSnapshot.driverRanking);
        List<PriorityAlertItem> combinedAlerts = new ArrayList<>();
        combinedAlerts.addAll(financialSnapshot.alerts);
        combinedAlerts.addAll(safetySnapshot.priorityAlerts);
        combinedAlerts.addAll(operationalAlertEngine.buildAlerts(refuels, safetyEvents, financialSnapshot));
        item.priorityAlerts.addAll(limitAlerts(combinedAlerts, 5));
        item.integratedInsights.addAll(buildPortfolioInsights(item, safetySnapshot));
        item.integratedInsights.addAll(operationalAlertEngine.buildInsights(refuels, safetyEvents, financialSnapshot));
        if (item.integratedInsights.size() > 5) {
            item.integratedInsights.subList(5, item.integratedInsights.size()).clear();
        }
        return item;
    }

    private String buildExecutiveSummary(
        DashboardSnapshot dashboard,
        SafetyDashboardSnapshot safetySnapshot,
        FinancialDashboardSnapshot financialSnapshot
    ) {
        return "Leitura integrada do periodo: "
            + dashboard.totalSafetyEventsPeriod + " evento(s) Maxtrack, gasto de "
            + String.format(Locale.US, "R$ %.2f", financialSnapshot.totalCostPeriod)
            + ", custo medio de "
            + String.format(Locale.US, "R$ %.2f/km", financialSnapshot.averageCostPerKm)
            + " e risco " + safetySnapshot.riskLevel + ".";
    }

    private int countHighRiskAssets(Map<String, Integer> occurrences) {
        int result = 0;
        for (int count : occurrences.values()) {
            if (count >= 2) {
                result++;
            }
        }
        return result;
    }

    private List<String> buildIntegratedInsights(
        Map<String, Integer> refuelsPerVehicle,
        Map<String, Integer> safetyPerVehicle,
        Map<String, Integer> refuelsPerDriver,
        Map<String, Integer> safetyPerDriver
    ) {
        List<String> insights = new ArrayList<>();
        for (String plate : refuelsPerVehicle.keySet()) {
            int refuels = refuelsPerVehicle.getOrDefault(plate, 0);
            int events = safetyPerVehicle.getOrDefault(plate, 0);
            if (refuels >= 2 && events >= 2) {
                insights.add("Veiculo " + plate + " combina volume alto de abastecimentos com recorrencia de eventos.");
                break;
            }
        }
        for (String driver : refuelsPerDriver.keySet()) {
            int refuels = refuelsPerDriver.getOrDefault(driver, 0);
            int events = safetyPerDriver.getOrDefault(driver, 0);
            if (refuels >= 2 && events >= 2) {
                insights.add("Motorista " + driver + " pede leitura cruzada entre rotina operacional e seguranca.");
                break;
            }
        }
        if (!safetyPerVehicle.isEmpty() && Collections.max(safetyPerVehicle.values()) >= 3) {
            insights.add("Ha concentracao de ocorrencias em um mesmo ativo, sugerindo risco operacional localizado.");
        }
        if (insights.size() > 3) {
            insights.subList(3, insights.size()).clear();
        }
        return insights;
    }

    private List<String> buildPortfolioInsights(IndicatorSnapshot indicator, SafetyDashboardSnapshot safetySnapshot) {
        List<String> insights = new ArrayList<>();
        if (indicator.averagePerVehicle > 0d && indicator.totalSafetyEvents > 0) {
            insights.add(
                "A frota operou com media de " + String.format(Locale.US, "%.1f", indicator.averagePerVehicle)
                    + " L por veiculo e " + indicator.totalSafetyEvents + " evento(s) de seguranca no ciclo."
            );
        }
        if (indicator.totalCostPeriod > 0d) {
            insights.add(
                "Gasto total de " + String.format(Locale.US, "R$ %.2f", indicator.totalCostPeriod)
                    + " com custo medio de " + String.format(Locale.US, "R$ %.2f/km", indicator.averageCostPerKm) + "."
            );
        }
        if (!indicator.vehicleSafetyRanking.isEmpty()) {
            SafetyRankingItem topVehicle = indicator.vehicleSafetyRanking.get(0);
            insights.add("Veiculo com maior risco atual: " + topVehicle.title + " (score " + topVehicle.riskScore + ").");
        }
        if (!indicator.vehicleCostRanking.isEmpty()) {
            insights.add("Veiculo mais caro para operar no periodo: " + indicator.vehicleCostRanking.get(0).title + ".");
        }
        if (!indicator.driverSafetyRanking.isEmpty()) {
            SafetyRankingItem topDriver = indicator.driverSafetyRanking.get(0);
            insights.add("Motorista com maior exposicao atual: " + topDriver.title + " com " + topDriver.eventCount + " ocorrencia(s).");
        }
        if (safetySnapshot.evidenceCoveragePercent < 70d) {
            insights.add("A cobertura de evidencias ainda esta abaixo da meta para auditoria operacional.");
        }
        if (indicator.refuelAlertCount > 0) {
            insights.add("Ha " + indicator.refuelAlertCount + " abastecimento(s) fora do padrao pedindo leitura do operacional.");
        }
        if (insights.size() > 4) {
            insights.subList(4, insights.size()).clear();
        }
        return insights;
    }

    private List<PriorityAlertItem> limitAlerts(List<PriorityAlertItem> source, int limit) {
        List<PriorityAlertItem> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (PriorityAlertItem item : source) {
            result.add(item);
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private void updateRanking(Map<String, VehicleRankingItem> rankingMap, Map<String, Integer> counters, RefuelEntity entity, boolean diesel) {
        String key = entity.vehiclePlate + "|" + entity.fuelType;
        VehicleRankingItem item = rankingMap.get(key);
        if (item == null) {
            item = new VehicleRankingItem();
            item.fuelType = entity.fuelType;
            item.plate = entity.vehiclePlate;
            item.fleetCode = entity.vehicleFleetCode;
            item.model = entity.vehicleModel;
            item.topStatus = entity.statusLevel;
            rankingMap.put(key, item);
            counters.put(key, 0);
        }

        item.totalLiters += entity.liters;
        if (diesel && entity.kmPerLiter != null) {
            item.averageConsumptionValue += entity.kmPerLiter;
            counters.put(key, counters.get(key) + 1);
        } else if (!diesel && entity.litersPer1000Km != null) {
            item.averageConsumptionValue += entity.litersPer1000Km;
            counters.put(key, counters.get(key) + 1);
        }
        if (RefuelStatus.priority(entity.statusLevel) > RefuelStatus.priority(item.topStatus)) {
            item.topStatus = entity.statusLevel;
        }
    }

    private void finalizeVehicleRanking(Map<String, VehicleRankingItem> rankingMap, Map<String, Integer> counters, List<VehicleRankingItem> target) {
        for (Map.Entry<String, VehicleRankingItem> entry : rankingMap.entrySet()) {
            int divisor = counters.get(entry.getKey());
            if (divisor > 0) {
                entry.getValue().averageConsumptionValue = entry.getValue().averageConsumptionValue / divisor;
            }
            target.add(entry.getValue());
        }
        target.sort(Comparator.comparingDouble((VehicleRankingItem value) -> value.totalLiters).reversed());
    }

    private LocalDate parseDate(String iso) {
        try {
            return LocalDateTime.parse(iso).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }
}
