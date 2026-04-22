package com.example.arlacontrole.vision;

import org.json.JSONObject;

public class ExtractionResult {
    public String fuelType = "";
    public String status = ExtractionStatus.INSUFFICIENT;
    public String summaryMessage = "";
    public String rawText = "";
    public Double liters;
    public Double totalAmount;
    public String suppliedAtIso = "";
    public String locationName = "";
    public String pumpNumber = "";
    public String cnpj = "";
    public String documentNumber = "";
    public String paymentMethod = "";

    public boolean hasAnySuggestion() {
        return liters != null
            || totalAmount != null
            || (suppliedAtIso != null && !suppliedAtIso.trim().isEmpty())
            || (locationName != null && !locationName.trim().isEmpty());
    }

    public String toMetadataJson() {
        try {
            JSONObject object = new JSONObject();
            object.put("pump_number", pumpNumber == null ? "" : pumpNumber);
            object.put("cnpj", cnpj == null ? "" : cnpj);
            object.put("document_number", documentNumber == null ? "" : documentNumber);
            object.put("payment_method", paymentMethod == null ? "" : paymentMethod);
            object.put("status", status == null ? "" : status);
            return object.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }
}
