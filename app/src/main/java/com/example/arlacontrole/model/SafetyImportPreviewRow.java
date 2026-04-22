package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class SafetyImportPreviewRow {
    public int rowNumber;
    public String importRecordId = "";
    public String driverName = "";
    public long driverId;
    public boolean driverMatched;
    public String vehiclePlate = "";
    public String vehicleFleetCode = "";
    public String vehicleModel = "";
    public boolean vehicleMatched;
    public String rawEventType = "";
    public String normalizedEventType = "";
    public String description = "";
    public String severity = "";
    public String locationName = "";
    public String probableCause = "";
    public String notes = "";
    public String analysisStatus = "";
    public String occurredAtIso = "";
    public int occurrenceCount = 1;
    public boolean hasEvidence;
    public boolean duplicate;
    public boolean valid;
    public boolean suspect;
    public final List<String> issues = new ArrayList<>();

    public String buildPayloadSummary() {
        return "Motorista: " + driverName
            + " | Veiculo: " + vehiclePlate
            + " | Evento: " + rawEventType
            + " | Data: " + occurredAtIso;
    }
}
