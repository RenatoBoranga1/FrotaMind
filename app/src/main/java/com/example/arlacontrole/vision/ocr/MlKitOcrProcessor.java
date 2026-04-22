package com.example.arlacontrole.vision.ocr;

import android.content.Context;
import android.net.Uri;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MlKitOcrProcessor implements OcrProcessor {

    @Override
    public void process(Context context, String imagePath, Callback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, Uri.fromFile(new File(imagePath)));
            com.google.mlkit.vision.text.TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                .addOnSuccessListener(result -> {
                    List<String> lines = extractLines(result);
                    recognizer.close();
                    callback.onSuccess(result.getText() == null ? "" : result.getText(), lines);
                })
                .addOnFailureListener(error -> {
                    recognizer.close();
                    callback.onError(error.getMessage() == null ? "Nao foi possivel analisar a imagem." : error.getMessage());
                });
        } catch (Exception exception) {
            callback.onError(exception.getMessage() == null ? "Nao foi possivel carregar a imagem para leitura." : exception.getMessage());
        }
    }

    private List<String> extractLines(Text result) {
        List<String> lines = new ArrayList<>();
        if (result == null) {
            return lines;
        }
        for (Text.TextBlock block : result.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                if (line.getText() != null && !line.getText().trim().isEmpty()) {
                    lines.add(line.getText().trim());
                }
            }
        }
        return lines;
    }
}
