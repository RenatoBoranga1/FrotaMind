package com.example.arlacontrole.vision.parser;

import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.vision.ExtractionResult;
import com.example.arlacontrole.vision.ExtractionStatus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DieselReceiptParser {

    private static final Pattern CNPJ_PATTERN = Pattern.compile("(\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2})");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("(?:DOC|DOCUMENTO|CUPOM|NFC-E|NFCE|COO|CCF)\\D{0,8}(\\d{3,20})", Pattern.CASE_INSENSITIVE);

    public ExtractionResult parse(String rawText, List<String> recognizedLines) {
        ExtractionResult result = new ExtractionResult();
        result.fuelType = FuelType.DIESEL;
        result.rawText = rawText == null ? "" : rawText;

        List<String> lines = ParsingUtils.splitLines(rawText, recognizedLines);
        double litersScore = Double.NEGATIVE_INFINITY;
        double totalScore = Double.NEGATIVE_INFINITY;

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            String normalized = ParsingUtils.normalize(line);

            if (result.cnpj.isEmpty()) {
                Matcher cnpjMatcher = CNPJ_PATTERN.matcher(line);
                if (cnpjMatcher.find()) {
                    result.cnpj = cnpjMatcher.group(1);
                }
            }

            if (result.documentNumber.isEmpty()) {
                Matcher documentMatcher = DOCUMENT_PATTERN.matcher(line);
                if (documentMatcher.find()) {
                    result.documentNumber = documentMatcher.group(1);
                }
            }

            if (result.paymentMethod.isEmpty()) {
                if (normalized.contains("PIX")) {
                    result.paymentMethod = "PIX";
                } else if (normalized.contains("DEBITO")) {
                    result.paymentMethod = "DEBITO";
                } else if (normalized.contains("CREDITO")) {
                    result.paymentMethod = "CREDITO";
                } else if (normalized.contains("DINHEIRO")) {
                    result.paymentMethod = "DINHEIRO";
                }
            }

            if (result.locationName.isEmpty() && index < 5 && isStationCandidate(normalized)) {
                result.locationName = ParsingUtils.clean(line);
            }
            if (result.locationName.isEmpty() && normalized.contains("POSTO")) {
                result.locationName = ParsingUtils.clean(line);
            }

            String date = ParsingUtils.extractDate(line);
            String time = ParsingUtils.extractTime(line);
            if (!date.isEmpty() && result.suppliedAtIso.isEmpty()) {
                result.suppliedAtIso = ParsingUtils.buildIsoDateTime(date, time);
            }

            List<Double> candidates = ParsingUtils.extractNumericCandidates(line);
            for (Double candidate : candidates) {
                double candidateLitersScore = scoreLiters(line, candidate);
                if (candidateLitersScore > 0d && candidateLitersScore > litersScore) {
                    litersScore = candidateLitersScore;
                    result.liters = candidate;
                }

                double candidateTotalScore = scoreTotal(line, candidate);
                if (candidateTotalScore > 0d && candidateTotalScore > totalScore) {
                    totalScore = candidateTotalScore;
                    result.totalAmount = candidate;
                }
            }
        }

        if (result.liters != null && result.totalAmount != null && (!result.suppliedAtIso.isEmpty() || !result.locationName.isEmpty())) {
            result.status = ExtractionStatus.CONFIDENT;
            result.summaryMessage = "Leitura confiavel do comprovante de diesel.";
        } else if (result.liters != null || result.totalAmount != null || !result.locationName.isEmpty()) {
            result.status = ExtractionStatus.PARTIAL;
            result.summaryMessage = "Alguns dados do comprovante foram lidos. Revise antes de salvar.";
        } else {
            result.status = ExtractionStatus.INSUFFICIENT;
            result.summaryMessage = "Nao foi possivel identificar os dados principais do comprovante.";
        }

        return result;
    }

    private boolean isStationCandidate(String normalizedLine) {
        return normalizedLine.length() >= 5
            && !normalizedLine.contains("CNPJ")
            && !normalizedLine.contains("DOCUMENTO")
            && !normalizedLine.contains("TOTAL")
            && !normalizedLine.matches(".*\\d{5,}.*");
    }

    private double scoreLiters(String line, Double value) {
        double score = 0d;
        if (value == null) {
            return score;
        }
        if (value >= 1d && value <= 1500d) {
            score += 3d;
        } else {
            score -= 4d;
        }
        if (ParsingUtils.containsKeyword(line, "LITRO", "LTS", "LT", "VOLUME", "QTD", "QUANT")) {
            score += 5d;
        } else {
            score -= 3d;
        }
        if (ParsingUtils.containsKeyword(line, "TOTAL", "VALOR", "R$")) {
            score -= 2d;
        }
        return score;
    }

    private double scoreTotal(String line, Double value) {
        double score = 0d;
        if (value == null) {
            return score;
        }
        if (value >= 1d && value <= 50000d) {
            score += 2d;
        } else {
            score -= 4d;
        }
        if (ParsingUtils.containsKeyword(line, "TOTAL", "VALOR", "R$", "VL TOTAL")) {
            score += 5d;
        }
        if (ParsingUtils.containsKeyword(line, "LITRO", "LTS", "LT", "VOLUME")) {
            score -= 2d;
        }
        return score;
    }
}
