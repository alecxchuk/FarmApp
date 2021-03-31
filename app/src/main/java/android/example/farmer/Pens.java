package android.example.farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.Adapters.PenAdapt;
import android.example.farmer.Adapters.StockAdapter;
import android.example.farmer.ViewModel.PenViewModel;
import android.example.farmer.source.PenEntry;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Pens extends AppCompatActivity implements PenAdapt.ItemClickListener {

    private static final String TAG = android.example.farmer.Pens.class.getSimpleName();

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

    // know when complete
    FirebaseMultiQuery firebaseMultiQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pens);

        // PenEntry ArrayList instance
        mPenEntries = new ArrayList<>();
        keyList = new ArrayList<>();

        // firebase database instance
        mFirebaseDatabase = FirebaseDatabase.getInstance();


        // reference specific part of the database
        mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
        mPensDatabaseReference.keepSynced(true);
        // add child listener
        //attachDatabaseReadListener();


        // reference for recycler view
        penRecycler = findViewById(R.id.pen_recycler_view);

        // Assign Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        penRecycler.setLayoutManager(layoutManager);
        penRecycler.setHasFixedSize(true);


        // Initialize recycler view adapter
        mAdapter = new PenAdapt(this, (PenAdapt.ItemClickListener) this);
        penRecycler.setAdapter(mAdapter);

        //oneQuery();
        sssf();





    }

    private DatabaseSingleton databaseSingleton;


    private void sssf() {
        databaseSingleton = DatabaseSingleton.getInstance();

    PenViewModel penViewModel=  new ViewModelProvider(Pens.this).get(PenViewModel.class);
    penViewModel.getPenEntry().observe(this, penEntries -> {
        mPenEntries=penEntries;
        if (mPenEntries!=null) {
            mAdapter.setPen(mPenEntries);
        }
        //Log.v(TAG, " MutablePenEntry is this " + mPenEntries.size());
    });
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
        SharedPreferences sharedPreferences = getSharedPreferences("key", MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = this
                .getSharedPreferences("key", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, NAME_DEFAULT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "add new pen" menu option
            // intent to open AddNew Activity
            case R.id.add_new_pen:
                Intent intent = new Intent(Pens.this, AddNewPen.class);
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
        Intent intent = new Intent(this, PenBlock.class);
        String PEN_KEY = showPreferences(name);
        intent.putExtra("key", PEN_KEY);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        oneQuery();


        Log.v(TAG, "onStart called adapter size is " + mAdapter.getItemCount());

    }

    private void oneQuery() {
        //firebaseMultiQuery = new FirebaseMultiQuery(mPensDatabaseReference,Home.mProductionDatabaseReference);
        firebaseMultiQuery=FirebaseMultiQuery.getInstance();
        firebaseMultiQuery.setRefs(mPensDatabaseReference,Home.mProductionDatabaseReference);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(Pens.this, databaseSingleton  );

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onStop() {
        super.onStop();
        firebaseMultiQuery.stop();
        mPenEntries.clear();
        Log.v(TAG, "onStop called adapter size is " + mAdapter.getItemCount());


    }



/*private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            if (task.isSuccessful()) {
                final Map<DatabaseReference, DataSnapshot> result = task.getResult();

                // Look up DataSnapshot objects using the same DatabaseReferences you passed into FirebaseMultiQuery
                DataSnapshot snapshot = result.get(mPensDatabaseReference);
                /*PenEntry entry =snapshot.getValue(PenEntry.class);
                List<PenEntry>aaa=new ArrayList<>();
                aaa.add(entry);
                mAdapter.setPen(aaa);*/
                /*for (DataSnapshot p : snapshot.getChildren()) {
                    Log.v(TAG, "map list is " + p);
                    PenEntry penEntry = p.getValue(PenEntry.class);
                    mPenEntries.add(penEntry);
                    mAdapter.setPen(mPenEntries);
                    Log.v(TAG, "pens list is " + penEntry.getAge());
                }

            } else {
                //exception = task.getException();
                // log the error or whatever you need to do
            }
            // Do stuff with views
            //updateUi();

        }
    }*/

}