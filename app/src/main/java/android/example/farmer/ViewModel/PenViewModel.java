package android.example.farmer.ViewModel;

import android.app.Application;
import android.example.farmer.DatabaseSingleton;
import android.example.farmer.source.PenEntry;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class PenViewModel extends AndroidViewModel {
    private MutableLiveData<List<PenEntry>> penEntry;
    private MutableLiveData<List<String>> keyEntry;

    public PenViewModel(@NonNull Application application) {
        super(application);
       DatabaseSingleton singleton = DatabaseSingleton.getInstance();
        penEntry = singleton.setPenEntry();
        keyEntry = singleton.getKeyList();
    }



    public MutableLiveData<List<PenEntry>> getPenEntry() {
        return penEntry;
    }

    public MutableLiveData<List<String>> getKeyEntry() {
        return keyEntry;
    }
}
