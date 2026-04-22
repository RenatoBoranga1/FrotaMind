package com.example.arlacontrole.model;

import java.time.LocalDate;

public class OperationalFilter {

    public String startDateIso = "";
    public String endDateIso = "";
    public String vehiclePlate = "";
    public String driverName = "";
    public String eventType = "";

    public OperationalFilter copy() {
        OperationalFilter copy = new OperationalFilter();
        copy.startDateIso = startDateIso;
        copy.endDateIso = endDateIso;
        copy.vehiclePlate = vehiclePlate;
        copy.driverName = driverName;
        copy.eventType = eventType;
        return copy;
    }

    public LocalDate resolveStartDate() {
        if (startDateIso == null || startDateIso.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(startDateIso.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    public LocalDate resolveEndDate() {
        if (endDateIso == null || endDateIso.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(endDateIso.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isEmpty() {
        return safe(startDateIso).isEmpty()
            && safe(endDateIso).isEmpty()
            && safe(vehiclePlate).isEmpty()
            && safe(driverName).isEmpty()
            && safe(eventType).isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
