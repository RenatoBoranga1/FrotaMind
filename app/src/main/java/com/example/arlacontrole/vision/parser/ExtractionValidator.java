package com.example.arlacontrole.vision.parser;

import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.vision.ExtractionResult;
import com.example.arlacontrole.vision.ExtractionStatus;

public class ExtractionValidator {

    public ExtractionResult validate(ExtractionResult result) {
        if (result == null) {
            return new ExtractionResult();
        }
        if (result.rawText == null || result.rawText.trim().length() < 4) {
            result.status = ExtractionStatus.INSUFFICIENT;
            result.summaryMessage = "Nao foi possivel ler a imagem com seguranca. Tente novamente.";
            return result;
        }

        if (FuelType.ARLA.equals(result.fuelType)) {
            validateArla(result);
        } else {
            validateDiesel(result);
        }
        return result;
    }

    private void validateArla(ExtractionResult result) {
        if (result.liters == null || result.liters <= 0d || result.liters > 120d) {
            result.liters = null;
            result.status = ExtractionStatus.INSUFFICIENT;
            result.summaryMessage = "Leitura insuficiente do visor da bomba. Tire outra foto ou preencha manualmente.";
            return;
        }
        if (ExtractionStatus.CONFIDENT.equals(result.status) && result.liters < 2d) {
            result.status = ExtractionStatus.REVIEW_REQUIRED;
            result.summaryMessage = "Os litros parecem muito baixos. Revise o visor antes de salvar.";
        }
    }

    private void validateDiesel(ExtractionResult result) {
        if (result.liters != null && (result.liters <= 0d || result.liters > 1500d)) {
            result.liters = null;
        }
        if (result.totalAmount != null && (result.totalAmount <= 0d || result.totalAmount > 50000d)) {
            result.totalAmount = null;
        }

        if (result.liters == null && result.totalAmount == null && (result.locationName == null || result.locationName.trim().isEmpty())) {
            result.status = ExtractionStatus.INSUFFICIENT;
            result.summaryMessage = "Nao foi possivel ler o comprovante com seguranca. Tente novamente.";
            return;
        }
        if (result.liters == null || result.totalAmount == null) {
            result.status = ExtractionStatus.PARTIAL;
            result.summaryMessage = "Leitura parcial do comprovante. Revise litros, valor e data antes de salvar.";
        }
    }
}
