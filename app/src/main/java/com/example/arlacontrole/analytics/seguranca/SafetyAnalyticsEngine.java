package com.example.arlacontrole.analytics.seguranca;

import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyDashboardSnapshot;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetyRankingItem;
import com.example.arlacontrole.model.SafetySeverity;

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

public class SafetyAnalyticsEngine {

    public SafetyDashboardSnapshot buildDashboard(List<SafetyEventEntity> events) {
        SafetyDashboardSnapshot snapshot = new SafetyDashboardSnapshot();
        List<SafetyEventEntity> safeEvents = events == null ? new ArrayList<>() : events;
        List<SafetyEventEntity> currentWindow = filterCurrentWindow(safeEvents);
        if (currentWindow.isEmpty() && safeEvents.isEmpty()) {
            snapshot.executiveSummary = "Sem eventos de seguranca registrados no periodo atual.";
            return snapshot;
        }

        int evidenceCount = 0;
        int resolvedCount = 0;
        int criticalCount = 0;
        Map<String, SafetyRankingItem> vehicleMap = new HashMap<>();
        Map<String, SafetyRankingItem> driverMap = new HashMap<>();

        for (SafetyEventEntity event : currentWindow) {
            int eventCount = resolveOccurrenceCount(event);
            snapshot.totalEvents += eventCount;
            if (event.hasEvidence()) {
                evidenceCount += eventCount;
            }
            if (SafetyAnalysisStatus.isResolved(event.analysisStatus)) {
                resolvedCount += eventCount;
            } else {
                snapshot.unresolvedEvents += eventCount;
            }
            if (SafetySeverity.priority(event.severity) >= SafetySeverity.priority(SafetySeverity.HIGH)) {
                criticalCount += eventCount;
            }
            incrementTypeCounters(snapshot, event.eventType, eventCount);
            accumulateRanking(
                vehicleMap,
                event.vehiclePlate,
                event.vehiclePlate,
                event.vehicleFleetCode + " | " + event.vehicleModel,
                event
            );
            accumulateRanking(
                driverMap,
                String.valueOf(event.driverId),
                event.driverName,
                event.vehiclePlate,
                event
            );
        }

        snapshot.evidenceCoveragePercent = percentage(evidenceCount, snapshot.totalEvents);
        snapshot.resolutionRatePercent = percentage(resolvedCount, snapshot.totalEvents);
        snapshot.riskIndex = computeRiskIndex(safeEvents, currentWindow);
        snapshot.riskLevel = resolveRiskLevel(snapshot.riskIndex);

        finalizeRanking(vehicleMap, snapshot.vehicleRanking);
        finalizeRanking(driverMap, snapshot.driverRanking);
        buildPriorityAlerts(snapshot, currentWindow, criticalCount);
        snapshot.executiveSummary = buildExecutiveSummary(snapshot);
        return snapshot;
    }

    private List<SafetyEventEntity> filterCurrentWindow(List<SafetyEventEntity> events) {
        LocalDate threshold = LocalDate.now().minusDays(30);
        List<SafetyEventEntity> result = new ArrayList<>();
        for (SafetyEventEntity event : events) {
            LocalDate date = parseDate(event.occurredAtIso);
            if (date != null && !date.isBefore(threshold)) {
                result.add(event);
            }
        }
        return result;
    }

    private void incrementTypeCounters(SafetyDashboardSnapshot snapshot, String type, int eventCount) {
        if (SafetyEventType.ACCIDENT.equals(type)) {
            snapshot.accidents += eventCount;
        } else if (SafetyEventType.INCIDENT.equals(type)) {
            snapshot.incidents += eventCount;
        } else if (SafetyEventType.NEAR_MISS.equals(type)) {
            snapshot.nearMisses += eventCount;
        } else if (SafetyEventType.UNSAFE_CONDITION.equals(type)) {
            snapshot.unsafeConditions += eventCount;
        } else {
            snapshot.unsafeBehaviors += eventCount;
        }
    }

    private void accumulateRanking(
        Map<String, SafetyRankingItem> target,
        String key,
        String title,
        String subtitle,
        SafetyEventEntity event
    ) {
        SafetyRankingItem item = target.get(key);
        if (item == null) {
            item = new SafetyRankingItem();
            item.key = key;
            item.title = title == null ? "" : title;
            item.subtitle = subtitle == null ? "" : subtitle;
            target.put(key, item);
        }
        int eventCount = resolveOccurrenceCount(event);
        item.eventCount += eventCount;
        if (!SafetyAnalysisStatus.isResolved(event.analysisStatus)) {
            item.unresolvedCount += eventCount;
        }
        item.riskScore += (SafetySeverity.weight(event.severity) + SafetyEventType.weight(event.eventType)) * eventCount;
        if (SafetySeverity.priority(event.severity) > SafetySeverity.priority(item.topSeverity)) {
            item.topSeverity = event.severity;
        }
    }

    private void finalizeRanking(Map<String, SafetyRankingItem> source, List<SafetyRankingItem> target) {
        target.addAll(source.values());
        Collections.sort(
            target,
            Comparator.comparingInt((SafetyRankingItem value) -> value.riskScore)
                .thenComparingInt(value -> value.eventCount)
                .reversed()
        );
        if (target.size() > 5) {
            target.subList(5, target.size()).clear();
        }
    }

