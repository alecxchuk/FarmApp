package android.example.farmer.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.example.farmer.R;
import android.example.farmer.source.PenEntry;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.util.Objects;

public class PenData extends Fragment {

    // Contains default string value for shared preferences.
    private static final String NAME_DEFAULT = "default";

    private TextView mPenNameTextView, mBreedTextView, mStockDateTextView, mAgeTextView, mNumberTextView;

    /**
     * variable for Adapter
     */


    private MenuItem item;

    private int mPenId;

    public static PenData newInstance() {
        return new PenData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pen_data_fragment, container, false);


        // reference for respective views
        mPenNameTextView = rootView.findViewById(R.id.pen_data_penName);
        mBreedTextView = rootView.findViewById(R.id.pen_data_penBreed);
        mStockDateTextView = rootView.findViewById(R.id.pen_data_stockDate);
        mAgeTextView = rootView.findViewById(R.id.pen_data_age);
        mNumberTextView = rootView.findViewById(R.id.pen_data_number);

        Gson gson = new Gson();
        //studentDataObjectAsAString = getIntent().getStringExtra("key");
        String json = showPreferences("key");
        PenEntry penEntry = gson.fromJson(json, PenEntry.class);
        populateUI(penEntry);


        return rootView;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Helper method to retrieve data from shared preference with a key.
     *
     * @param key for referencing particular data in shared preferences.
     * @return returns string stored
     */
    private String showPreferences(String key) {
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("key", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, NAME_DEFAULT);
    }

    private void populateUI(PenEntry penEntry) {
        if (penEntry == null) {
            return;
        }

        mPenNameTextView.setText(penEntry.getPenName());
        mBreedTextView.setText(penEntry.getPenBreed());
        mAgeTextView.setText(String.valueOf(penEntry.getAge()));
        mNumberTextView.setText(String.valueOf(penEntry.getNumber()));
        mStockDateTextView.setText(penEntry.getStockDate().toString());


    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        item = menu.findItem(R.id.pen_block_menu_delete);
        MenuItem item2 = menu.findItem(R.id.pen_block_menu_edit);
        item2.setVisible(true);
        item.setVisible(false);
    }


}
