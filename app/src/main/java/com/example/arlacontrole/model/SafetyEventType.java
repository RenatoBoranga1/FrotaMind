package com.example.arlacontrole.model;

public final class SafetyEventType {
    public static final String ACCIDENT = "ACIDENTE";
    public static final String INCIDENT = "INCIDENTE";
    public static final String NEAR_MISS = "QUASE_ACIDENTE";
    public static final String UNSAFE_CONDITION = "CONDICAO_INSEGURA";
    public static final String UNSAFE_BEHAVIOR = "COMPORTAMENTO_INSEGURO";

    private SafetyEventType() {
    }

    public static int weight(String type) {
        if (ACCIDENT.equals(type)) {
            return 4;
        }
        if (INCIDENT.equals(type)) {
            return 3;
        }
        if (NEAR_MISS.equals(type)) {
            return 2;
        }
        return 1;
    }
}
