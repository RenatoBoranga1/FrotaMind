package com.example.arlacontrole.vision.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.arlacontrole.vision.ExtractionResult;
import com.example.arlacontrole.vision.ExtractionStatus;

import org.junit.Test;

import java.util.Arrays;

public class ArlaImageParserTest {

    private final ArlaImageParser parser = new ArlaImageParser();
    private final ExtractionValidator validator = new ExtractionValidator();

    @Test
    public void parse_detectsLitersFromPumpDisplay() {
        ExtractionResult result = validator.validate(parser.parse(
            "BOMBA 03\nVOLUME LITROS 32,7\n14/04/2026 10:32",
            Arrays.asList("BOMBA 03", "VOLUME LITROS 32,7", "14/04/2026 10:32")
        ));

        assertEquals(ExtractionStatus.CONFIDENT, result.status);
        assertEquals(32.7d, result.liters, 0.001d);
        assertEquals("03", result.pumpNumber);
    }

    @Test
    public void parse_marksInsufficientWhenNoUsefulReadingExists() {
        ExtractionResult result = validator.validate(parser.parse(
            "REFLEXO ILEGIVEL",
            Arrays.asList("REFLEXO ILEGIVEL")
        ));

        assertEquals(ExtractionStatus.INSUFFICIENT, result.status);
        assertNull(result.liters);
    }
}
