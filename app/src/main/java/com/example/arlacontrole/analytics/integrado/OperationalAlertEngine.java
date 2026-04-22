package com.example.arlacontrole.analytics.integrado;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.FinancialDashboardSnapshot;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OperationalAlertEngine {

    public List<PriorityAlertItem> buildAlerts(
        List<RefuelEntity> refuels,
        List<SafetyEventEntity> safetyEvents,
        FinancialDashboardSnapshot financialSnapshot
    ) {
        List<PriorityAlertItem> alerts = new ArrayList<>();
        Map<String, VehicleAggregate> vehicleMap = buildVehicleAggregates(refuels, safetyEvents);
        Map<String, DriverAggregate> driverMap = buildDriverAggregates(refuels, safetyEvents);
        double averageVehicleCost = averageVehicleCost(vehicleMap);

        for (VehicleAggregate aggregate : vehicleMap.values()) {
            if (aggregate.events >= 2 && aggregate.totalCost >= averageVehicleCost * 1.25d) {
                alerts.add(createAlert(
                    RefuelStatus.ALERT,
                    "OPERACAO",
                    "Veiculo com custo alto e recorrencia de eventos",
                    aggregate.plate + " combina " + aggregate.events + " evento(s) com gasto acima da media do periodo."
                ));
            } else if (aggregate.events >= 1
                && aggregate.averageCostPerKm > 0d
                && financialSnapshot != null
                && financialSnapshot.averageCostPerKm > 0d
                && aggregate.averageCostPerKm >= financialSnapshot.averageCostPerKm * 1.20d) {
                alerts.add(createAlert(
                    RefuelStatus.ATTENTION,
                    "OPERACAO",
                    "Custo por km elevado com eventos na operacao",
                    aggregate.plate + " esta com custo por km acima do baseline e ja registra evento(s) de conducao."
                ));
            }
        }

        for (DriverAggregate aggregate : driverMap.values()) {
            if (aggregate.events >= 3) {
                alerts.add(createAlert(
                    RefuelStatus.ATTENTION,
                    "OPERACAO",
                    "Motorista com recorrencia de eventos",
                    aggregate.driverName + " acumulou " + aggregate.events + " evento(s) no periodo e precisa de acompanhamento."
                ));
            }
        }

        int currentWindowEvents = countEventsLastDays(safetyEvents, 30, 0);
        int previousWindowEvents = countEventsLastDays(safetyEvents, 60, 30);
        if (previousWindowEvents > 0 && currentWindowEvents > previousWindowEvents * 1.25d) {
            alerts.add(createAlert(
                RefuelStatus.ATTENTION,
                "MAXTRACK",
                "Aumento de eventos em relacao ao periodo anterior",
                "Os eventos cresceram de " + previousWindowEvents + " para " + currentWindowEvents + " nas ultimas semanas."
            ));
        }

        alerts.sort((first, second) -> Integer.compare(
            com.example.arlacontrole.model.RefuelStatus.priority(second.level),
            com.example.arlacontrole.model.RefuelStatus.priority(first.level)
        ));
        if (alerts.size() > 5) {
            alerts.subList(5, alerts.size()).clear();
        }
        return alerts;
    }

    public List<String> buildInsights(
        List<RefuelEntity> refuels,
        List<SafetyEventEntity> safetyEvents,
        FinancialDashboardSnapshot financialSnapshot
    ) {
        List<String> insights = new ArrayList<>();
        Map<String, VehicleAggregate> vehicleMap = buildVehicleAggregates(refuels, safetyEvents);
        Map<String, DriverAggregate> driverMap = buildDriverAggregates(refuels, safetyEvents);

        VehicleAggregate topVehicle = topVehicle(vehicleMap);
        if (topVehicle != null && topVehicle.events > 0) {
            insights.add(
                "Veiculo " + topVehicle.plate + " lidera a leitura cruzada com "
                    + topVehicle.events + " evento(s) e gasto de "
                    + String.format(Locale.US, "R$ %.2f", topVehicle.totalCost) + "."
            );
        }

        DriverAggregate topDriver = topDriver(driverMap);
        if (topDriver != null && topDriver.events > 0) {
            insights.add(
                "Motorista " + topDriver.driverName + " aparece com "
                    + topDriver.events + " evento(s) e custo total de "
                    + String.format(Locale.US, "R$ %.2f", topDriver.totalCost) + " no periodo."
            );
        }

        if (financialSnapshot != null && financialSnapshot.averageCostPerKm > 0d) {
            insights.add(
                "Custo medio atual de "
                    + String.format(Locale.US, "R$ %.2f/km", financialSnapshot.averageCostPerKm)
                    + " com leitura integrada de seguranca e consumo."
            );
        }

        if (insights.size() > 3) {
            insights.subList(3, insights.size()).clear();
        }
        return insights;
    }

    private Map<String, VehicleAggregate> buildVehicleAggregates(List<RefuelEntity> refuels, List<SafetyEventEntity> safetyEvents) {
        Map<String, VehicleAggregate> result = new HashMap<>();
        LocalDate threshold = LocalDate.now().minusDays(30);
        if (refuels != null) {
            for (RefuelEntity entity : refuels) {
                LocalDate date = parseDate(entity.suppliedAtIso);
                if (date == null || date.isBefore(threshold)) {
                    continue;
                }
                VehicleAggregate item = result.get(entity.vehiclePlate);
                if (item == null) {
                    item = new VehicleAggregate();
                    item.plate = entity.vehiclePlate;
                    result.put(entity.vehiclePlate, item);
                }
                item.totalCost += entity.totalAmount == null ? 0d : entity.totalAmount;
                if (entity.costPerKm > 0d) {
                    item.costPerKmSum += entity.costPerKm;
                    item.costPerKmCount++;
                }
            }
        }
        if (safetyEvents != null) {
            for (SafetyEventEntity event : safetyEvents) {
                LocalDate date = parseDate(event.occurredAtIso);
                if (date == null || date.isBefore(threshold)) {
                    continue;
                }
                VehicleAggregate item = result.get(event.vehiclePlate);
                if (item == null) {
                    item = new VehicleAggregate();
                    item.plate = event.vehiclePlate;
                    result.put(event.vehiclePlate, item);
                }
                item.events += resolveOccurrenceCount(event);
            }
        }
        for (VehicleAggregate item : result.values()) {
            item.averageCostPerKm = item.costPerKmCount == 0 ? 0d : item.costPerKmSum / item.costPerKmCount;
        }
        return result;
    }

    private Map<String, DriverAggregate> buildDriverAggregates(List<RefuelEntity> refuels, List<SafetyEventEntity> safetyEvents) {
        Map<String, DriverAggregate> result = new HashMap<>();
        LocalDate threshold = LocalDate.now().minusDays(30);
        if (refuels != null) {
            for (RefuelEntity entity : refuels) {
                LocalDate date = parseDate(entity.suppliedAtIso);
                if (date == null || date.isBefore(threshold)) {
                    continue;
                }
                DriverAggregate item = result.get(entity.driverName);
                if (item == null) {
                    item = new DriverAggregate();
                    item.driverName = entity.driverName;
                    result.put(entity.driverName, item);
                }
                item.totalCost += entity.totalAmount == null ? 0d : entity.totalAmount;
            }
        }
        if (safetyEvents != null) {
            for (SafetyEventEntity event : safetyEvents) {
                LocalDate date = parseDate(event.occurredAtIso);
                if (date == null || date.isBefore(threshold)) {
                    continue;
                }
                DriverAggregate item = result.get(event.driverName);
                if (item == null) {
                    item = new DriverAggregate();
                    item.driverName = event.driverName;
                    result.put(event.driverName, item);
                }
                item.events += resolveOccurrenceCount(event);
            }
        }
        return result;
    }

    private double averageVehicleCost(Map<String, VehicleAggregate> vehicleMap) {
        if (vehicleMap.isEmpty()) {
            return 0d;
        }
        double total = 0d;
        for (VehicleAggregate aggregate : vehicleMap.values()) {
            total += aggregate.totalCost;
        }
        return total / vehicleMap.size();
    }

    private int countEventsLastDays(List<SafetyEventEntity> events, int upperDays, int lowerDaysExclusive) {
        if (events == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDate upperBound = today.minusDays(upperDays);
        LocalDate lowerBound = today.minusDays(lowerDaysExclusive);
        int result = 0;
        for (SafetyEventEntity event : events) {
            LocalDate date = parseDate(event.occurredAtIso);
            if (date != null && !date.isBefore(upperBound) && date.isBefore(lowerBound)) {
                result += resolveOccurrenceCount(event);
            }
        }
        return result;
    }

    private PriorityAlertItem createAlert(String level, String source, String title, String description) {
        PriorityAlertItem item = new PriorityAlertItem();
        item.level = level;
        item.source = source;
        item.title = title;
        item.description = description;
        return item;
    }

    private VehicleAggregate topVehicle(Map<String, VehicleAggregate> source) {
        VehicleAggregate result = null;
        double bestScore = 0d;
        for (VehicleAggregate item : source.values()) {
            double score = item.totalCost + (item.events * 250d);
            if (score > bestScore) {
                bestScore = score;
                result = item;
            }
        }
        return result;
    }

    private DriverAggregate topDriver(Map<String, DriverAggregate> source) {
        DriverAggregate result = null;
        double bestScore = 0d;
        for (DriverAggregate item : source.values()) {
            double score = item.totalCost + (item.events * 250d);
            if (score > bestScore) {
                bestScore = score;
                result = item;
            }
        }
        return result;
    }

    private int resolveOccurrenceCount(SafetyEventEntity event) {
        return event == null || event.occurrenceCount <= 0 ? 1 : event.occurrenceCount;
    }

    private LocalDate parseDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(iso).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class VehicleAggregate {
        String plate = "";
        double totalCost;
        int events;
        double costPerKmSum;
        int costPerKmCount;
        double averageCostPerKm;
    }

    private static class DriverAggregate {
        String driverName = "";
        double totalCost;
        int events;
    }
}
