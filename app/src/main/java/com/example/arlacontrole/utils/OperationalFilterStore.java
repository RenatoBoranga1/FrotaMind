package com.example.arlacontrole.utils;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.arlacontrole.model.OperationalFilter;

public class OperationalFilterStore {

    private static volatile OperationalFilterStore INSTANCE;

    private final AppPreferences preferences;
    private final MutableLiveData<OperationalFilter> filterLiveData = new MutableLiveData<>();

    private OperationalFilterStore(Context context) {
        preferences = new AppPreferences(context.getApplicationContext());
        filterLiveData.setValue(preferences.getOperationalFilter());
    }

    public static OperationalFilterStore getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (OperationalFilterStore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OperationalFilterStore(context);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<OperationalFilter> getFilter() {
        return filterLiveData;
    }

    public OperationalFilter getCurrent() {
        OperationalFilter current = filterLiveData.getValue();
        return current == null ? new OperationalFilter() : current.copy();
    }

    public void update(OperationalFilter filter) {
        OperationalFilter safe = filter == null ? new OperationalFilter() : filter.copy();
        preferences.saveOperationalFilter(safe);
        filterLiveData.postValue(safe);
    }

    public void clear() {
        update(new OperationalFilter());
    }
}
