package com.example.arlacontrole.analytics.operacional;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.FinancialDashboardSnapshot;
import com.example.arlacontrole.model.FinancialRankingItem;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FinancialIndicatorCalculator {

    public FinancialDashboardSnapshot build(List<RefuelEntity> refuels) {
        FinancialDashboardSnapshot snapshot = new FinancialDashboardSnapshot();
        LocalDate today = LocalDate.now();
        LocalDate currentMonth = today.withDayOfMonth(1);
        LocalDate previousMonth = currentMonth.minusMonths(1);

        Map<String, Aggregate> vehicleMap = new HashMap<>();
        Map<String, Aggregate> driverMap = new HashMap<>();
        double arlaPriceSum = 0d;
        int arlaPriceCount = 0;
        double dieselPriceSum = 0d;
        int dieselPriceCount = 0;
        double totalDistance = 0d;

        if (refuels != null) {
            for (RefuelEntity entity : refuels) {
                LocalDate date = parseDate(entity.suppliedAtIso);
                if (date == null) {
                    continue;
                }

                double totalAmount = entity.totalAmount == null ? 0d : entity.totalAmount;
                if (date.equals(today)) {
                    snapshot.totalCostToday += totalAmount;
                }

                if (date.getYear() == currentMonth.getYear() && date.getMonthValue() == currentMonth.getMonthValue()) {
                    snapshot.totalCostPeriod += totalAmount;
                    if (FuelType.DIESEL.equals(entity.fuelType)) {
                        snapshot.totalDieselCostPeriod += totalAmount;
                        if (entity.pricePerLiter > 0d) {
                            dieselPriceSum += entity.pricePerLiter;
                            dieselPriceCount++;
                        }
                    } else {
                        snapshot.totalArlaCostPeriod += totalAmount;
                        if (entity.pricePerLiter > 0d) {
                            arlaPriceSum += entity.pricePerLiter;
                            arlaPriceCount++;
                        }
                    }

                    if (entity.costPerKm > 0d) {
                        int distance = resolveDistance(entity);
                        if (distance > 0) {
                            totalDistance += distance;
                        }
                    }

                    accumulate(
                        vehicleMap,
                        entity.vehiclePlate,
                        entity.vehiclePlate + " | " + entity.vehicleFleetCode,
                        entity.vehicleModel,
                        totalAmount,
                        entity.costPerKm,
                        entity.pricePerLiter,
                        entity.statusLevel
                    );
                    accumulate(
                        driverMap,
                        entity.driverName,
                        entity.driverName,
                        entity.vehiclePlate,
                        totalAmount,
                        entity.costPerKm,
                        entity.pricePerLiter,
                        entity.statusLevel
                    );

                    if (entity.pricePerLiter > 0d && RefuelStatus.ALERT.equals(entity.statusLevel)) {
                        snapshot.alerts.add(buildAlert(entity, "Abastecimento fora do padrao financeiro."));
                    } else if (entity.pricePerLiter > 0d && RefuelStatus.ATTENTION.equals(entity.statusLevel)) {
                        snapshot.alerts.add(buildAlert(entity, "Preco por litro pede revisao operacional."));
                    }
                } else if (date.getYear() == previousMonth.getYear() && date.getMonthValue() == previousMonth.getMonthValue()) {
                    snapshot.previousPeriodTotalCost += totalAmount;
                }
            }
        }

        snapshot.averageArlaPricePerLiter = arlaPriceCount == 0 ? null : arlaPriceSum / arlaPriceCount;
        snapshot.averageDieselPricePerLiter = dieselPriceCount == 0 ? null : dieselPriceSum / dieselPriceCount;
        snapshot.averageCostPerKm = totalDistance <= 0d ? 0d : snapshot.totalCostPeriod / totalDistance;
        snapshot.costVariationPercent = calculateVariation(snapshot.totalCostPeriod, snapshot.previousPeriodTotalCost);
        snapshot.summary = buildSummary(snapshot);
        snapshot.vehicleRanking.addAll(toRankingList(vehicleMap));
        snapshot.driverRanking.addAll(toRankingList(driverMap));
        limitAlerts(snapshot.alerts, 4);
        return snapshot;
    }

    private void accumulate(
        Map<String, Aggregate> target,
        String key,
        String title,
        String subtitle,
        double totalAmount,
        double costPerKm,
        double pricePerLiter,
        String status
    ) {
        Aggregate item = target.get(key);
        if (item == null) {
            item = new Aggregate();
            item.key = key;
            item.title = title;
            item.subtitle = subtitle;
            target.put(key, item);
        }
        item.totalCost += totalAmount;
        item.refuelCount++;
        if (costPerKm > 0d) {
            item.costPerKmSum += costPerKm;
            item.costPerKmCount++;
        }
        if (pricePerLiter > 0d) {
            item.pricePerLiterSum += pricePerLiter;
            item.pricePerLiterCount++;
        }
        if (RefuelStatus.priority(status) > RefuelStatus.priority(item.topStatus)) {
            item.topStatus = status;
        }
    }

    private List<FinancialRankingItem> toRankingList(Map<String, Aggregate> source) {
        List<FinancialRankingItem> items = new ArrayList<>();
        for (Aggregate aggregate : source.values()) {
            FinancialRankingItem item = new FinancialRankingItem();
            item.key = aggregate.key;
            item.title = aggregate.title;
            item.subtitle = aggregate.subtitle;
            item.totalCost = aggregate.totalCost;
            item.costPerKm = aggregate.costPerKmCount == 0 ? null : aggregate.costPerKmSum / aggregate.costPerKmCount;
            item.averagePricePerLiter = aggregate.pricePerLiterCount == 0 ? null : aggregate.pricePerLiterSum / aggregate.pricePerLiterCount;
            item.refuelCount = aggregate.refuelCount;
            item.topStatus = aggregate.topStatus;
            items.add(item);
        }
        items.sort(Comparator.comparingDouble((FinancialRankingItem item) -> item.totalCost).reversed());
        if (items.size() > 5) {
            items.subList(5, items.size()).clear();
        }
        return items;
    }

    private PriorityAlertItem buildAlert(RefuelEntity entity, String description) {
        PriorityAlertItem alert = new PriorityAlertItem();
        alert.title = entity.vehiclePlate + " | " + entity.driverName;
        alert.description = description;
        alert.level = entity.statusLevel;
        alert.source = "FINANCEIRO";
        return alert;
    }

    private void limitAlerts(List<PriorityAlertItem> alerts, int limit) {
        alerts.sort((first, second) -> Integer.compare(RefuelStatus.priority(second.level), RefuelStatus.priority(first.level)));
        if (alerts.size() > limit) {
            alerts.subList(limit, alerts.size()).clear();
        }
    }

    private double calculateVariation(double current, double previous) {
        if (previous <= 0d) {
            return current <= 0d ? 0d : 100d;
        }
        return ((current - previous) / previous) * 100d;
    }

    private String buildSummary(FinancialDashboardSnapshot snapshot) {
        String trendWord = snapshot.costVariationPercent > 0d ? "alta" : (snapshot.costVariationPercent < 0d ? "queda" : "estavel");
        return "Financeiro do mes com gasto total de "
            + String.format(Locale.US, "R$ %.2f", snapshot.totalCostPeriod)
            + ", custo medio de "
            + String.format(Locale.US, "R$ %.2f/km", snapshot.averageCostPerKm)
            + " e tendencia em " + trendWord + ".";
    }

    private int resolveDistance(RefuelEntity entity) {
        if (entity.odometerFinalKm > 0 && entity.odometerInitialKm > 0 && entity.odometerFinalKm > entity.odometerInitialKm) {
            return entity.odometerFinalKm - entity.odometerInitialKm;
        }
        if (entity.kmSinceLastSupply != null && entity.kmSinceLastSupply > 0) {
            return entity.kmSinceLastSupply;
        }
        return 0;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class Aggregate {
        String key = "";
        String title = "";
        String subtitle = "";
        double totalCost;
        double costPerKmSum;
        int costPerKmCount;
        double pricePerLiterSum;
        int pricePerLiterCount;
        int refuelCount;
        String topStatus = RefuelStatus.NORMAL;
    }
}
