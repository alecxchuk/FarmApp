package android.example.farmer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.Adapters.FeedAdapter;
import android.example.farmer.ChangeFeed;
import android.example.farmer.PenBlock;
import android.example.farmer.R;
import android.example.farmer.source.FeedEntry;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.util.Objects;

public class FeedFragment extends Fragment implements FeedAdapter.ItemClickListener {
    // variables for strings.
    private String feedName, startDate, endDate;
    // variables for textView,layout and recyclerView reference.
    private TextView feedNameView, feedTypeView, startDateView, endDateView;
    private ConstraintLayout layout;
    private RecyclerView feedRecycler;

    // Variable for adapter instance.
    private FeedAdapter mAdapter;

    // Contains default string value for shared preferences.
    private static final String DATE_DEFAULT = "dateDefault";

    // Instance variable fields for firebase database and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFeedDatabaseReference;
    private ChildEventListener mChildEventListener;

    // private variable to store ProductionEntry list
    private List<FeedEntry> mFeedEntries;

    // list variable for storing keys
    private List<String> keyList;

    // String variable to store penKey
    private String penKey;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_fragment, container, false);
        // contains references for all the views in the xml layout.
        initViews(rootView);

        //Get Argument that passed from activity in "data" key value
        penKey = getArguments().getString(PenBlock.PEN_KEY);

        // Initialize ArrayLists
        mFeedEntries = new ArrayList<>();
        keyList = new ArrayList<>();

        // Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // reference specific part of the database
        mFeedDatabaseReference = mFirebaseDatabase.getReference().child("feeding")
                .child(penKey).child("feed");
        // Attach ChildEventListener to database
        attachDatabaseReadListener();

        // adapter instance
        mAdapter = new FeedAdapter(getActivity(), this);
        // attach adapter to feedRecycler
        feedRecycler.setAdapter(mAdapter);

        // Assign Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        feedRecycler.setLayoutManager(layoutManager);
        feedRecycler.setHasFixedSize(true);

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
                FeedEntry feedEntry = snapshot.getValue(FeedEntry.class);
                mFeedEntries.add(feedEntry);
                // list that contains the keys for each production.
                keyList.add(snapshot.getKey());
                // Add arrayList to PenAdapter.
                mAdapter.setFeed(mFeedEntries);
                Gson gson = new Gson();
                // Convert ProductionEntry object to string.
                String feedEntryAsAString = gson.toJson(feedEntry);
                // save in shared preference, with key
                SavePreferences("feedObj", feedEntryAsAString);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = keyList.indexOf(snapshot.getKey());
                mFeedEntries.remove(index);
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
        mFeedDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private void initViews(View rootView) {
        layout = rootView.findViewById(R.id.frameLayout2);
        feedNameView = rootView.findViewById(R.id.feed_name);
        reference(feedTypeView, rootView, R.id.feed_type);
        startDateView = rootView.findViewById(R.id.feed_start_date);
        //reference(startDateView,rootView,R.id.feed_start_date);
        //reference(endDateView,rootView,R.id.feed_end_date);
        endDateView = rootView.findViewById(R.id.feed_end_date);
        feedRecycler = rootView.findViewById(R.id.feed_recycler);
    }

    /**
     * Helper method for saving data to shared preferences
     *
     * @param value data stored in the shared preference
     */
    private void SavePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("key", Context.MODE_PRIVATE);
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
        return sharedPreferences.getString(key, DATE_DEFAULT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the menu options from the res/menu/change_feed.xml file.
        // This adds menu items to the app bar.
        requireActivity().getMenuInflater().inflate(R.menu.change_feed, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "add new pen" menu option
            // intent to open AddNew Activity
            case R.id.change_feed:
                Intent intent = new Intent(getActivity(), ChangeFeed.class);
                intent.putExtra("penKey", penKey);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    private void reference(View view, View rootView, int sss) {
        view = rootView.findViewById(sss);
    }

    private void displayInfo(TextView textView, String string) {
        textView.setText(string);
    }


    @Override
    public void onItemClickListener(String startDate) {
        Intent intent = new Intent(getActivity(), ChangeFeed.class);
        String FEED_KEY = showPreferences(startDate);
        intent.putExtra("feedKey", FEED_KEY);
        intent.putExtra("penKey2", penKey);

        startActivity(intent);
    }
}