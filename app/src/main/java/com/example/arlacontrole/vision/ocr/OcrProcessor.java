package com.example.arlacontrole.vision.ocr;

import android.content.Context;

import java.util.List;

public interface OcrProcessor {

    interface Callback {
        void onSuccess(String rawText, List<String> lines);

        void onError(String message);
    }

    void process(Context context, String imagePath, Callback callback);
}
