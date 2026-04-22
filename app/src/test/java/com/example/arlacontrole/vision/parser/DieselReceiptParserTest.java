package com.example.arlacontrole.vision.parser;

import static org.junit.Assert.assertEquals;

import com.example.arlacontrole.vision.ExtractionResult;
import com.example.arlacontrole.vision.ExtractionStatus;

import org.junit.Test;

import java.util.Arrays;

public class DieselReceiptParserTest {

    private final DieselReceiptParser parser = new DieselReceiptParser();
    private final ExtractionValidator validator = new ExtractionValidator();

    @Test
    public void parse_extractsMainReceiptFields() {
        ExtractionResult result = validator.validate(parser.parse(
            "POSTO RITMO SUL\nCNPJ 12.345.678/0001-90\nVOLUME TOTAL 210,45 LT\nVALOR TOTAL R$ 1450,80\n14/04/2026 08:40\nPIX",
            Arrays.asList(
                "POSTO RITMO SUL",
                "CNPJ 12.345.678/0001-90",
                "VOLUME TOTAL 210,45 LT",
                "VALOR TOTAL R$ 1450,80",
                "14/04/2026 08:40",
                "PIX"
            )
        ));

        assertEquals(ExtractionStatus.CONFIDENT, result.status);
        assertEquals(210.45d, result.liters, 0.001d);
        assertEquals(1450.80d, result.totalAmount, 0.001d);
        assertEquals("POSTO RITMO SUL", result.locationName);
        assertEquals("PIX", result.paymentMethod);
    }

    @Test
    public void parse_returnsPartialWhenOnlyPartOfReceiptIsReadable() {
        ExtractionResult result = validator.validate(parser.parse(
            "POSTO RITMO SUL\nVALOR TOTAL 999,00",
            Arrays.asList("POSTO RITMO SUL", "VALOR TOTAL 999,00")
        ));

        assertEquals(ExtractionStatus.PARTIAL, result.status);
        assertEquals(999d, result.totalAmount, 0.001d);
    }
}
