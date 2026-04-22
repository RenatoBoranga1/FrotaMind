package com.example.arlacontrole.analytics.calculators;

import com.example.arlacontrole.analytics.models.OperationalAlertItem;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.RefuelEntryMode;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;
import com.example.arlacontrole.vision.ExtractionStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlertEngine {

    public List<OperationalAlertItem> build(List<RefuelEntity> records) {
        List<OperationalAlertItem> alerts = new ArrayList<>();
        if (records == null) {
            return alerts;
        }

        LocalDateTime now = LocalDateTime.now();
        for (RefuelEntity entity : records) {
            OperationalAlertItem alert = buildForEntity(entity, now);
            if (alert != null) {
                alerts.add(alert);
            }
        }

        Collections.sort(alerts, Comparator
            .comparingInt((OperationalAlertItem value) -> severityPriority(value.severity)).reversed()
            .thenComparing((OperationalAlertItem value) -> value.suppliedAtIso, Comparator.reverseOrder()));
        return alerts;
    }

    public boolean hasOperationalAlert(RefuelEntity entity) {
        return buildForEntity(entity, LocalDateTime.now()) != null;
    }

    private OperationalAlertItem buildForEntity(RefuelEntity entity, LocalDateTime now) {
        if (entity == null) {
            return null;
        }

        LocalDateTime suppliedAt = parseDateTime(entity.suppliedAtIso, now);
        if (!entity.hasEvidence()) {
            return createAlert(entity, "Evidencia", RefuelStatus.ALERT,
                "Registro sem evidencia obrigatoria",
                "O abastecimento precisa de foto vinculada para auditoria operacional.",
                buildSupporting(entity));
        }
        if (!entity.hasChecklist()) {
            return createAlert(entity, "Checklist", RefuelStatus.ALERT,
                "Checklist operacional ausente",
                "O registro foi salvo sem checklist completo e exige verificacao da operacao.",
                buildSupporting(entity));
        }
        if (SyncState.FAILED.equals(entity.syncStatus)) {
            return createAlert(entity, "Sincronizacao", RefuelStatus.ALERT,
                "Falha de sincronizacao",
                "O abastecimento ainda nao foi entregue ao backend e precisa de reenvio.",
                buildSupporting(entity));
        }
        if (RefuelStatus.ALERT.equals(entity.statusLevel)) {
            return createAlert(entity, "Consumo", RefuelStatus.ALERT,
                "Consumo fora do padrao",
                emptyFallback(entity.statusReason, "O abastecimento ficou acima da faixa esperada para o veiculo."),
                buildSupporting(entity));
        }
        if (RefuelStatus.ATTENTION.equals(entity.statusLevel)) {
            return createAlert(entity, "Analise", RefuelStatus.ATTENTION,
                "Abastecimento requer atencao",
                emptyFallback(entity.statusReason, "Existe um desvio que deve ser revisado pela operacao."),
                buildSupporting(entity));
        }
        if (ExtractionStatus.REVIEW_REQUIRED.equals(entity.ocrStatus) || RefuelEntryMode.OCR_REVIEWED.equals(entity.dataEntryMode)) {
            return createAlert(entity, "OCR", RefuelStatus.ATTENTION,
                "Leitura automatica revisada manualmente",
                "A leitura da imagem precisou de revisao e merece conferenca adicional.",
                buildSupporting(entity));
        }
        if (SyncState.PENDING.equals(entity.syncStatus) && Duration.between(suppliedAt, now).toHours() >= 12) {
            return createAlert(entity, "Fila", RefuelStatus.ATTENTION,
                "Registro aguardando sincronizacao",
                "O abastecimento segue pendente ha mais de 12 horas.",
                buildSupporting(entity));
        }
        return null;
    }

    private OperationalAlertItem createAlert(
        RefuelEntity entity,
        String category,
        String severity,
        String title,
        String description,
        String supportingText
    ) {
        OperationalAlertItem item = new OperationalAlertItem();
        item.refuelLocalId = entity.localId;
        item.category = category;
        item.severity = severity;
        item.title = title;
        item.description = description;
        item.supportingText = supportingText;
        item.vehiclePlate = entity.vehiclePlate;
        item.suppliedAtIso = entity.suppliedAtIso;
        return item;
    }

    private String buildSupporting(RefuelEntity entity) {
        return entity.vehiclePlate + " | " + entity.driverName + " | " + entity.locationName;
    }

    private String emptyFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private LocalDateTime parseDateTime(String suppliedAtIso, LocalDateTime fallback) {
        try {
            return LocalDateTime.parse(suppliedAtIso);
        } catch (Exception ignored) {
            return fallback == null ? LocalDate.now().atStartOfDay() : fallback;
        }
    }

    private int severityPriority(String severity) {
        if (RefuelStatus.ALERT.equals(severity)) {
            return 3;
        }
        if (RefuelStatus.ATTENTION.equals(severity)) {
            return 2;
        }
        return 1;
    }
}
