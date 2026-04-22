package com.example.arlacontrole.utils;

import android.content.Context;

import com.example.arlacontrole.R;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetySeverity;
import com.example.arlacontrole.model.RefuelEntryMode;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;
import com.example.arlacontrole.vision.ExtractionStatus;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatUtils {

    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");
    private static final NumberFormat LITER_FORMAT = NumberFormat.getNumberInstance(LOCALE_PT_BR);
    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance(LOCALE_PT_BR);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
    private static final DecimalFormat EDITABLE_DECIMAL_FORMAT = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(LOCALE_PT_BR));
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_PT_BR);
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PT_BR);

    static {
        LITER_FORMAT.setMinimumFractionDigits(1);
        LITER_FORMAT.setMaximumFractionDigits(1);
        DECIMAL_FORMAT.setMinimumFractionDigits(1);
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
    }

    private FormatUtils() {
    }

    public static String formatLiters(double liters) {
        return LITER_FORMAT.format(liters) + " L";
    }

    public static String formatDecimal(Double value) {
        if (value == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(value);
    }

    public static String formatEditableDecimal(Double value) {
        if (value == null) {
            return "";
        }
        return EDITABLE_DECIMAL_FORMAT.format(value);
    }

    public static Double parseFlexibleDecimal(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        String sanitized = rawValue
            .trim()
            .replace("R$", "")
            .replace(" ", "")
            .replaceAll("[^0-9,.-]", "");
        if (sanitized.isEmpty() || "-".equals(sanitized)) {
            return null;
        }

        int lastComma = sanitized.lastIndexOf(',');
        int lastDot = sanitized.lastIndexOf('.');
        int decimalIndex = Math.max(lastComma, lastDot);
        String normalized;
        if (decimalIndex >= 0) {
            String integerPart = sanitized.substring(0, decimalIndex).replaceAll("[^0-9-]", "");
            String decimalPart = sanitized.substring(decimalIndex + 1).replaceAll("[^0-9]", "");
            if (decimalPart.isEmpty()) {
                decimalPart = "0";
            }
            normalized = integerPart + "." + decimalPart;
        } else {
            normalized = sanitized.replaceAll("[^0-9-]", "");
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static String formatAverage(Double value) {
        if (value == null || value <= 0d) {
            return "--";
        }
        return formatDecimal(value) + " L/1000 km";
    }

    public static String formatFuelMetric(Context context, String fuelType, Double value) {
        if (value == null || value <= 0d) {
            return context.getString(R.string.metric_not_available);
        }
        String formattedValue = formatDecimal(value);
        if (FuelType.DIESEL.equals(fuelType)) {
            return context.getString(R.string.metric_diesel_consumption, formattedValue);
        }
        return context.getString(R.string.metric_arla_consumption, formattedValue);
    }

    public static String formatFuelType(Context context, String fuelType) {
        if (FuelType.DIESEL.equals(fuelType)) {
            return context.getString(R.string.fuel_diesel);
        }
        return context.getString(R.string.fuel_arla);
    }

    public static String formatCurrency(Double value) {
        if (value == null || value <= 0d) {
            return "--";
        }
        return CURRENCY_FORMAT.format(value);
    }

    public static String formatKilometers(int value) {
        return NumberFormat.getIntegerInstance(LOCALE_PT_BR).format(value) + " km";
    }

    public static String formatDateTime(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return "--";
        }
        try {
            return LocalDateTime.parse(iso, INPUT_FORMATTER).format(DISPLAY_FORMATTER);
        } catch (Exception ignored) {
            return iso;
        }
    }

    public static String formatDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return "--";
        }
        try {
            return LocalDateTime.parse(iso, INPUT_FORMATTER).format(DATE_ONLY_FORMATTER);
        } catch (Exception ignored) {
            return iso;
        }
    }

    public static String formatStatus(Context context, String status) {
        if (RefuelStatus.ALERT.equals(status)) {
            return context.getString(R.string.status_alert);
        }
        if (RefuelStatus.ATTENTION.equals(status)) {
            return context.getString(R.string.status_attention);
        }
        return context.getString(R.string.status_normal);
    }

    public static String formatSafetyEventType(Context context, String type) {
        if (SafetyEventType.ACCIDENT.equals(type)) {
            return context.getString(R.string.safety_type_accident);
        }
        if (SafetyEventType.INCIDENT.equals(type)) {
            return context.getString(R.string.safety_type_incident);
        }
        if (SafetyEventType.NEAR_MISS.equals(type)) {
            return context.getString(R.string.safety_type_near_miss);
        }
        if (SafetyEventType.UNSAFE_CONDITION.equals(type)) {
            return context.getString(R.string.safety_type_unsafe_condition);
        }
        return context.getString(R.string.safety_type_unsafe_behavior);
    }

    public static String formatSafetySeverity(Context context, String severity) {
        if (SafetySeverity.CRITICAL.equals(severity)) {
            return context.getString(R.string.safety_severity_critical);
        }
        if (SafetySeverity.HIGH.equals(severity)) {
            return context.getString(R.string.safety_severity_high);
        }
        if (SafetySeverity.MODERATE.equals(severity)) {
            return context.getString(R.string.safety_severity_moderate);
        }
        return context.getString(R.string.safety_severity_low);
    }

    public static String formatSafetyAnalysisStatus(Context context, String status) {
        if (SafetyAnalysisStatus.IN_REVIEW.equals(status)) {
            return context.getString(R.string.safety_status_in_review);
        }
        if (SafetyAnalysisStatus.ACTION_PENDING.equals(status)) {
            return context.getString(R.string.safety_status_action_pending);
        }
        if (SafetyAnalysisStatus.RESOLVED.equals(status)) {
            return context.getString(R.string.safety_status_resolved);
        }
        return context.getString(R.string.safety_status_open);
    }

    public static String formatSyncStatus(Context context, String syncStatus) {
        if (SyncState.SYNCED.equals(syncStatus)) {
            return context.getString(R.string.sync_synced);
        }
        if (SyncState.FAILED.equals(syncStatus)) {
            return context.getString(R.string.sync_failed);
        }
        return context.getString(R.string.sync_pending);
    }

    public static String formatEntryMode(Context context, String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return context.getString(R.string.entry_mode_manual);
        }
        if (RefuelEntryMode.OCR_AUTO.equals(mode)) {
            return context.getString(R.string.entry_mode_ocr_auto);
        }
        if (RefuelEntryMode.OCR_REVIEWED.equals(mode)) {
            return context.getString(R.string.entry_mode_ocr_reviewed);
        }
        return context.getString(R.string.entry_mode_manual);
    }

    public static String formatExtractionStatus(Context context, String status) {
        if (status == null || status.trim().isEmpty()) {
            return context.getString(R.string.ocr_status_not_used);
        }
        if (ExtractionStatus.CONFIDENT.equals(status)) {
            return context.getString(R.string.ocr_status_confident);
        }
        if (ExtractionStatus.PARTIAL.equals(status)) {
            return context.getString(R.string.ocr_status_partial);
        }
        if (ExtractionStatus.REVIEW_REQUIRED.equals(status)) {
            return context.getString(R.string.ocr_status_review);
        }
        return context.getString(R.string.ocr_status_insufficient);
    }
}
