package android.example.farmer.ViewModel;

import android.app.Application;
import android.example.farmer.DatabaseSingleton;
import android.example.farmer.source.PenEntry;
import android.example.farmer.source.ProductionEntry;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class ProductionzViewModel extends AndroidViewModel {
    // Production entry member variable for ProductionEntry object wrapped in a MutableLiveData
    private MutableLiveData<List<List<ProductionEntry>>> productionEntry;
    // Production keys member variable wrapped in a MutableLiveData
    private MutableLiveData<List<String>> productionKeyEntry;

    public ProductionzViewModel(@NonNull Application application) {
        super(application);
        // DatabaseSingleton Instance
        DatabaseSingleton singleton = DatabaseSingleton.getInstance();
        // get production data and store in variable
        productionEntry = singleton.setProductionEntry();
        productionKeyEntry = singleton.getProductionKeyList();
    }

    // Getter for both variables
    public MutableLiveData<List<List<ProductionEntry>>> getProductionEntry() {
        return productionEntry;
    }

    public MutableLiveData<List<String>> getProductionKeyEntry() {
        return productionKeyEntry;
    }
}
