package android.example.farmer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.Adapters.FeedAdapter;
import android.example.farmer.ViewModel.ChangeFeedViewModel;
import android.example.farmer.ViewModel.PenViewModel;
import android.example.farmer.source.FeedEntry;
import android.example.farmer.source.PenEntry;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChangeFeed extends AppCompatActivity implements FeedAdapter.ItemClickListener, View.OnClickListener {

    private static final String TAG = ChangeFeed.class.getSimpleName();
    // variable for FeedAdapter instance
    private FeedAdapter mAdapter;

    private List<FeedEntry> feed;

    // Variable for DatabaseSingleton instance
    private DatabaseSingleton databaseSingleton;

    // private variable to store PenEntry list
    private List<PenEntry> mPenEntries;

    // list variable for storing keys
    private List<String> keyList;
    private String penName;
    // int variable to know position of the pen.
    private int m = 0;


    /**
     * type of the feed. The possible values are:
     * 0 for unknown feed, 1 for Broilers Starter, 2 for chicks marsh, 3 for growers, 4 for layers marsh.
     */
    private int mFeedType = 0;
    /*Possible values for the feed type*/
    public static final int FEED_UNKNOWN = 0;
    public static final int FEED_BROILERS_STARTER = 1;
    public static final int FEED_CHICKS_MARSH = 2;
    public static final int FEED_GROWERS_MARSH = 3;
    public static final int FEED_LAYERS_MARSH = 4;

    // String key for referencing data in shared preferences.
    private static final String GSON_DEFAULT = "default";

    private EditText feedName, feedTypeEditText, quantityEditText;
    private Spinner mFeedSpinner;
    private CheckBox checkBox;
    private int quantity;
    private Calendar startCalendar, endCalendar;
    private String feedDataObjectAsAString;


    // Instance variable fields for firebase databse and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFeedingDatabaseReference;
    private String productionDataObjectAsAString;
    // variable to store production push key
    private String prodKey;
    // variable to store pen push key
    private String penKey;
    private String editFeedKey;
    private String penItemKey;
    private String dateString;
    private TextView feedDateTextView;
    private Calendar stockDate;
    private TextView penSelector;
    private List<List<FeedEntry>> bigFeedEntry;
    // variable to hold if production data has change
    private boolean mFeedHasChanged = false;
    private Button mainDeleteButton;
    private Button mainCancelButton;
    private Button mainSaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_feed);
        // Get reference to views in the layout
        initViews();

        // set date to current
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy",
                Locale.getDefault());

        dateString = df.format(date);
        feedDateTextView.setText(dateString);

        keyList = new ArrayList<>();
        mPenEntries = new ArrayList<>();
        bigFeedEntry = new ArrayList<>();

        // Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFeedingDatabaseReference = mFirebaseDatabase.getReference().child("feeding");

        // reference specific part of the database
        penKey = getIntent().getStringExtra("penKey");
        editFeedKey = getIntent().getStringExtra("feedKey");
        penItemKey = getIntent().getStringExtra("penKey2");
        // adapter instance.
        mAdapter = new FeedAdapter(this, this);
        // change title
        setTitle("Add Feed");
        // Check if its a new feed
        // if it its not populate ui
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("feedKey")) {
            // if above its through, change Title to "Edit Production"
            setTitle("Change Feed");
            // convert ProductionEntry object to string and pass it into shared preferences
            Gson gson = new Gson();
            feedDataObjectAsAString = showPreferences("feedObj");
            FeedEntry feedEntry = gson.fromJson(feedDataObjectAsAString, FeedEntry.class);
            populateUI(feedEntry);
        }

        feed = mAdapter.getFeed();
        // spinner setup
        setupSpinner();

        // set feed date
        pickDate();


        RetrieveFeedData();
        RetrievePenData();
        PenSelector();


    }

    /**
     * Helper method containing references to views in xml layout.
     */
    private void initViews() {
        feedName = findViewById(R.id.change_feed_name);
        mFeedSpinner = findViewById(R.id.change_feed_type);
        feedTypeEditText = findViewById(R.id.change_custom_feedType);
        quantityEditText = findViewById(R.id.change_feed_quantity);
        checkBox = findViewById(R.id.change_checkBox);
        feedDateTextView = findViewById(R.id.change_feed_feed_date);
        penSelector = findViewById(R.id.change_feed_pen_selector);
        mainDeleteButton = findViewById(R.id.change_feed_main_delete_button);
        mainCancelButton = findViewById(R.id.change_feed_main_cancel_button);
        mainSaveButton = findViewById(R.id.change_feed_main_save_button);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        feedName.setOnTouchListener(mTouchListener);
        mFeedSpinner.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        feedDateTextView.setOnTouchListener(mTouchListener);
        penSelector.setOnTouchListener(mTouchListener);

        // Set onClickListeners on the save, cancel and delete buttons
        mainSaveButton.setOnClickListener(this);
        mainCancelButton.setOnClickListener(this);
        mainDeleteButton.setOnClickListener(this);

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
        return sharedPreferences.getString(key, GSON_DEFAULT);
    }


    /**
     * Method to check if checkbox in layout is ticked. if it is, enter custom feed.
     */
    private void customFeed() {
        if (checkBox.isChecked()) {
            feedTypeEditText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Populate Ui with data from data base
     *
     * @param feed
     */
    private void populateUI(FeedEntry feed) {
        if (feed == null) {
            feedName.setText("");
            quantityEditText.setText("");
            //mFeedType = feed.getFeedType();
            return;
        }
        feedName.setText(feed.getFeedBrand());
        quantityEditText.setText(String.valueOf(feed.getQuantity()));
        mFeedType = feed.getFeedType();

        switch (mFeedType) {
            case FEED_BROILERS_STARTER:
                mFeedSpinner.setSelection(1);
                break;
            case FEED_CHICKS_MARSH:
                mFeedSpinner.setSelection(2);
                break;
            case FEED_GROWERS_MARSH:
                mFeedSpinner.setSelection(3);
                break;
            case FEED_LAYERS_MARSH:
                mFeedSpinner.setSelection(4);
                break;
            default:
                mFeedSpinner.setSelection(0);
                break;
        }
        /*if (checkBox.isChecked()){
            feedTypeEditText.setVisibility(View.VISIBLE);
            feedTypeEditText.setText(feed.getFeedType());
        }*/
    }

    /**
     * Method for adding/saving feed into the database
     */
    private void addFeed() {

        String feedBrand = feedName.getText().toString().trim();
        int quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
        String status = "Active";


        // Constructor of Production Entry class
        final FeedEntry feedEntry = new FeedEntry(penName, dateString, feedBrand, mFeedType, status, quantity
        );
        // push key for production
        String feedKey = mFeedingDatabaseReference.child(penKey).child("feed").push().getKey();
        // save in shared preference, with startDate as key
        //SavePreferences(startDate, feedKey);

        // check if there is a product key
        if (editFeedKey == null) {
            // If there is no key, then it is a new feed entry

        }
        // reference database and add data.
        mFeedingDatabaseReference.child(penKey).child("feed").child(feedKey).setValue(feedEntry);

        finish();
    }

    private void deleteFeed() {
        // TODO SOLVE THIS DELETION ISSUE
        // Only perform the delete if this is an existing feed.
        mFeedingDatabaseReference.child(penItemKey).child("feed").child(editFeedKey).removeValue();
        finish();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the feed.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_feed_types, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mFeedSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mFeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.broiler_starter))) {
                        mFeedType = FEED_BROILERS_STARTER; // Broilers starter
                    } else if (selection.equals(getString(R.string.chicks_marsh))) {
                        mFeedType = FEED_CHICKS_MARSH; // Chicks marsh
                    } else if (selection.equals(getString(R.string.growers_marsh))) {
                        mFeedType = FEED_GROWERS_MARSH; // Growers marsh
                    } else if (selection.equals(getString(R.string.layers_marsh))) {
                        mFeedType = FEED_LAYERS_MARSH; // Layers marsh
                    } else {
                        mFeedType = FEED_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mFeedType = 0; // Unknown
            }
        });
    }

    /**
     * Helper method to get string format of the date.
     */
    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateString = sdf.format(stockDate.getTime());
        feedDateTextView.setText(dateString);
        populateUI(null);
        qqqq();

    }

    /**
     * Method for date picker dialog.
     */
    private Calendar pickDate() {
        stockDate = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    stockDate.set(Calendar.YEAR, year);
                    stockDate.set(Calendar.MONTH, month);
                    stockDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel();
                }, stockDate.get(Calendar.YEAR), stockDate.get(Calendar.MONTH),
                stockDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        feedDateTextView.setOnClickListener(v -> datePickerDialog.show());
        return stockDate;
    }


    /**
     * for choosing pen
     */
    @SuppressLint("ClickableViewAccessibility")
    private void PenSelector() {

        // Handle click events on penSelector
        penSelector.setOnTouchListener(new View.OnTouchListener() {

            Boolean isTouch = true;

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                //Log.v(TAG,"production penKey6 is " + penSelector.getText());
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Handle event when the right drawable is touched
                    if (event.getRawX() >= (penSelector.getRight() -
                            penSelector.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        // if the drawable right is touched
                        if (isTouch) {
                            // add one to the list index
                            m++;
                        }
                        // set correct pen key
                        penKey = keyList.get(m);
                        // get the pen name at index m and set it on the selector
                        penName = mPenEntries.get(m).getPenName();
                        penSelector.setText(mPenEntries.get(m).getPenName());

                        populateUI(null);
                        qqqq();


                        isTouch = true;

                        // if the list is at the last index, start over. Hence set m=0
                        if (m == mPenEntries.size() - 1) {
                            m = 0;
                            isTouch = false;
                        }


                        return true;
                    } else {
                        // if the arrayList index is at 0
                        if (m == 0) {
                            // set the arrayList index to the last index
                            m = mPenEntries.size() - 1;
                        } else {
                            // minus one from the arrayList index
                            m--;
                        }
                        // get the pen name at index m and set it on the selector
                        penName = mPenEntries.get(m).getPenName();
                        penSelector.setText(mPenEntries.get(m).getPenName());
                        // set correct pen key
                        penKey = keyList.get(m);

                        // call listener
                        //attachProductionDatabaseListener();
                        //populateUI(mProductionEntries);
                        populateUI(null);
                        qqqq();

                    }
                }


                return true;
            }

        });

    }

    /**
     * Get pen entries from database singleton class with view model.
     */
    private void RetrievePenData() {
        // Database singleton class instance
        databaseSingleton = DatabaseSingleton.getInstance();
        // PenViewModel Instance
        PenViewModel penViewModel = new ViewModelProvider(ChangeFeed.this).get(PenViewModel.class);

        // Call observer to notify when pen key data changes.
        penViewModel.getKeyEntry().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> keys) {
                keyList = keys;
                penKey = keys.get(m);
                Log.v(TAG, "penkey is ss " + penKey);


            }

        });

        // call observer to notify when pen data changes
        penViewModel.getPenEntry().observe(this, penEntries -> {
            // set penEntry
            mPenEntries = penEntries;
            // Ensure penEntries arrayList is not null and contains at least one item
            if (mPenEntries != null && mPenEntries.size() > 0) {
                // Get pen name and set to pen selector text view.
                penName = mPenEntries.get(m).getPenName();
                penSelector.setText(mPenEntries.get(m).getPenName());
                penKey = keyList.get(m);
                //attachProductionDatabaseListener();

                qqqq();
            }
        });


    }

    private void qqqq() {
        //FeedEntry nnn = null;


        for (List<FeedEntry> ere : bigFeedEntry) {
            for (FeedEntry ssa : ere) {
                if (ssa.getFeedDate().equals(dateString) && ssa.getPenName().equals(penName)) {
                    populateUI(ssa);
                    Log.v(TAG, "mmmm is good");
                    mainDeleteButton.setVisibility(View.VISIBLE);
                }
            }

           /* if (bigFeedEntry.get(m).get(i).getFeedDate() == null) {
                return;
            }
            Log.v(TAG, "bbbb is " + bigFeedEntry.get(m).get(i).getFeedDate());
            if (bigFeedEntry.get(m).get(i).getFeedDate().equals(dateString)) {
                populateUI(bigFeedEntry.get(m).get(i));
            }*/
        }


        /*if (bigFeedEntry.iterator().hasNext()){
            for (int i = 0; i < bigFeedEntry.get(m).size(); i++) {
                if (bigFeedEntry.get(m).get(i).getFeedDate() == null) {
                    return;
                }
                Log.v(TAG, "bbbb is " + bigFeedEntry.get(m).get(i).getFeedDate());
                if (bigFeedEntry.get(m).get(i).getFeedDate().equals(dateString)) {
                    populateUI(bigFeedEntry.get(m).get(i));
                }
            }

        }*/
    }

    /**
     * Retrieve feed data from singleton class using viewModel
     */
    private void RetrieveFeedData() {
        // ChangeFeedViewModel Instance
        ChangeFeedViewModel viewModel = new ViewModelProvider(this).get(ChangeFeedViewModel.class);
        // Observe for data changes.
        viewModel.getFeedEntry().observe(this, (List<List<FeedEntry>> feed) -> {
            // set data on mProductionEntries arrayList.
            bigFeedEntry = feed;

            // TODO HANDLE WHEN AND HOW TO USE EACH OF THE RETRIEVED LIST
        });

        viewModel.getFeedKeyEntry().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                editFeedKey = strings.get(m);
            }
        });
    }

    // know when complete
    FirebaseMultiQuery firebaseMultiQuery;

    @Override
    protected void onStart() {
        super.onStart();
        oneQuery();
    }

    /**
     * Instances for database query
     */
    private void oneQuery() {
        //firebaseMultiQuery = new FirebaseMultiQuery(mPensDatabaseReference, mProductionDatabaseReference);
        firebaseMultiQuery = FirebaseMultiQuery.getInstance();
        firebaseMultiQuery.setRefs(Home.mPensDatabaseReference, Home.mProductionDatabaseReference);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(ChangeFeed.this, databaseSingleton  /*databaseSingleton.new AllOnCompleteListener()*/);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detach listeners
        firebaseMultiQuery.stop();
        // Clear the list
        mPenEntries.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_add_new, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "add new pen" menu option
            // intent to open AddNew Activity
            case R.id.save_pen:
                addFeed();
                break;
            case R.id.delete_production:
                showDeleteConfirmationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mFeedHasChanged = true;
            return false;
        }
    };


    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // if the production has'nt changed, continue with handling the back button press
        if (!mFeedHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup dialog and warn the user
        // Create a click listener to handle the user confirming that changes
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User clicked "Discard" button, close the current activity
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangeDialog(discardButtonClickListener);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangeDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create AlertDialog.Builder and set the message and click listeners
        // for the positive and negative buttons on the dialog.
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "keep editing" button, so dismiss the dialog,
                // and continue editing the production.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show AlertDialog
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_production_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteFeed();

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onItemClickListener(String startDate) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_feed_main_save_button:
                addFeed();
                break;
            case R.id.change_feed_main_delete_button:
                showDeleteConfirmationDialog();
                break;
            case R.id.change_feed_main_cancel_button:
                finish();
            default:
                break;
        }
    }
}