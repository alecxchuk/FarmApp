package android.example.farmer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.Adapters.ProductAdapter;
import android.example.farmer.AddNewProduction;
import android.example.farmer.PenBlock;
import android.example.farmer.R;
import android.example.farmer.source.ProductionEntry;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProductionFragment extends Fragment implements ProductAdapter.ItemClickListener {
    // Contains default string value for shared preferences.
    private static final String DATE_DEFAULT = "dateDefault";

    // Instance variable fields for firebase database and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProductionDatabaseReference;
    private ChildEventListener mChildEventListener;

    // private variable to store ProductionEntry list
    private List<ProductionEntry> mProductionEntries;

    // list variable for storing keys
    private List<String> keyList;

    // String variable to store penKey
    private String penKey;

    //Variable to store recyclerView
    private RecyclerView productionRecycler;

    // Instance variable for Product Adapter.
    private ProductAdapter mAdapter;
    // variables to store textViews and button
    private TextView dateTextView, firstProdTextView, secondProdTextView, thirdProdTextView, totalProdTextView;
    private Button addProductionButton;

    /**
     * Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
     */
    private boolean mProductionHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductionHasChanged = true;
            return false;
        }
    };


    public ProductionFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_production, container, false);
        // contains references for all the views in the xml layout.
        initViews(rootView);
        // Open AddNewProduction.class
        open();

        //Get Argument that passed from activity in "data" key value
        penKey = getArguments().getString(PenBlock.PEN_KEY);

        // Initialize ArrayLists
        mProductionEntries = new ArrayList<>();
        keyList = new ArrayList<>();

        // Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // reference specific part of the database
        mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product")
                .child(penKey).child("production");
        // Attach ChildEventListener to database
        attachDatabaseReadListener();

        // Assign Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        productionRecycler.setLayoutManager(layoutManager);
        productionRecycler.setHasFixedSize(true);

        // Initialize recycler view adapter
        mAdapter = new ProductAdapter(getActivity(), this);
        productionRecycler.setAdapter(mAdapter);
        return rootView;
    }

    /**
     * Helper method to attach Listener to Database
     */
    private void attachDatabaseReadListener() {
        // Initialize ChildEventListener
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Retrieve ProductionEntry data from DataSnapshot
                ProductionEntry productionEntry = snapshot.getValue(ProductionEntry.class);
                mProductionEntries.add(productionEntry);
                // list that contains the keys for each production.
                keyList.add(snapshot.getKey());
                // Add arrayList to PenAdapter.
                mAdapter.setProduction(mProductionEntries);
                Gson gson = new Gson();
                // Convert ProductionEntry object to string.
                String productionEntryAsAString = gson.toJson(productionEntry);
                // save in shared preference, with key
                SavePreferences(productionEntryAsAString);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = keyList.indexOf(snapshot.getKey());
                mProductionEntries.remove(index);
                keyList.remove(index);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        // Attach ChildEventListener to database.
        mProductionDatabaseReference.addChildEventListener(mChildEventListener);
    }

    /**
     * Helper method for saving data to shared preferences
     *
     * @param value data stored in the shared preference
     */
    private void SavePreferences(String value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("key", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("productionkey", value);
        editor.apply();
    }

    /**
     * Helper method to retrieve data from shared preference with a key.
     *
     * @param key for referencing particular data in shared preferences.
     * @return returns string stored
     */
    private String showPreferences(String key) {
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences("key", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, DATE_DEFAULT);
    }

    /**
     * Helper method containing references to views in xml layout.
     */
    private void initViews(View view) {
        // Find all relevant views that we will need to read user input from
        dateTextView = view.findViewById(R.id.production_table_date);
        firstProdTextView = view.findViewById(R.id.production_table_first_prod_value);
        secondProdTextView = view.findViewById(R.id.production_table_second_prod_value);
        thirdProdTextView = view.findViewById(R.id.production_table_third_prod_value);
        totalProdTextView = view.findViewById(R.id.production_table_total_prod_value);
        addProductionButton = view.findViewById(R.id.add_production_button);
        // reference for recycler view
        productionRecycler = view.findViewById(R.id.production_recycler);
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        dateTextView.setOnTouchListener(mTouchListener);
        firstProdTextView.setOnTouchListener(mTouchListener);
        secondProdTextView.setOnTouchListener(mTouchListener);
        thirdProdTextView.setOnTouchListener(mTouchListener);
        totalProdTextView.setOnTouchListener(mTouchListener);
    }

    /**
     * Helper method to open AddNewProduction.class
     */
    private void open() {
        addProductionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddNewProduction.class);
            intent.putExtra("penKey", penKey);
            startActivity(intent);
        });
    }

    /**
     * Helper method for deleting whole production
     */
    private void delete() {
        mProductionDatabaseReference.removeValue();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_production_table, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_production_id:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Click listener helper method
     *
     * @param date refers to the production date
     */
    @Override
    public void onItemClickListener(String date) {
        Intent intent = new Intent(getActivity(), AddNewProduction.class);
        String PROD_KEY = showPreferences(date);
        intent.putExtra("prodKey", PROD_KEY);
        intent.putExtra("penKey2", penKey);

        startActivity(intent);
    }
}