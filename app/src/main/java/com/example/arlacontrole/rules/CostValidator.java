package com.example.arlacontrole.rules;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.CostValidationResult;
import com.example.arlacontrole.model.FuelCostSnapshot;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;

public final class CostValidator {

    private static final double DIESEL_PRICE_MIN = 3.00d;
    private static final double DIESEL_PRICE_MAX = 10.00d;
    private static final double ARLA_PRICE_MIN = 1.50d;
    private static final double ARLA_PRICE_MAX = 8.00d;
    private static final double DIVERGENCE_TOLERANCE_PERCENT = 0.20d;
    private static final double HIGH_COST_PER_KM = 12.00d;

    private CostValidator() {
    }

    public static CostValidationResult validate(String fuelType, FuelCostSnapshot snapshot, RefuelEntity previousRecord) {
        CostValidationResult result = new CostValidationResult();
        if (snapshot == null || snapshot.liters == null || snapshot.liters <= 0d) {
            result.valid = false;
            result.level = RefuelStatus.ALERT;
            result.message = "Informe uma quantidade valida de litros para calcular os custos.";
            return result;
        }
        if (!snapshot.hasTotalAmount()) {
            result.valid = false;
            result.level = RefuelStatus.ALERT;
            result.message = "Informe valor total ou preco por litro para concluir o abastecimento.";
            return result;
        }
        if (!snapshot.hasPricePerLiter()) {
            result.valid = false;
            result.level = RefuelStatus.ALERT;
            result.message = "Nao foi possivel calcular o preco por litro deste abastecimento.";
            return result;
        }

        if (snapshot.totalAmount < 0d || snapshot.pricePerLiter < 0d) {
            result.valid = false;
            result.level = RefuelStatus.ALERT;
            result.message = "Valores financeiros nao podem ser negativos.";
            return result;
        }

        double minPrice = FuelType.DIESEL.equals(fuelType) ? DIESEL_PRICE_MIN : ARLA_PRICE_MIN;
        double maxPrice = FuelType.DIESEL.equals(fuelType) ? DIESEL_PRICE_MAX : ARLA_PRICE_MAX;
        if (snapshot.pricePerLiter < minPrice || snapshot.pricePerLiter > maxPrice) {
            result.level = RefuelStatus.ATTENTION;
            result.message = "Preco por litro fora da faixa esperada para " + fuelType + ".";
        }

        if (previousRecord != null && previousRecord.pricePerLiter > 0d) {
            double previousPrice = previousRecord.pricePerLiter;
            double delta = Math.abs(snapshot.pricePerLiter - previousPrice) / previousPrice;
            if (delta >= DIVERGENCE_TOLERANCE_PERCENT) {
                result.level = RefuelStatus.ATTENTION;
                result.message = "Preco por litro com variacao relevante em relacao ao ultimo abastecimento.";
            }
        }

        if (snapshot.hasCostPerKm() && snapshot.costPerKm >= HIGH_COST_PER_KM) {
            result.level = RefuelStatus.ALERT;
            result.message = "Custo por km acima do padrao esperado para a operacao.";
        }

        if (result.message == null || result.message.trim().isEmpty()) {
            result.message = "Leitura financeira dentro do padrao esperado.";
        }
        return result;
    }
}
