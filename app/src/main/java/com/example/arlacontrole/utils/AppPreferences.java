package com.example.arlacontrole.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.arlacontrole.model.AuthSession;
import com.example.arlacontrole.model.OperationalFilter;

import java.net.URI;
import java.time.LocalDateTime;

public class AppPreferences {

    private static final String PREFS_NAME = "arla_controle_prefs";
    private static final String KEY_API_BASE_URL = "api_base_url";
    private static final String KEY_LAST_SYNC_AT = "last_sync_at";
    private static final String KEY_LAST_SYNC_MESSAGE = "last_sync_message";
    private static final String KEY_LAST_SAFETY_SPREADSHEET_IMPORT_AT = "last_safety_spreadsheet_import_at";
    private static final String KEY_LAST_SAFETY_SPREADSHEET_FILE_NAME = "last_safety_spreadsheet_file_name";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_SESSION_EXPIRES_AT = "session_expires_at";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_LINKED_DRIVER_NAME = "linked_driver_name";
    private static final String KEY_FILTER_START_DATE = "filter_start_date";
    private static final String KEY_FILTER_END_DATE = "filter_end_date";
    private static final String KEY_FILTER_VEHICLE = "filter_vehicle";
    private static final String KEY_FILTER_DRIVER = "filter_driver";
    private static final String KEY_FILTER_EVENT = "filter_event";
    private static final String DEFAULT_API_URL = "http://10.0.2.2:8000/";

    private final SharedPreferences sharedPreferences;

    public AppPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public String getApiBaseUrl() {
        return sanitizeBaseUrl(sharedPreferences.getString(KEY_API_BASE_URL, DEFAULT_API_URL));
    }

    public void saveApiBaseUrl(String rawUrl) {
        sharedPreferences.edit().putString(KEY_API_BASE_URL, sanitizeBaseUrl(rawUrl)).apply();
    }

    public long getLastSyncAt() {
        return sharedPreferences.getLong(KEY_LAST_SYNC_AT, 0L);
    }

    @NonNull
    public String getLastSyncMessage() {
        return sharedPreferences.getString(KEY_LAST_SYNC_MESSAGE, "");
    }

    public void updateLastSync(long timestamp, @NonNull String message) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_SYNC_AT, timestamp)
            .putString(KEY_LAST_SYNC_MESSAGE, message)
            .apply();
    }

    public long getLastSafetySpreadsheetImportAt() {
        return sharedPreferences.getLong(KEY_LAST_SAFETY_SPREADSHEET_IMPORT_AT, 0L);
    }

    @NonNull
    public String getLastSafetySpreadsheetFileName() {
        return sharedPreferences.getString(KEY_LAST_SAFETY_SPREADSHEET_FILE_NAME, "");
    }

    public void updateLastSafetySpreadsheetImport(long timestamp, @NonNull String fileName) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_SAFETY_SPREADSHEET_IMPORT_AT, timestamp)
            .putString(KEY_LAST_SAFETY_SPREADSHEET_FILE_NAME, fileName)
            .apply();
    }

    public void saveSession(@NonNull AuthSession session) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_SESSION_EXPIRES_AT, session.expiresAtIso)
            .putLong(KEY_USER_ID, session.userId)
            .putString(KEY_USER_NAME, session.fullName)
            .putString(KEY_USER_EMAIL, session.email)
            .putString(KEY_USER_ROLE, session.role)
            .putString(KEY_LINKED_DRIVER_NAME, session.linkedDriverName)
            .apply();
    }

    @NonNull
    public AuthSession getSession() {
        AuthSession session = new AuthSession();
        session.accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        session.expiresAtIso = sharedPreferences.getString(KEY_SESSION_EXPIRES_AT, "");
        session.userId = sharedPreferences.getLong(KEY_USER_ID, 0L);
        session.fullName = sharedPreferences.getString(KEY_USER_NAME, "");
        session.email = sharedPreferences.getString(KEY_USER_EMAIL, "");
        session.role = sharedPreferences.getString(KEY_USER_ROLE, "");
        session.linkedDriverName = sharedPreferences.getString(KEY_LINKED_DRIVER_NAME, "");
        return session;
    }

    public void clearSession() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_SESSION_EXPIRES_AT)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .remove(KEY_LINKED_DRIVER_NAME)
            .apply();
    }

    public void saveOperationalFilter(@NonNull OperationalFilter filter) {
        sharedPreferences.edit()
            .putString(KEY_FILTER_START_DATE, filter.startDateIso == null ? "" : filter.startDateIso)
            .putString(KEY_FILTER_END_DATE, filter.endDateIso == null ? "" : filter.endDateIso)
            .putString(KEY_FILTER_VEHICLE, filter.vehiclePlate == null ? "" : filter.vehiclePlate)
            .putString(KEY_FILTER_DRIVER, filter.driverName == null ? "" : filter.driverName)
            .putString(KEY_FILTER_EVENT, filter.eventType == null ? "" : filter.eventType)
            .apply();
    }

    @NonNull
    public OperationalFilter getOperationalFilter() {
        OperationalFilter filter = new OperationalFilter();
        filter.startDateIso = sharedPreferences.getString(KEY_FILTER_START_DATE, "");
        filter.endDateIso = sharedPreferences.getString(KEY_FILTER_END_DATE, "");
        filter.vehiclePlate = sharedPreferences.getString(KEY_FILTER_VEHICLE, "");
        filter.driverName = sharedPreferences.getString(KEY_FILTER_DRIVER, "");
        filter.eventType = sharedPreferences.getString(KEY_FILTER_EVENT, "");
        return filter;
    }

    public boolean hasValidSession() {
        String accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }
        String expiresAt = sharedPreferences.getString(KEY_SESSION_EXPIRES_AT, "");
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            return true;
        }
        try {
            return LocalDateTime.parse(expiresAt).isAfter(LocalDateTime.now());
        } catch (Exception ignored) {
            return true;
        }
    }

    @NonNull
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    @NonNull
    public static String sanitizeBaseUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return DEFAULT_API_URL;
        }
        String value = rawUrl.trim();
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "http://" + value;
        }
        if (!value.endsWith("/")) {
            value += "/";
        }
        return value;
    }

    public static boolean isValidBaseUrl(String rawUrl) {
        try {
            String value = sanitizeBaseUrl(rawUrl);
            URI uri = URI.create(value);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception exception) {
            return false;
        }
    }
}
