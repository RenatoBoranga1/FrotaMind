package com.example.arlacontrole.model;

public final class RefuelStatus {
    public static final String NORMAL = "NORMAL";
    public static final String ATTENTION = "ATENCAO";
    public static final String ALERT = "ALERTA";

    private RefuelStatus() {
    }

    public static int priority(String status) {
        if (ALERT.equals(status)) {
            return 3;
        }
        if (ATTENTION.equals(status)) {
            return 2;
        }
        return 1;
    }
}
