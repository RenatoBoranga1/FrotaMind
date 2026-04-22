package com.example.arlacontrole.vision.parser;

import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.vision.ExtractionResult;
import com.example.arlacontrole.vision.ExtractionStatus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArlaImageParser {

    private static final Pattern PUMP_PATTERN = Pattern.compile("(?:BOMBA|BICO|PUMP)\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);

    public ExtractionResult parse(String rawText, List<String> recognizedLines) {
        ExtractionResult result = new ExtractionResult();
        result.fuelType = FuelType.ARLA;
        result.rawText = rawText == null ? "" : rawText;

        List<String> lines = ParsingUtils.splitLines(rawText, recognizedLines);
        double bestScore = Double.NEGATIVE_INFINITY;
        Double litersCandidate = null;

        for (String line : lines) {
            String date = ParsingUtils.extractDate(line);
            String time = ParsingUtils.extractTime(line);
            if (!date.isEmpty() && result.suppliedAtIso.isEmpty()) {
                result.suppliedAtIso = ParsingUtils.buildIsoDateTime(date, time);
            }

            if (result.pumpNumber.isEmpty()) {
                Matcher matcher = PUMP_PATTERN.matcher(line);
                if (matcher.find()) {
                    result.pumpNumber = matcher.group(1);
                }
            }

            List<Double> candidates = ParsingUtils.extractNumericCandidates(line);
            for (Double candidate : candidates) {
                double score = scoreLitersCandidate(line, candidate);
                if (score > bestScore) {
                    bestScore = score;
                    litersCandidate = candidate;
                }
            }
        }

        result.liters = litersCandidate;
        if (litersCandidate == null) {
            result.status = ExtractionStatus.INSUFFICIENT;
            result.summaryMessage = "Nao foi possivel localizar os litros da bomba com seguranca.";
        } else if (bestScore >= 7d) {
            result.status = ExtractionStatus.CONFIDENT;
            result.summaryMessage = "Leitura confiavel do visor da bomba de ARLA.";
        } else {
            result.status = ExtractionStatus.PARTIAL;
            result.summaryMessage = "Leitura parcial da bomba. Revise os litros antes de salvar.";
        }
        return result;
    }

    private double scoreLitersCandidate(String line, Double value) {
        double score = 0d;
        if (value == null) {
            return score;
        }
        if (value >= 1d && value <= 120d) {
            score += 4d;
        } else {
            score -= 4d;
        }
        if (value % 1d != 0d) {
            score += 1.5d;
        }
        if (ParsingUtils.containsKeyword(line, "LITRO", "LTS", "LT", "VOLUME", "QTD", "QTY")) {
            score += 4d;
        }
        if (ParsingUtils.containsKeyword(line, "TOTAL", "VALOR", "R$")) {
            score -= 2d;
        }
        if (ParsingUtils.looksLikeDateOrTime(line)) {
            score -= 1d;
        }
        return score;
    }
}
