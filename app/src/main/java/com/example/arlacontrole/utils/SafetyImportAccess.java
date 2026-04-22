package com.example.arlacontrole.utils;

import com.example.arlacontrole.model.AuthSession;

import java.util.Locale;

public final class SafetyImportAccess {

    public static final String AUTHORIZED_IMPORT_EMAIL = "seguranca.importador@frotamind.local";

    private SafetyImportAccess() {
    }

    public static boolean canImportOccurrences(AuthSession session) {
        if (session == null || session.email == null) {
            return false;
        }
        return AUTHORIZED_IMPORT_EMAIL.equals(session.email.trim().toLowerCase(Locale.ROOT));
    }
}
