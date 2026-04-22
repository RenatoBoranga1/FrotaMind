package com.example.arlacontrole.media;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.core.content.FileProvider;

import com.example.arlacontrole.model.CalibrationMediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class CalibrationMediaManager {

    private static final String PHOTO_FOLDER = "Pictures/calibration_media";
    private static final String VIDEO_FOLDER = "Movies/calibration_media";

    private CalibrationMediaManager() {
    }

    public static File createPhotoFile(Context context) throws IOException {
        return createMediaFile(context, CalibrationMediaType.PHOTO, ".jpg");
    }

    public static String copyPhotoToLocal(Context context, Uri sourceUri) throws IOException {
        File outputFile = createMediaFile(context, CalibrationMediaType.PHOTO, resolveExtension(context, sourceUri, ".jpg"));
        copyStream(context, sourceUri, outputFile);
        return outputFile.getAbsolutePath();
    }

    public static String copyVideoToLocal(Context context, Uri sourceUri) throws IOException {
        File outputFile = createMediaFile(context, CalibrationMediaType.VIDEO, resolveExtension(context, sourceUri, ".mp4"));
        copyStream(context, sourceUri, outputFile);
        return outputFile.getAbsolutePath();
    }

    public static boolean exists(String filePath) {
        return filePath != null && !filePath.trim().isEmpty() && new File(filePath).exists();
    }

    public static void deleteQuietly(String filePath) {
        if (!exists(filePath)) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        new File(filePath).delete();
    }

    public static Uri buildUri(Context context, String filePath) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(filePath));
    }

    private static File createMediaFile(Context context, String mediaType, String extension) throws IOException {
        File baseDir = context.getExternalFilesDir(
            CalibrationMediaType.VIDEO.equals(mediaType) ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES
        );
        File targetDir = new File(
            baseDir == null ? context.getCacheDir() : baseDir,
            CalibrationMediaType.VIDEO.equals(mediaType) ? "calibration_media" : "calibration_media"
        );
        if (!targetDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            targetDir.mkdirs();
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US));
        String prefix = CalibrationMediaType.VIDEO.equals(mediaType) ? "calibration_video_" : "calibration_photo_";
        File file = new File(targetDir, prefix + timestamp + extension);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        return file;
    }

    private static void copyStream(Context context, Uri sourceUri, File outputFile) throws IOException {
        try (
            InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
            FileOutputStream outputStream = new FileOutputStream(outputFile)
        ) {
            if (inputStream == null) {
                throw new IOException("Nao foi possivel abrir o arquivo de video selecionado.");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
    }

    private static String resolveExtension(Context context, Uri uri, String fallback) {
        String displayName = "";
        Cursor cursor = context.getContentResolver().query(uri, new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        displayName = cursor.getString(index);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        int dotIndex = displayName == null ? -1 : displayName.lastIndexOf('.');
        if (dotIndex >= 0) {
            return displayName.substring(dotIndex);
        }
        return fallback;
    }
}
