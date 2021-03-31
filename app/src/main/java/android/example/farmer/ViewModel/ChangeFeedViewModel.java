package android.example.farmer.ViewModel;

import android.app.Application;
import android.example.farmer.DatabaseSingleton;
import android.example.farmer.source.FeedEntry;
import android.example.farmer.source.PenEntry;
import android.example.farmer.source.ProductionEntry;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class ChangeFeedViewModel extends AndroidViewModel {
    // Feed entry member variable for FeedEntry object wrapped in a MutableLiveData
    private MutableLiveData<List<List<FeedEntry>>> feedEntry;
    // Feed keys member variable wrapped in a MutableLiveData
    private MutableLiveData<List<String>> feedKeyEntry;
    public ChangeFeedViewModel(@NonNull Application application) {
        super(application);
        // DatabaseSingleton Instance
        DatabaseSingleton singleton = DatabaseSingleton.getInstance();
        // get production data and store in variable
        feedEntry = singleton.setFeedEntry();
        feedKeyEntry = singleton.getFeedKeyList();
    }
    // Getter for both variables
    public MutableLiveData<List<List<FeedEntry>>> getFeedEntry() {
        return feedEntry;
    }

    public MutableLiveData<List<String>> getFeedKeyEntry() {
        return feedKeyEntry;
    }
}
