package com.example.arlacontrole.rules;

import com.example.arlacontrole.model.RefuelOdometerValidation;

public final class RefuelOdometerRules {

    public static final int DIVERGENCE_WARNING_THRESHOLD_KM = 100;

    private RefuelOdometerRules() {
    }

    public static RefuelOdometerValidation validate(Integer expectedInitialKm, int informedInitialKm, int informedFinalKm) {
        RefuelOdometerValidation result = new RefuelOdometerValidation();
        if (informedInitialKm <= 0) {
            result.valid = false;
            result.message = "Informe um odometro inicial valido.";
            return result;
        }
        if (informedFinalKm <= 0) {
            result.valid = false;
            result.message = "Informe um odometro final valido.";
            return result;
        }
        if (informedFinalKm <= informedInitialKm) {
            result.valid = false;
            result.message = "O odometro final deve ser maior que o odometro inicial.";
            return result;
        }

        result.valid = true;
        result.calculatedDistanceKm = informedFinalKm - informedInitialKm;
        if (expectedInitialKm != null && expectedInitialKm > 0) {
            result.divergenceKm = Math.abs(informedInitialKm - expectedInitialKm);
            result.hasDivergenceWarning = result.divergenceKm >= DIVERGENCE_WARNING_THRESHOLD_KM;
            if (result.hasDivergenceWarning) {
                result.message = "O odometro inicial diverge " + result.divergenceKm + " km do ultimo final registrado.";
            }
        }
        if (result.message == null || result.message.trim().isEmpty()) {
            result.message = "Controle ARLA calculado com base na diferenca entre os odometros.";
        }
        return result;
    }
}
