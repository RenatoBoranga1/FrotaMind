package com.example.arlacontrole.vision.parser;

import com.example.arlacontrole.utils.FormatUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ParsingUtils {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,4}(?:[\\.,]\\d{1,3})?)(?!\\d)");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2}[/-]\\d{2}[/-]\\d{2,4})");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{2}:\\d{2}(?::\\d{2})?)");

    private ParsingUtils() {
    }

    static List<String> splitLines(String rawText, List<String> fallbackLines) {
        if (fallbackLines != null && !fallbackLines.isEmpty()) {
            return fallbackLines;
        }
        List<String> lines = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return lines;
        }
        String[] parts = rawText.split("\\r?\\n");
        for (String part : parts) {
            String value = clean(part);
            if (!value.isEmpty()) {
                lines.add(value);
            }
        }
        return lines;
    }

    static List<Double> extractNumericCandidates(String line) {
        List<Double> values = new ArrayList<>();
        if (line == null) {
            return values;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(line);
        while (matcher.find()) {
            String token = matcher.group(1);
            if (looksLikeDateOrTime(token)) {
                continue;
            }
            Double value = FormatUtils.parseFlexibleDecimal(token);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    static String extractDate(String text) {
        return extractFirst(text, DATE_PATTERN);
    }

    static String extractTime(String text) {
        return extractFirst(text, TIME_PATTERN);
    }

    static String buildIsoDateTime(String dateText, String timeText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return "";
        }
        LocalDate date = parseDate(dateText);
        if (date == null) {
            return "";
        }
        LocalTime time = parseTime(timeText);
        if (time == null) {
            time = LocalTime.of(0, 0);
        }
        return LocalDateTime.of(date, time).withSecond(0).withNano(0).toString();
    }

    static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    static boolean containsKeyword(String line, String... keywords) {
        String normalized = normalize(line);
        for (String keyword : keywords) {
            if (normalized.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    static String normalize(String value) {
        return clean(value).toUpperCase(Locale.ROOT);
    }

    static boolean looksLikeDateOrTime(String value) {
        return value != null && (value.contains("/") || value.contains(":"));
    }

    private static String extractFirst(String value, Pattern pattern) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static LocalDate parseDate(String value) {
        String[] patterns = {"dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern, Locale.ROOT));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String[] patterns = {"HH:mm:ss", "HH:mm"};
        for (String pattern : patterns) {
            try {
                return LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern, Locale.ROOT));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
