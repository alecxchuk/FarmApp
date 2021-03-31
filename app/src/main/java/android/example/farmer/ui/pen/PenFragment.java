package android.example.farmer.ui.pen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.Adapters.PenAdapt;
import android.example.farmer.AddNewPen;
import android.example.farmer.PenBlock;
import android.example.farmer.Pens;
import android.example.farmer.R;
import android.example.farmer.source.PenEntry;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

public class PenFragment extends Fragment implements PenAdapt.ItemClickListener {

    // Contains default string value for shared preferences.
    private static final String NAME_DEFAULT = "default";

    // String field variable for push key for pen
    private String penKey;

    // Instance variable for recycler view
    private RecyclerView penRecycler;

    // Instance variable for Pen Adapter.
    public static PenAdapt mAdapter;

    // Instance variable fields for firebase database and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPensDatabaseReference;

    // Instance variable for child event listener.
    private ChildEventListener mChildEventListener;

    // private variable to store PenEntry list
    private List<PenEntry> mPenEntries;

    // list variable for storing keys
    private List<String> keyList;

    private PenViewModel mViewModel;

    public static PenFragment newInstance() {
        return new PenFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // PenEntry ArrayList instance
        mPenEntries = new ArrayList<>();
        keyList = new ArrayList<>();

        // firebase database instance
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // reference specific part of the database
        mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
        // add child listener
        attachDatabaseReadListener();

        // Assign Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        penRecycler.setLayoutManager(layoutManager);
        penRecycler.setHasFixedSize(true);


        // Initialize recycler view adapter
        mAdapter = new PenAdapt(getActivity(), (PenAdapt.ItemClickListener) this);
        penRecycler.setAdapter(mAdapter);
        return inflater.inflate(R.layout.pen_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(PenViewModel.class);
        // TODO: Use the ViewModel
    }
    /**
     * Helper method to attach ChildEventListener to database.
     */
    private void attachDatabaseReadListener() {
        // Initialize the ChildEventListener
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Retrieve PenEntry data from DataSnapshot
                PenEntry penEntry = snapshot.getValue(PenEntry.class);
                // add data to mPenEntries Arraylist.
                mPenEntries.add(penEntry);
                // list that contains the keys for each pen.
                keyList.add(snapshot.getKey());
                // Add arrayList to PenAdapter.
                mAdapter.setPen(mPenEntries);
                // gson instance
                Gson gson = new Gson();
                // Convert PenEntry object to string.
                String penEntryAsAString = gson.toJson(penEntry);
                // save in shared preference, with key
                SavePreferences("key", penEntryAsAString);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = keyList.indexOf(snapshot.getKey());
                mPenEntries.remove(index);
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
        mPensDatabaseReference.addChildEventListener(mChildEventListener);
    }

    /**
     * Helper method for saving data to shared preferences
     *
     * @param key   String for references value stored
     * @param value data stored in the shared preference
     */
    private void SavePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("key",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
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
        return sharedPreferences.getString(key, NAME_DEFAULT);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu options from the res/menu/main.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "add new pen" menu option
            // intent to open AddNew Activity
            case R.id.add_new_pen:
                Intent intent = new Intent(getActivity(), AddNewPen.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }




    /**
     * Click listener helper method
     *
     * @param itemId position of the object in the adapter
     * @param name   refers to the penName
     */
    @Override
    public void onItemClickListener(int itemId, String name) {
        Intent intent = new Intent(getActivity(), PenBlock.class);
        String PEN_KEY = showPreferences(name);
        intent.putExtra("key", PEN_KEY);
        startActivity(intent);
    }
}