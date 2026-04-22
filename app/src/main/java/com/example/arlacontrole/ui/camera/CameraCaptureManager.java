package com.example.arlacontrole.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.concurrent.Executor;

public class CameraCaptureManager {

    public interface ErrorCallback {
        void onError(String message);
    }

    public interface CaptureCallback {
        void onSuccess(String filePath);

        void onError(String message);
    }

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    public void bind(@NonNull AppCompatActivity activity, @NonNull PreviewView previewView, @NonNull ErrorCallback callback) {
        com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(activity);
        providerFuture.addListener(() -> {
            try {
                cameraProvider = providerFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

                cameraProvider.unbindAll();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception exception) {
                callback.onError(exception.getMessage() == null ? "Nao foi possivel abrir a camera." : exception.getMessage());
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    @SuppressLint("MissingPermission")
    public void capture(@NonNull Context context, @NonNull File outputFile, @NonNull CaptureCallback callback) {
        if (imageCapture == null) {
            callback.onError("Camera indisponivel no momento.");
            return;
        }
        Executor executor = ContextCompat.getMainExecutor(context);
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                callback.onSuccess(outputFile.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                callback.onError(exception.getMessage() == null ? "Falha ao capturar a imagem." : exception.getMessage());
            }
        });
    }

    public void unbind() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
