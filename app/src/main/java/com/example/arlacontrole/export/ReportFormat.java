package com.example.arlacontrole.export;

public final class ReportFormat {

    public static final String PDF = "PDF";
    public static final String XLSX = "XLSX";

    private ReportFormat() {
    }

    public static boolean isPdf(String format) {
        return PDF.equals(format);
    }

    public static boolean isXlsx(String format) {
        return XLSX.equals(format);
    }
}
