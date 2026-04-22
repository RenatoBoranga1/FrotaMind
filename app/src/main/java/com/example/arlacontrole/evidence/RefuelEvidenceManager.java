package com.example.arlacontrole.evidence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.model.FuelType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class RefuelEvidenceManager {

    public static final String CATEGORY_ARLA_PUMP = "ARLA_PUMP_DISPLAY";
    public static final String CATEGORY_DIESEL_RECEIPT = "DIESEL_RECEIPT";
    public static final String CATEGORY_SAFETY_OCCURRENCE = "SAFETY_OCCURRENCE";

    private static final String EVIDENCE_FOLDER = "refuel_evidence";

    private RefuelEvidenceManager() {
    }

    public static String resolveCategory(String fuelType) {
        if (FuelType.DIESEL.equals(fuelType)) {
            return CATEGORY_DIESEL_RECEIPT;
        }
        return CATEGORY_ARLA_PUMP;
    }

    public static String buildInstruction(Context context, String fuelType) {
        if (FuelType.DIESEL.equals(fuelType)) {
            return context.getString(R.string.evidence_instruction_diesel);
        }
        return context.getString(R.string.evidence_instruction_arla);
    }

    public static String buildButtonLabel(Context context, String fuelType, boolean hasEvidence) {
        if (FuelType.DIESEL.equals(fuelType)) {
            return context.getString(hasEvidence ? R.string.evidence_retake_diesel : R.string.evidence_capture_diesel);
        }
        return context.getString(hasEvidence ? R.string.evidence_retake_arla : R.string.evidence_capture_arla);
    }

    public static String buildTitle(Context context, String category) {
        if (CATEGORY_DIESEL_RECEIPT.equals(category)) {
            return context.getString(R.string.evidence_title_diesel);
        }
        return context.getString(R.string.evidence_title_arla);
    }

    public static File createEvidenceFile(Context context, String fuelType) throws IOException {
        return createEvidenceFile(context, fuelType, EVIDENCE_FOLDER);
    }

    public static File createSafetyEvidenceFile(Context context) throws IOException {
        return createEvidenceFile(context, CATEGORY_SAFETY_OCCURRENCE, "safety_evidence");
    }

    private static File createEvidenceFile(Context context, String contextType, String folderName) throws IOException {
        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File baseDir = picturesDir == null
            ? new File(context.getCacheDir(), folderName)
            : new File(picturesDir, folderName);
        if (!baseDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US));
        String prefix;
        if (CATEGORY_SAFETY_OCCURRENCE.equals(contextType)) {
            prefix = "safety_";
        } else {
            prefix = FuelType.DIESEL.equals(contextType) ? "diesel_" : "arla_";
        }
        File file = new File(baseDir, prefix + timestamp + ".jpg");
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        return file;
    }

    public static Uri buildContentUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public static boolean hasEvidence(String filePath) {
        return filePath != null && !filePath.trim().isEmpty() && new File(filePath).exists();
    }

    public static void deleteQuietly(String filePath) {
        if (!hasEvidence(filePath)) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        new File(filePath).delete();
    }

    public static Bitmap loadPreview(String filePath, int reqWidth, int reqHeight) {
        if (!hasEvidence(filePath)) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bounds);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight);
        return BitmapFactory.decodeFile(filePath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int sample = 1;
        while ((height / sample) > reqHeight || (width / sample) > reqWidth) {
            sample *= 2;
        }
        return Math.max(1, sample);
    }
}
