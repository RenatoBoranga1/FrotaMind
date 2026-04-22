package com.example.arlacontrole.ui.historico;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class RefuelDetailViewModel extends BaseArlaViewModel {

    private final MutableLiveData<Long> selectedId = new MutableLiveData<>();
    private final LiveData<RefuelEntity> refuel = Transformations.switchMap(selectedId, repository::observeRefuel);

    public RefuelDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void setRefuelId(long localId) {
        selectedId.setValue(localId);
    }

    public LiveData<RefuelEntity> getRefuel() {
        return refuel;
    }
}
