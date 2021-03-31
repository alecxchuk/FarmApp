package android.example.farmer.ViewModel;

import android.example.farmer.source.FeedEntry;
import android.example.farmer.source.PenEntry;
import android.example.farmer.source.ProductionEntry;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainHomeViewModel extends ViewModel {
    private MutableLiveData<PenEntry> penEntry = new MutableLiveData<>();
    private MutableLiveData<ProductionEntry> productionEntry = new MutableLiveData<>();
    private MutableLiveData<FeedEntry> feedEntry = new MutableLiveData<>();


    public MainHomeViewModel() {

    }

    public MutableLiveData<PenEntry> getPenEntry() {
        return penEntry;
    }

    public void setPenEntry(PenEntry penEntry) {
        this.penEntry.setValue(penEntry);
    }

    public MutableLiveData<ProductionEntry> getProductionEntry() {
        return productionEntry;
    }

    public void setProductionEntry(ProductionEntry productionEntry) {
        this.productionEntry.setValue(productionEntry);
    }

    public MutableLiveData<FeedEntry> getFeedEntry() {
        return feedEntry;
    }

    public void setFeedEntry(MutableLiveData<FeedEntry> feedEntry) {
        this.feedEntry = feedEntry;
    }
}
