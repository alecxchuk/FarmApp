package android.example.farmer;

import android.example.farmer.Adapters.BreedSourceAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PenBreed extends Fragment implements BreedSourceAdapter.ListItemClickListener {

    private static final String TAG = PenBreed.class.getSimpleName();
    private PenBreedViewModel mViewModel;

    // Variable to Store reference to views
    EditText editBreed;
    ImageView saveBreed;
    RecyclerView recyclerView;

    // Variable to store Adapter instance
    BreedSourceAdapter mAdapter;

    // Variable for breed ArrayList
    private List<String> breedList;
    private LinearLayout linearLayout;


    // Instance variable fields for firebase database and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDataDatabaseReference;

    // Instance variable for child event listener.
    private ChildEventListener mChildEventListener;
    private String breedKey;
    private List<String> keyList;

    public static PenBreed newInstance() {
        return new PenBreed();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pen_breed_fragment, container, false);
        initViews(rootView);
        mAdapter = new BreedSourceAdapter(getContext(), this);
        breedList = new ArrayList<>();
        keyList = new ArrayList<>();
        //mAdapter.setBreed(breedList);

        // Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // reference specific part of the database
        mDataDatabaseReference = mFirebaseDatabase.getReference().child("data").child("breed");
        mDataDatabaseReference.keepSynced(true);
        attachDatabaseReadListener();
        // Assign Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);


        AddBreed();


        DividerItemDecoration decoration = new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(decoration);


        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                // delete breed using keys
                mDataDatabaseReference.child(keyList.get(viewHolder.getAdapterPosition())).removeValue();
            }
        }).attachToRecyclerView(recyclerView);


        return rootView;
    }

    private void initViews(View rootView) {
        editBreed = rootView.findViewById(R.id.enter_pen_breed);
        saveBreed = rootView.findViewById(R.id.pen_breed_save);
        recyclerView = rootView.findViewById(R.id.pen_breed_recycler);
        linearLayout = rootView.findViewById(R.id.pen_breed_linearLayout);
    }

    private void AddBreed() {
        saveBreed.setOnClickListener(v -> {
            if (editBreed.getText().toString().matches("")) {
                return;
            }
            // get text from edit text
            String breed = editBreed.getText().toString().trim();

            breedKey = mDataDatabaseReference.push().getKey();


            mDataDatabaseReference.child(breedKey).setValue(breed);
            editBreed.setText("");


            // populate arrayList
            /*breedList.add(breed);
            Toast.makeText(getContext(), "breedList size is" + breedList.size(), Toast.LENGTH_LONG).show();

            // get the adapter and notify it that data has changed
            BreedSourceAdapter adapter = (BreedSourceAdapter) recyclerView.getAdapter();
            adapter.notifyDataSetChanged();*/

            linearLayout.setVisibility(View.GONE);

        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //AddBreed();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(PenBreedViewModel.class);
        // TODO: Use the ViewModel

    }

    private void attachDatabaseReadListener() {
        // Initialize the ChildEventListener
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Retrieve String data from DataSnapshot
                String breed = Objects.requireNonNull(snapshot.getValue()).toString();

                // add data to breed ArrayList.
                breedList.add(breed);


                // list that contains the keys for each breed.
                keyList.add(snapshot.getKey());

                // Add arrayList to Adapter.
                mAdapter.setBreed(breedList);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = keyList.indexOf(snapshot.getKey());
                breedList.remove(index);
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
        mDataDatabaseReference.addChildEventListener(mChildEventListener);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.breed_source, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_breed_source:
                linearLayout.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.edit_breed_source);
        MenuItem item2 = menu.findItem(R.id.save_pen);
        MenuItem item3 = menu.findItem(R.id.delete_production);
        if (!item.isVisible()) {
            item.setVisible(true);
        }
        item2.setVisible(false);
        item3.setVisible(false);

    }

    /**
     * @param clickedItemIndex
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        // get the breed selected by the user from the recycler view
        String selectedBreed = ((TextView) recyclerView.findViewHolderForAdapterPosition(clickedItemIndex)
                .itemView.findViewById(R.id.pen_breed_list_item_breed)).getText().toString();
        // pass breed into view model
        mViewModel.setBreed(selectedBreed);
        // close the fragment and return to previous activity
        getFragmentManager().beginTransaction().remove(PenBreed.this).commit();
        getFragmentManager().popBackStack();
        // get the reference to layout container in the parent activity
        View view = getActivity().findViewById(R.id.addNew_container);
        // make container visible.
        view.setVisibility(View.VISIBLE);

    }
}