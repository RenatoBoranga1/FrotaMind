package com.example.arlacontrole.model;

public final class SafetyAnalysisStatus {
    public static final String OPEN = "ABERTO";
    public static final String IN_REVIEW = "EM_ANALISE";
    public static final String ACTION_PENDING = "PLANO_DE_ACAO";
    public static final String RESOLVED = "RESOLVIDO";

    private SafetyAnalysisStatus() {
    }

    public static boolean isResolved(String status) {
        return RESOLVED.equals(status);
    }
}
