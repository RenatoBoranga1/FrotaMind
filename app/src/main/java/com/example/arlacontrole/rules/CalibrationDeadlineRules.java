package com.example.arlacontrole.rules;

import com.example.arlacontrole.model.CalibrationDeadlineStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class CalibrationDeadlineRules {

    public static final String LEVEL_OK = "OK";
    public static final String LEVEL_WARNING = "WARNING";
    public static final String LEVEL_OVERDUE = "OVERDUE";
    private static final int CALIBRATION_VALID_DAYS = 60;
    private static final int WARNING_WINDOW_DAYS = 10;

    private CalibrationDeadlineRules() {
    }

    public static CalibrationDeadlineStatus evaluate(String calibrationAtIso) {
        return evaluate(calibrationAtIso, LocalDate.now());
    }

    public static CalibrationDeadlineStatus evaluate(String calibrationAtIso, LocalDate referenceDate) {
        CalibrationDeadlineStatus result = new CalibrationDeadlineStatus();
        LocalDate calibrationDate = parseDate(calibrationAtIso);
        LocalDate today = referenceDate == null ? LocalDate.now() : referenceDate;
        if (calibrationDate == null) {
            result.level = LEVEL_OVERDUE;
            result.message = "Afericao ainda nao registrada.";
            return result;
        }

        long daysSinceCalibration = ChronoUnit.DAYS.between(calibrationDate, today);
        int remainingDays = (int) (CALIBRATION_VALID_DAYS - daysSinceCalibration);
        result.daysRemaining = Math.max(remainingDays, 0);
        result.daysOverdue = Math.max(-remainingDays, 0);

        if (remainingDays < 0) {
            result.level = LEVEL_OVERDUE;
            result.message = "Afericao vencida ha " + result.daysOverdue + " dia(s).";
            return result;
        }
        if (remainingDays <= WARNING_WINDOW_DAYS) {
            result.level = LEVEL_WARNING;
            result.message = "Faltam " + remainingDays + " dia(s) para a proxima afericao.";
            return result;
        }
        result.level = LEVEL_OK;
        result.message = "Afericao em dia.";
        return result;
    }

    private static LocalDate parseDate(String calibrationAtIso) {
        if (calibrationAtIso == null || calibrationAtIso.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(calibrationAtIso).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }
}
