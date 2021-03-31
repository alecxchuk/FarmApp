package android.example.farmer.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.example.farmer.AddNewProduction;
import android.example.farmer.ChangeFeed;
import android.example.farmer.CompleteListener;
import android.example.farmer.DatabaseSingleton;
import android.example.farmer.FirebaseMultiQuery;
import android.example.farmer.Home;
import android.example.farmer.PenBlock;
import android.example.farmer.Pens;
import android.example.farmer.R;
import android.example.farmer.source.PenEntry;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Map;

public class HomeFragment extends Fragment {

    // variable field to store reference to constraint layout
    private ConstraintLayout penLayout, feedLayout, productionLayout;


    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Reference views
        initViews(root);

        // open pen activity
        openViews(penLayout, Pens.class);
        // add new production
        openViews(productionLayout, AddNewProduction.class);
        // add feed
        openViews(feedLayout, ChangeFeed.class);

        return root;
    }

    /**
     * This method sets a listener on the constraint layout and opens the desired activity
     *
     * @param layout   constraint layout container
     * @param activity destination activity to be launched
     */
    private void openViews(ConstraintLayout layout, Class activity) {
        layout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), activity);
            startActivity(intent);
        });
    }

    /**
     * Contains the Reference to views in the xml layout
     *
     * @param v root view
     */
    private void initViews(View v) {
        // Reference for views in the xml layout.
        penLayout = v.findViewById(R.id.home_pen_management_layout);
        feedLayout = v.findViewById(R.id.home_feeding_layout);
        productionLayout = v.findViewById(R.id.home_egg_production_layout);
    }

}