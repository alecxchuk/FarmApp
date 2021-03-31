package android.example.farmer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PenBreedViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private final MutableLiveData<String> breed = new MutableLiveData<String>();

    public void setBreed (String breed){
        this.breed.setValue(breed);
    }
    public LiveData<String> getBreed() {
        return breed;
    }

}