package com.example.arlacontrole.model;

public final class SafetySeverity {
    public static final String LOW = "BAIXA";
    public static final String MODERATE = "MODERADA";
    public static final String HIGH = "ALTA";
    public static final String CRITICAL = "CRITICA";

    private SafetySeverity() {
    }

    public static int weight(String severity) {
        if (CRITICAL.equals(severity)) {
            return 4;
        }
        if (HIGH.equals(severity)) {
            return 3;
        }
        if (MODERATE.equals(severity)) {
            return 2;
        }
        return 1;
    }

    public static int priority(String severity) {
        return weight(severity);
    }
}
