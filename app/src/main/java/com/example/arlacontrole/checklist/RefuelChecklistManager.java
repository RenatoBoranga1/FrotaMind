package com.example.arlacontrole.checklist;

import android.content.Context;

import com.example.arlacontrole.R;
import com.example.arlacontrole.model.FuelType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class RefuelChecklistManager {

    private static final String KEY_BOMBA_ZERADA = "bomba_zerada";
    private static final String KEY_SEM_VAZAMENTO = "sem_vazamento";
    private static final String KEY_TANQUE_CORRETO = "tanque_correto";
    private static final String KEY_TIPO_CONFERIDO = "tipo_conferido";
    private static final String KEY_PLACA_CONFERIDA = "placa_conferida";
    private static final String KEY_COMPROVANTE_OBTIDO = "comprovante_obtido";
    private static final String KEY_EVIDENCIA = "evidencia";

    private RefuelChecklistManager() {
    }

    public static List<ChecklistItemState> build(Context context, String fuelType, boolean hasEvidence) {
        List<ChecklistItemState> items = new ArrayList<>();
        if (FuelType.DIESEL.equals(fuelType)) {
            items.add(new ChecklistItemState(KEY_TIPO_CONFERIDO, context.getString(R.string.checklist_diesel_type_confirmed), false, false));
            items.add(new ChecklistItemState(KEY_PLACA_CONFERIDA, context.getString(R.string.checklist_diesel_plate_confirmed), false, false));
            items.add(new ChecklistItemState(KEY_COMPROVANTE_OBTIDO, context.getString(R.string.checklist_diesel_receipt_obtained), false, false));
            items.add(new ChecklistItemState(KEY_EVIDENCIA, context.getString(R.string.checklist_diesel_photo_required), hasEvidence, true));
            return items;
        }
        items.add(new ChecklistItemState(KEY_BOMBA_ZERADA, context.getString(R.string.checklist_arla_pump_zeroed), false, false));
        items.add(new ChecklistItemState(KEY_SEM_VAZAMENTO, context.getString(R.string.checklist_arla_no_leak), false, false));
        items.add(new ChecklistItemState(KEY_TANQUE_CORRETO, context.getString(R.string.checklist_arla_correct_tank), false, false));
        items.add(new ChecklistItemState(KEY_EVIDENCIA, context.getString(R.string.checklist_arla_photo_required), hasEvidence, true));
        return items;
    }

    public static void applyEvidenceState(List<ChecklistItemState> items, boolean hasEvidence) {
        if (items == null) {
            return;
        }
        for (ChecklistItemState item : items) {
            if (item.autoManaged) {
                item.checked = hasEvidence;
            }
        }
    }

    public static boolean isComplete(List<ChecklistItemState> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (ChecklistItemState item : items) {
            if (!item.checked) {
                return false;
            }
        }
        return true;
    }

    public static String serialize(List<ChecklistItemState> items) {
        JSONArray array = new JSONArray();
        if (items != null) {
            for (ChecklistItemState item : items) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("key", item.key);
                    object.put("label", item.label);
                    object.put("checked", item.checked);
                    object.put("auto_managed", item.autoManaged);
                    array.put(object);
                } catch (Exception ignored) {
                }
            }
        }
        return array.toString();
    }

    public static List<ChecklistItemState> deserialize(String payload) {
        List<ChecklistItemState> items = new ArrayList<>();
        if (payload == null || payload.trim().isEmpty()) {
            return items;
        }
        try {
            JSONArray array = new JSONArray(payload);
            for (int index = 0; index < array.length(); index++) {
                JSONObject object = array.getJSONObject(index);
                items.add(new ChecklistItemState(
                    object.optString("key"),
                    object.optString("label"),
                    object.optBoolean("checked"),
                    object.optBoolean("auto_managed")
                ));
            }
        } catch (Exception ignored) {
        }
        return items;
    }
}
