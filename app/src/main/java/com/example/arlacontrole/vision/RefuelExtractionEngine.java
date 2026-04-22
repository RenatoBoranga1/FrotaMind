package com.example.arlacontrole.vision;

import android.content.Context;

import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.vision.ocr.OcrProcessor;
import com.example.arlacontrole.vision.parser.ArlaImageParser;
import com.example.arlacontrole.vision.parser.DieselReceiptParser;
import com.example.arlacontrole.vision.parser.ExtractionValidator;

public class RefuelExtractionEngine {

    public interface Callback {
        void onSuccess(ExtractionResult result);

        void onError(String message);
    }

    private final OcrProcessor ocrProcessor;
    private final ArlaImageParser arlaImageParser = new ArlaImageParser();
    private final DieselReceiptParser dieselReceiptParser = new DieselReceiptParser();
    private final ExtractionValidator extractionValidator = new ExtractionValidator();

    public RefuelExtractionEngine(OcrProcessor ocrProcessor) {
        this.ocrProcessor = ocrProcessor;
    }

    public void analyze(Context context, String fuelType, String imagePath, Callback callback) {
        ocrProcessor.process(context, imagePath, new OcrProcessor.Callback() {
            @Override
            public void onSuccess(String rawText, java.util.List<String> lines) {
                ExtractionResult result = FuelType.DIESEL.equals(fuelType)
                    ? dieselReceiptParser.parse(rawText, lines)
                    : arlaImageParser.parse(rawText, lines);
                callback.onSuccess(extractionValidator.validate(result));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