    private void buildPriorityAlerts(SafetyDashboardSnapshot snapshot, List<SafetyEventEntity> events, int criticalCount) {
        Map<String, Integer> vehicleRecurrence = new HashMap<>();
        Map<String, Integer> driverRecurrence = new HashMap<>();
        Set<String> unresolvedCriticalVehicles = new HashSet<>();
        for (SafetyEventEntity event : events) {
            int eventCount = resolveOccurrenceCount(event);
            vehicleRecurrence.put(event.vehiclePlate, vehicleRecurrence.getOrDefault(event.vehiclePlate, 0) + eventCount);
            driverRecurrence.put(event.driverName, driverRecurrence.getOrDefault(event.driverName, 0) + eventCount);
            if (!SafetyAnalysisStatus.isResolved(event.analysisStatus) && SafetySeverity.priority(event.severity) >= SafetySeverity.priority(SafetySeverity.HIGH)) {
                unresolvedCriticalVehicles.add(event.vehiclePlate);
            }
        }

        appendAlert(
            snapshot.priorityAlerts,
            criticalCount > 0,
            RefuelStatus.ALERT,
            "Eventos graves em aberto",
            criticalCount + " ocorrencia(s) de alta severidade exigem tratamento imediato."
        );
        appendAlert(
            snapshot.priorityAlerts,
            maxValue(vehicleRecurrence) >= 3,
            RefuelStatus.ATTENTION,
            "Recorrencia por veiculo",
            resolveTopKey(vehicleRecurrence) + " concentrou " + maxValue(vehicleRecurrence) + " evento(s) no periodo."
        );
        appendAlert(
            snapshot.priorityAlerts,
            maxValue(driverRecurrence) >= 3,
            RefuelStatus.ATTENTION,
            "Recorrencia por motorista",
            resolveTopKey(driverRecurrence) + " aparece com maior frequencia e precisa de acompanhamento."
        );
        appendAlert(
            snapshot.priorityAlerts,
            snapshot.evidenceCoveragePercent < 70d && snapshot.totalEvents > 0,
            RefuelStatus.ATTENTION,
            "Cobertura de evidencias abaixo da meta",
            String.format(Locale.US, "%.0f%% dos eventos possuem evidencia fotografica.", snapshot.evidenceCoveragePercent)
        );
        appendAlert(
            snapshot.priorityAlerts,
            snapshot.resolutionRatePercent < 60d && snapshot.totalEvents > 0,
            RefuelStatus.ATTENTION,
            "Baixa taxa de resolucao",
            String.format(Locale.US, "%.0f%% dos eventos foram encerrados no periodo atual.", snapshot.resolutionRatePercent)
        );
        if (snapshot.priorityAlerts.isEmpty()) {
            PriorityAlertItem item = new PriorityAlertItem();
            item.level = RefuelStatus.NORMAL;
            item.source = "SEGURANCA";
            item.title = "Cenario controlado";
            item.description = "Sem concentracao critica de eventos nas ultimas semanas.";
            snapshot.priorityAlerts.add(item);
        }
    }

    private void appendAlert(List<PriorityAlertItem> target, boolean condition, String level, String title, String description) {
        if (!condition) {
            return;
        }
        PriorityAlertItem item = new PriorityAlertItem();
        item.level = level;
        item.source = "SEGURANCA";
        item.title = title;
        item.description = description;
        target.add(item);
    }

    private String buildExecutiveSummary(SafetyDashboardSnapshot snapshot) {
        if (snapshot.totalEvents == 0) {
            return "Sem eventos registrados para compor leitura executiva.";
        }
        return "Cenario " + snapshot.riskLevel
            + ": " + snapshot.totalEvents + " evento(s) no periodo, "
            + snapshot.unresolvedEvents + " em aberto e "
            + String.format(Locale.US, "%.0f%%", snapshot.evidenceCoveragePercent)
            + " com evidencia.";
    }

    private double computeRiskIndex(List<SafetyEventEntity> allEvents, List<SafetyEventEntity> currentWindow) {
        if (allEvents == null || allEvents.isEmpty() || currentWindow == null) {
            return 0d;
        }
        Set<LocalDate> days = new HashSet<>();
        for (SafetyEventEntity event : allEvents) {
            LocalDate date = parseDate(event.occurredAtIso);
            if (date != null) {
                days.add(date);
            }
        }
        if (days.isEmpty()) {
            return 0d;
        }
        double historicalDailyAverage = (double) resolveTotalOccurrences(allEvents) / days.size();
        double currentDailyAverage = (double) resolveTotalOccurrences(currentWindow) / 30d;
        return historicalDailyAverage == 0d ? 0d : currentDailyAverage / historicalDailyAverage;
    }

    private String resolveRiskLevel(double riskIndex) {
        if (riskIndex >= 1.2d) {
            return "alto";
        }
        if (riskIndex >= 0.9d) {
            return "moderado";
        }
        return "controlado";
    }

    private int maxValue(Map<String, Integer> values) {
        int max = 0;
        for (int value : values.values()) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private String resolveTopKey(Map<String, Integer> values) {
        String bestKey = "";
        int bestValue = 0;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (entry.getValue() > bestValue) {
                bestValue = entry.getValue();
                bestKey = entry.getKey();
            }
        }
        return bestKey;
    }

    private double percentage(int value, int total) {
        if (total <= 0) {
            return 0d;
        }
        return (value * 100d) / total;
    }

    private LocalDate parseDate(String iso) {
        try {
            return LocalDateTime.parse(iso).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }

    private int resolveTotalOccurrences(List<SafetyEventEntity> events) {
        int total = 0;
        for (SafetyEventEntity event : events) {
            total += resolveOccurrenceCount(event);
        }
        return total;
    }

    private int resolveOccurrenceCount(SafetyEventEntity event) {
        if (event == null || event.occurrenceCount <= 0) {
            return 1;
        }
        return event.occurrenceCount;
    }
}
