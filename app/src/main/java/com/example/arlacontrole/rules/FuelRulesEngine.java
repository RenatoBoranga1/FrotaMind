package com.example.arlacontrole.rules;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;

public final class FuelRulesEngine {

    private FuelRulesEngine() {
    }

    public static FuelEvaluation evaluate(String fuelType, VehicleEntity vehicle, double liters, int odometerKm, RefuelEntity previousRecord) {
        if (FuelType.DIESEL.equals(fuelType)) {
            return evaluateDiesel(vehicle, liters, odometerKm, previousRecord);
        }
        return evaluateArla(vehicle, liters, odometerKm, previousRecord);
    }

    private static FuelEvaluation evaluateArla(VehicleEntity vehicle, double liters, int odometerKm, RefuelEntity previousRecord) {
        FuelEvaluation evaluation = baseEvaluation();

        if (previousRecord != null) {
            if (odometerKm <= previousRecord.odometerKm) {
                evaluation.status = RefuelStatus.ATTENTION;
                evaluation.reason = "Odometro menor ou igual ao ultimo registro do veiculo.";
                return evaluation;
            }
            evaluation.kmSinceLastSupply = odometerKm - previousRecord.odometerKm;
            if (evaluation.kmSinceLastSupply >= 100) {
                evaluation.litersPer1000Km = liters / (evaluation.kmSinceLastSupply / 1000d);
            }
        }

        boolean warningByFill = liters > vehicle.expectedFillMaxLiters;
        boolean alertByFill = liters > vehicle.expectedFillMaxLiters * 1.15d;
        boolean warningByEfficiency = evaluation.litersPer1000Km != null
            && evaluation.litersPer1000Km > vehicle.expectedPer1000KmMax;
        boolean alertByEfficiency = evaluation.litersPer1000Km != null
            && evaluation.litersPer1000Km > vehicle.expectedPer1000KmMax * 1.15d;

        if (alertByFill || alertByEfficiency) {
            evaluation.status = RefuelStatus.ALERT;
            evaluation.reason = "Consumo de ARLA acima da faixa esperada para a operacao.";
        } else if (warningByFill || warningByEfficiency) {
            evaluation.status = RefuelStatus.ATTENTION;
            evaluation.reason = "Consumo de ARLA acima do padrao e recomendado para verificacao.";
        }
        return evaluation;
    }

    private static FuelEvaluation evaluateDiesel(VehicleEntity vehicle, double liters, int odometerKm, RefuelEntity previousRecord) {
        FuelEvaluation evaluation = baseEvaluation();
        evaluation.reason = "Registro de diesel dentro do padrao esperado para o veiculo.";

        if (previousRecord != null) {
            if (odometerKm <= previousRecord.odometerKm) {
                evaluation.status = RefuelStatus.ATTENTION;
                evaluation.reason = "Odometro menor ou igual ao ultimo registro do veiculo.";
                return evaluation;
            }
            evaluation.kmSinceLastSupply = odometerKm - previousRecord.odometerKm;
            if (liters > 0d) {
                evaluation.kmPerLiter = evaluation.kmSinceLastSupply / liters;
            }
        }

        boolean warningByFill = liters > vehicle.expectedDieselFillMaxLiters;
        boolean alertByFill = liters > vehicle.expectedDieselFillMaxLiters * 1.15d;
        boolean warningByEfficiency = evaluation.kmPerLiter != null
            && evaluation.kmPerLiter < vehicle.expectedDieselKmPerLiterMin;
        boolean alertByEfficiency = evaluation.kmPerLiter != null
            && evaluation.kmPerLiter < vehicle.expectedDieselKmPerLiterMin * 0.85d;

        if (alertByFill || alertByEfficiency) {
            evaluation.status = RefuelStatus.ALERT;
            evaluation.reason = "Consumo de diesel fora da faixa esperada para o veiculo.";
        } else if (warningByFill || warningByEfficiency) {
            evaluation.status = RefuelStatus.ATTENTION;
            evaluation.reason = "Consumo de diesel acima do padrao e recomendado para verificacao.";
        }
        return evaluation;
    }

    private static FuelEvaluation baseEvaluation() {
        FuelEvaluation evaluation = new FuelEvaluation();
        evaluation.status = RefuelStatus.NORMAL;
        evaluation.reason = "Registro dentro do padrao esperado para o veiculo.";
        return evaluation;
    }
}
