package android.example.farmer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.ViewModel.PenViewModel;
import android.example.farmer.ViewModel.ProductionzViewModel;
import android.example.farmer.source.PenEntry;
import android.example.farmer.source.ProductionEntry;
import android.example.farmer.source.ReturnTypes;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddNewProduction extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AddNewProduction.class.getSimpleName();
    // Calender field to instantiate calender class
    private Calendar stockDate;
    // variable to store dateString
    private String dateString;
    // String key for referencing data in shared preferences.
    private static final String GSON_DEFAULT = "default";


    // variable for textViews in xml layout
    private TextView dateTextView, firstProdTextView, secondProdTextView, thirdProdTextView;
    // variable to hold if production data has change
    private boolean mProductionHasChanged = false;

    // Integer variables
    private int firstProd, secondProd, thirdProd, total;

    // Instance variable fields for firebase databse and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProductionDatabaseReference;
    private String productionDataObjectAsAString;
    // variable to store production push key
    private String prodKey;
    // variable to store pen push key
    private String penKey;
    private String editProdKey;
    private String penItemKey;


    private DatabaseReference mPensDatabaseReference;

    // Instance variable for child event listener.
    private ChildEventListener mChildEventListener;

    // private variable to store PenEntry list
    private List<PenEntry> mPenEntries;

    // list variable for storing keys
    private List<String> keyList;


    private ConstraintLayout firstProductionContainer, secondProductionContainer, thirdProductionContainer;
    private EditText collectedEggsEditText1, crackedEggsEditText1, collectedEggsEditText2, crackedEggsEditText2,
            collectedEggsEditText3, crackedEggsEditText3;
    private TextView goodEggsTextView1, goodEggsTextView2, goodEggsTextView3;
    private TextView penSelector;
    private ImageButton save1, save2, save3;
    private int firstCrackedEggs;
    private int secondCrackedEggs;
    private int thirdCrackedEggs;
    private int firstGoodEggs;
    private int secondGoodEggs;
    private int thirdGoodEggs;
    private ReturnTypes returnTypes1;
    private ReturnTypes returnTypes2;
    private ReturnTypes returnTypes3;
    private List<ProductionEntry> mProductionEntries;
    private ProductionEntry productionEntry2;
    private String penName;
    private List<List<ProductionEntry>> bigProductionEntry = new ArrayList<>();
    private Button mainSaveButton;
    private Button mainCancelButton;
    private Button mainDeleteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_production);
        // contains references for all the views in the xml layout.
        initViews();

        // Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // set date to current
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy",
                Locale.getDefault());

        dateString = df.format(date);
        dateTextView.setText(dateString);


        // reference specific part of the database
       /* penKey = getIntent().getStringExtra("penKey");
        editProdKey = getIntent().getStringExtra("prodKey");
        penItemKey = getIntent().getStringExtra("penKey2");
        Log.i(TAG, "key is" + penKey + penItemKey);
        Log.i(TAG, "editProdkey is" + editProdKey);*/


        // check for if intent is null
        //mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product").child(penKey).child("production");
        mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product");


        // PenEntry ArrayList instance
        mPenEntries = new ArrayList<>();
        mProductionEntries = new ArrayList<>();
        keyList = new ArrayList<>();

        // reference specific part of the database
        mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
        mPensDatabaseReference.keepSynced(true);

        // add child listener for pen and production database
        //attachDatabaseReadListener();
        RetrieveProductionData();
        RetrievePenData();


        //attachProductionDatabaseListener();


        // change title
        setTitle("Add Production");
        // Check if its a new production
        // if it its not populate ui
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("prodkey")) {
            // if above its through, change Title to "Edit Production"
            setTitle("Edit Production");
            // convert ProductionEntry object to string and pass it into shared preferences
            Gson gson = new Gson();
            productionDataObjectAsAString = showPreferences("productionkey");
            ProductionEntry productionEntry = gson.fromJson(productionDataObjectAsAString, ProductionEntry.class);
            //populateUI(mProductionEntries);
        }


        openProductionLayout();
        listenForChanges();
        //data();
        PenSelector();
        //getPen();
        // launch date picker and store date in dateString variable
        pickDate();
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

    private View.OnTouchListener mTouchListener = (view, motionEvent) -> {
        mProductionHasChanged = true;
        return false;
    };

    /**
     * Helper method containing references to views in xml layout.
     */
    private void initViews() {
        dateTextView = findViewById(R.id.add_prod_date);
        firstProdTextView = findViewById(R.id.add_prod_first_production);
        secondProdTextView = findViewById(R.id.add_prod_second_prod);
        thirdProdTextView = findViewById(R.id.add_prod_third_prod);
        firstProductionContainer = findViewById(R.id.add_prod_first_production_container);
        secondProductionContainer = findViewById(R.id.add_prod_second_production_container);
        thirdProductionContainer = findViewById(R.id.add_prod_third_production_container);
        penSelector = findViewById(R.id.add_prod_pen_selector);
        // variable for save, cancel and delete buttons
        mainSaveButton = findViewById(R.id.add_prod_save_button);
        mainCancelButton = findViewById(R.id.add_prod_cancel_button);
        mainDeleteButton = findViewById(R.id.add_prod_delete_button);


        collectedEggsEditText1 = findViewById(R.id.add_prod_first_prod_collected_eggs);
        goodEggsTextView1 = findViewById(R.id.add_new_first_prod_good_eggs);
        crackedEggsEditText1 = findViewById(R.id.add_prod_first_prod_cracked);
        save1 = findViewById(R.id.add_prod_first_prod_saveButton);

        // second prod
        crackedEggsEditText2 = findViewById(R.id.add_prod_second_prod_cracked);
        collectedEggsEditText2 = findViewById(R.id.add_prod_second_prod_collected_eggs);
        save2 = findViewById(R.id.add_prod_second_prod_saveButton);
        goodEggsTextView2 = findViewById(R.id.add_prod_second_prod_good);

        // Third production
        crackedEggsEditText3 = findViewById(R.id.add_prod_third_prod_cracked);
        collectedEggsEditText3 = findViewById(R.id.add_prod_third_prod_collected_eggs);
        save3 = findViewById(R.id.add_prod_third_prod_saveButton);
        goodEggsTextView3 = findViewById(R.id.add_prod_third_prod_good);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        penSelector.setOnTouchListener(mTouchListener);
        dateTextView.setOnTouchListener(mTouchListener);
        firstProdTextView.setOnTouchListener(mTouchListener);
        secondProdTextView.setOnTouchListener(mTouchListener);
        thirdProdTextView.setOnTouchListener(mTouchListener);

        collectedEggsEditText1.setOnTouchListener(mTouchListener);
        crackedEggsEditText1.setOnTouchListener(mTouchListener);

        collectedEggsEditText2.setOnTouchListener(mTouchListener);
        crackedEggsEditText2.setOnTouchListener(mTouchListener);

        collectedEggsEditText3.setOnTouchListener(mTouchListener);
        crackedEggsEditText3.setOnTouchListener(mTouchListener);

        // set click listener on buttons
        mainSaveButton.setOnClickListener(this);
        mainCancelButton.setOnClickListener(this);
        mainDeleteButton.setOnClickListener(this);
    }

    Boolean isClosed = true;

    /**
     * Make production containers visible
     */
    private void openProductionLayout() {
        int right = R.drawable.ic_baseline_keyboard_arrow_down_24;
        int down = R.drawable.ic_baseline_keyboard_arrow_right_24;


        firstProdTextView.setOnClickListener(v -> {
            if (isClosed) {
                isClosed = false;
                firstProductionContainer.setVisibility(View.VISIBLE);
                firstProdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, right, 0);

            } else {
                isClosed = true;
                firstProductionContainer.setVisibility(View.GONE);
                firstProdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, down, 0);
            }
        });

        secondProdTextView.setOnClickListener(v -> {
            if (isClosed) {
                isClosed = false;
                secondProductionContainer.setVisibility(View.VISIBLE);
                secondProdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, right, 0);

            } else {
                isClosed = true;
                secondProductionContainer.setVisibility(View.GONE);
                secondProdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, down, 0);

            }
        });

        thirdProdTextView.setOnClickListener(v -> {
            if (isClosed) {
                isClosed = false;
                thirdProductionContainer.setVisibility(View.VISIBLE);
                thirdProdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, right, 0);

            } else {
                isClosed = true;
                thirdProductionContainer.setVisibility(View.GONE);
                thirdProdTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, down, 0);

            }
        });
    }


    /**
     * set text watch listeners on views
     */
    private void listenForChanges() {
        // first production
        returnTypes1 = textWatchers(collectedEggsEditText1, goodEggsTextView1, crackedEggsEditText1, save1, firstProdTextView,
                firstProductionContainer);

        // second production
        returnTypes2 = textWatchers(collectedEggsEditText2, goodEggsTextView2, crackedEggsEditText2,
                save2, secondProdTextView, secondProductionContainer);

        // third production
        returnTypes3 = textWatchers(collectedEggsEditText3, goodEggsTextView3, crackedEggsEditText3,
                save3, thirdProdTextView, thirdProductionContainer);

    }

    private void calculateProductions() {
        // Total first production
        firstProd = returnTypes1.getCollected();
        // Total first cracked production
        firstCrackedEggs = returnTypes1.getCracked();
        // Total first good eggs
        firstGoodEggs = firstProd - firstCrackedEggs;


        // Total second production
        secondProd = returnTypes2.getCollected();
        // Total second cracked production
        secondCrackedEggs = returnTypes2.getCracked();

        // Total second good eggs
        secondGoodEggs = secondProd - secondCrackedEggs;


        // Total third production
        thirdProd = returnTypes3.getCollected();
        // Total third cracked production
        thirdCrackedEggs = returnTypes2.getCracked();
        // Total third good eggs
        thirdGoodEggs = thirdProd - thirdCrackedEggs;
    }

    /**
     * Helper method inputing daily production.
     *
     * @param collectedEggsEditText
     * @param goodEggsTextView
     * @param crackedEggsEditText
     */
    @NonNull
    private ReturnTypes textWatchers(@NonNull EditText collectedEggsEditText, TextView goodEggsTextView,
                                     @NonNull EditText crackedEggsEditText, @NonNull ImageButton saveButton,
                                     TextView productionTextView, View layoutContainer) {

        ReturnTypes returnTypes = new ReturnTypes();
    /*
    Text watcher to determine when collected eggs has changed.
     */
        collectedEggsEditText.addTextChangedListener(new TextWatcher() {
            // indicates if the change was made by the TextWatcher itself.
            Boolean ignore = false;
            String ll;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ignore) {
                    return;
                }

                ignore = true; // prevent infinite loop

                // prevent the TextView from being empty
                // instead set text as 0.
                if (s.toString().matches("")) {
                    goodEggsTextView.setText("0");
                } else {
                    // Set the number of good eggs on the text view
                    goodEggsTextView.setText(s);
                    ll = s.toString();
                }
                ignore = false; // release, so the TextWatcher start to listen again.


            }
        });

        /*
        Text watcher to determine when cracked eggs has changed.
         */
        crackedEggsEditText.addTextChangedListener(new TextWatcher() {
            // indicates if the change was made by the TextWatcher itself.
            Boolean ignore = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ignore) {
                    return;
                }

                ignore = true; // prevent infinite loop
                // Change your text here.
                int collectedEggs;
                if (collectedEggsEditText.getText().toString().matches("")) {
                    collectedEggs = 0;
                } else {
                    collectedEggs = Integer.parseInt(collectedEggsEditText.getText().toString());
                }


                if (s.toString().matches("")) {
                    goodEggsTextView.setText(String.valueOf(collectedEggs));
                } else {
                    int goodEggs = collectedEggs - Integer.parseInt(s.toString());
                    goodEggsTextView.setText(String.valueOf(goodEggs));
                }
                ignore = false; // release, so the TextWatcher start to listen again.
                // Set the number of good eggs on the text view

            }
        });

        // Define what happens when the first production save button is clicked
        saveButton.setOnClickListener(v -> {
            // prevent collectEggsEditText from being empty.
            if (collectedEggsEditText.getText().toString().matches("")) {
                collectedEggsEditText.setText("0");
                //isClosed=true;
                //layoutContainer.setVisibility(View.GONE);
                //return;
            }

            if (crackedEggsEditText.getText().toString().matches("")) {
                crackedEggsEditText.setText("0");
            }

            // Total collected eggs
            int as = Integer.parseInt(collectedEggsEditText.getText().toString().trim());

            // set the firstProduction as the number of collected eggs.
            productionTextView.setText(String.valueOf(as));
            isClosed = true;
            layoutContainer.setVisibility(View.GONE);

            // Total cracked eggs
            int bs = Integer.parseInt(crackedEggsEditText.getText().toString().trim());

            // input into object
            returnTypes.setCollected(as);
            returnTypes.setCracked(bs);
        });

        // return object
        return returnTypes;
    }

    private int m = 0;


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
                        // if the production has'nt changed, continue with handling the back button press
                        if (!mProductionHasChanged) {
                            // set correct pen key
                            penKey = keyList.get(m);
                            // get the pen name at index m and set it on the selector
                            penName = mPenEntries.get(m).getPenName();
                            penSelector.setText(mPenEntries.get(m).getPenName());

                            //attachProductionDatabaseListener();
                            //populateUI(mProductionEntries);
                            populateUI(null);
                            qqqq();


                            isTouch = true;





                        }else {

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
                        //penSelector.setEnabled(false);
                        //penSelector.postDelayed(() -> penSelector.setEnabled(true), 200);
                    }
                }


                return true;
            }

        });

    }


    /**
     * Helper method to set click listener on text views to edit text
     */
    private void clickText() {
        // click listener to open alert dialog
        firstProdTextView.setOnClickListener(v -> addProductionDialogBox("First Production",
                firstProdTextView));
        secondProdTextView.setOnClickListener(v -> addProductionDialogBox("Second Production",
                secondProdTextView));
        thirdProdTextView.setOnClickListener(v -> addProductionDialogBox("Third Production",
                thirdProdTextView));
    }

    /**
     * Helper method to add a new production into firebase database.
     */
    private void save() {
        //ProductionEntry production;
        // get text from text view as int and store in integer variable
        /*firstProd = Integer.parseInt((firstProdTextView.getText()).toString());
        secondProd = Integer.parseInt((secondProdTextView.getText()).toString());
        thirdProd = Integer.parseInt((thirdProdTextView.getText()).toString());*/
        calculateProductions();
        total = firstProd + secondProd + thirdProd;


        /*
        Check if its a new production entry for a particular date
         */
        if (bigProductionEntry != null) {
            for (ProductionEntry entry : bigProductionEntry.get(m)) {
                if (entry.getProductionDate().matches(dateString)) {
                    Toast.makeText(AddNewProduction.this, "Date already exits, Set new Date and " +
                            "try again", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
            /*if (mProductionEntries.get(m).getProductionDate().matches(dateString)) {
                Toast.makeText(AddNewProduction.this, "Date already exits, Set new Date and " +
                        "try again", Toast.LENGTH_LONG).show();
                return;
            } *///else {
        // Constructor of Production Entry class
        ProductionEntry production = new ProductionEntry(penName, firstProd, secondProd,
                thirdProd, total, firstCrackedEggs, firstGoodEggs, secondCrackedEggs, secondGoodEggs,
                thirdCrackedEggs, thirdGoodEggs, dateString);

        // push key for production
        prodKey = mProductionDatabaseReference.child(penKey).child("production").push().getKey();
        // save in shared preference, with key
        SavePreferences(dateString, prodKey);
        // reference database and add data.
        mProductionDatabaseReference.child(penKey).child("production").child(prodKey).setValue(production);
        // Exit the activity
        finish();
        //}
        // }
    }

    private void delete() {
        mProductionDatabaseReference.child(penItemKey).child("production").child(editProdKey).removeValue();
        finish();
    }


    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param production the taskEntry to populate the UI
     */
    private void populateUI(ProductionEntry production) {
        Log.v(TAG, "populate production is" + production);
        if (production == null) {
            firstProdTextView.setText("0");
            secondProdTextView.setText("0");
            thirdProdTextView.setText("0");
            //dateTextView.setText((production.get(m).getProductionDate()));

            collectedEggsEditText1.setText("0");
            crackedEggsEditText1.setText("0");
            goodEggsTextView1.setText("0");

            collectedEggsEditText2.setText("0");
            crackedEggsEditText2.setText("0");
            goodEggsTextView2.setText("0");

            collectedEggsEditText3.setText("0");
            crackedEggsEditText3.setText("0");
            goodEggsTextView3.setText("0");
            return;
        }


        firstProdTextView.setText(String.valueOf(production.getFirstProduction()));
        secondProdTextView.setText(String.valueOf(production.getSecondProduction()));
        thirdProdTextView.setText(String.valueOf(production.getThirdProduction()));
        dateTextView.setText(production.getProductionDate());

        collectedEggsEditText1.setText(String.valueOf(production.getFirstProduction()));
        crackedEggsEditText1.setText(String.valueOf(production.getFirstCrackedEggs()));
        goodEggsTextView1.setText(String.valueOf(production.getFirstGoodEggs()));

        collectedEggsEditText2.setText(String.valueOf(production.getSecondProduction()));
        crackedEggsEditText2.setText(String.valueOf(production.getSecondCrackedEggs()));
        goodEggsTextView2.setText(String.valueOf(production.getSecondGoodEggs()));

        collectedEggsEditText3.setText(String.valueOf(production.getThirdProduction()));
        crackedEggsEditText3.setText(String.valueOf(production.getThirdCrackedEggs()));
        goodEggsTextView3.setText(String.valueOf(production.getThirdGoodEggs()));

        dateTextView.setText(String.valueOf(production.getProductionDate()));


        /*firstProdTextView.setText(String.valueOf(production.get(m).getFirstProduction()));
        secondProdTextView.setText(String.valueOf((production.get(m).getSecondProduction())));
        thirdProdTextView.setText(String.valueOf((production.get(m).getThirdProduction())));
        dateTextView.setText((production.get(m).getProductionDate()));

        collectedEggsEditText1.setText(String.valueOf((production.get(m).getFirstProduction())));
        crackedEggsEditText1.setText(String.valueOf((production.get(m).getFirstCrackedEggs())));
        goodEggsTextView1.setText(String.valueOf((production.get(m).getFirstGoodEggs())));

        collectedEggsEditText2.setText(String.valueOf((production.get(m).getSecondProduction())));
        crackedEggsEditText2.setText(String.valueOf((production.get(m).getSecondCrackedEggs())));
        goodEggsTextView2.setText(String.valueOf((production.get(m).getSecondGoodEggs())));

        collectedEggsEditText3.setText(String.valueOf((production.get(m).getThirdProduction())));
        crackedEggsEditText3.setText(String.valueOf((production.get(m).getThirdCrackedEggs())));
        goodEggsTextView3.setText(String.valueOf((production.get(m).getThirdGoodEggs())));

        dateTextView.setText(String.valueOf((production.get(m).getProductionDate())));*/

    }

    private void addProductionDialogBox(final String title, final TextView textView) {

        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialogs = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialogs.setTitle(title);

        // Setting Dialog Message
        //alertDialogs.setMessage(message);
        final EditText editText = new EditText(this);
        alertDialogs.setView(editText);

        final int[] set = {0};


        // Setting Positive "Yes" Button
        alertDialogs.setPositiveButton("YES",
                (dialog, which) -> {
                    // Write your code here to execute after dialog
                    Toast.makeText(getApplicationContext(), title.concat("added"), Toast.LENGTH_SHORT).show();
                    set[0] = Integer.parseInt(editText.getText().toString().trim());
                    textView.setText(String.valueOf(set[0]));

                });
        // Setting Negative "NO" Button
        alertDialogs.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });
        alertDialogs.show();

    }

    /**
     * Method for date picker dialog.
     */
    private void pickDate() {
        // Instantiate calendar
        stockDate = Calendar.getInstance();
        // Initialize DatePickerDialog
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // store year, month and dayOfMonth in stockDate
                    stockDate.set(Calendar.YEAR, year);
                    stockDate.set(Calendar.MONTH, month);
                    stockDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel();
                }, stockDate.get(Calendar.YEAR), stockDate.get(Calendar.MONTH),
                stockDate.get(Calendar.DAY_OF_MONTH));
        // date to be picked should not exceed present date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        // clickListener on calendarImage to launch date picker
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }

    /**
     * Helper method to get string format of the date.
     */
    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        // convert date object to date string variable.
        dateString = sdf.format(stockDate.getTime());
        dateTextView.setText(dateString);
        populateUI(null);
        qqqq();
    }

    private void data() {

        /*
        Database reference path to date, to check if a particular date already exists in the database
         */

        mFirebaseDatabase.getReference("product").child(penKey).child("production").orderByChild("productionDate").equalTo(dateString).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            //dateString exists in Database. Thus there is an existing application
                            productionEntry2 = snapshot.getValue(ProductionEntry.class);
                            mProductionEntries.add(productionEntry2);
                            Log.v(TAG, String.valueOf("productionENtry2 is " + dateString + " " + productionEntry2.getProductionDate()));


                            //mProductionEntries.add(productionEntry);
                        } else {
                            //dateString doesn't exists.
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private DatabaseSingleton databaseSingleton;

    private void getPen() {
        databaseSingleton = DatabaseSingleton.getInstance();
        //databaseSingleton.attachDatabaseReadListener();
        List<PenEntry> ooo = Pens.mAdapter.getPen();
        Toast.makeText(AddNewProduction.this, "pen entry size is " + ooo.size(), Toast.LENGTH_LONG).show();

    }

    /**
     * Get pen entries from database singleton class with view model.
     */
    private void RetrievePenData() {
        // Database singleton class instance
        databaseSingleton = DatabaseSingleton.getInstance();
        // PenViewModel Instance
        PenViewModel penViewModel = new ViewModelProvider(AddNewProduction.this).get(PenViewModel.class);

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

    private void  qqqq() {
        /*Log.v(TAG, "date is " + mProductionEntries.get(m).getProductionDate());
        Log.v(TAG, "sizer is" +mProductionEntries.get(m).getPenName());
        Log.v(TAG,"sizer is " + m);



        if (mProductionEntries.get(m).getPenName().equals(penName) && mProductionEntries.get(m).getProductionDate().equals(dateString)) {
            //if (mProductionEntries.contains(penName)&&mProductionEntries.contains(dateString)){
            Log.v(TAG, "index m is " + m);

            // if there is an existing production entry in the database, populate the ui.
            populateUI(mProductionEntries);
        }*/
        /*if (bigProductionEntry.get(m).size()>1){
            for (int i=0;i<bigProductionEntry.get(m).size();i++){
               if( bigProductionEntry.get(m).get(i).getProductionDate().equals(dateString)){
                   Log.v(TAG, "big list size is " );
                   ProductionEntry Wentry =bigProductionEntry.get(m).get(i);
                   List<ProductionEntry > aasd =new ArrayList<>();
                   aasd.add(Wentry);
                   populateUI(aasd);
               }
            }
        }else{
                if(bigProductionEntry.get(m).get(0).getProductionDate().equals(dateString)){
                    ProductionEntry Wentry =bigProductionEntry.get(m).get(0);
                    List<ProductionEntry > aasd =new ArrayList<>();
                    aasd.add(Wentry);
                    populateUI(aasd);

            }
        }*/


        for (int i = 0; i < bigProductionEntry.get(m).size(); i++) {
            if (bigProductionEntry.get(m).get(i).getProductionDate().equals(dateString)) {
                populateUI(bigProductionEntry.get(m).get(i));
                mainDeleteButton.setVisibility(View.VISIBLE);
            }
        }


    }

    /**
     * Retrieve production data from singleton class using viewModel
     */
    private void RetrieveProductionData() {
        // ProductionzViewModel Instance
        ProductionzViewModel viewModel = new ViewModelProvider(this).get(ProductionzViewModel.class);
        // Observe for data changes.
        viewModel.getProductionEntry().observe(this, production -> {
            // set data on mProductionEntries arrayList.
            //mProductionEntries = production;
            List<ProductionEntry> doubleList = new ArrayList<>();
            List<ProductionEntry> singleList = new ArrayList<>();
            List<ProductionEntry> sasd = new ArrayList<>();
            ;
            bigProductionEntry = production;

            // TODO HANDLE WHEN AND HOW TO USE EACH OF THE RETRIEVED LIST


            //Log.v(TAG, "big list size is " + sasd.get(0).getTotalProduction());


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
        firebaseMultiQuery.setRefs(mPensDatabaseReference, mProductionDatabaseReference);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(AddNewProduction.this, databaseSingleton  /*databaseSingleton.new AllOnCompleteListener()*/);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detach listeners
        firebaseMultiQuery.stop();
        // Clear the list
        mPenEntries.clear();
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
                // add data to mPenEntries ArrayList.
                mPenEntries.add(penEntry);

                penSelector.setText(mPenEntries.get(m).getPenName());

                // list that contains the keys for each pen.
                keyList.add(snapshot.getKey());

                penKey = keyList.get(m);
                attachProductionDatabaseListener();

                // Add arrayList to PenAdapter.
                //mAdapter.setPen(mPenEntries);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = keyList.indexOf(snapshot.getKey());
                mPenEntries.remove(index);
                keyList.remove(index);
                //mAdapter.notifyDataSetChanged();
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

    private void attachProductionDatabaseListener() {
        penKey = keyList.get(m);


        // Initialize the ChildEventListener
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Retrieve PenEntry data from DataSnapshot
                //ProductionEntry productionEntry = snapshot.getValue(ProductionEntry.class);
                // add data to mPenEntries Arraylist.
                //mProductionEntries.add(productionEntry2);
                // list that contains the keys for each pen.
                // keyList.add(snapshot.getKey());
                // Add arrayList to PenAdapter.
                //mAdapter.setPen(mPenEntries);
                ;
                if (snapshot.exists()) {
                    //dateString exists in Database. Thus there is an existing application
                    productionEntry2 = snapshot.getValue(ProductionEntry.class);
                    mProductionEntries.add(productionEntry2);
                    Log.v(TAG, String.valueOf("productionENtry2 is " + dateString + " " + productionEntry2.getProductionDate()));
                    //populateUI(mProductionEntries);

                    //mProductionEntries.add(productionEntry);
                } else {
                    //dateString doesn't exists.
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //int index = keyList.indexOf(snapshot.getKey());
                //mPenEntries.remove(index);
                //keyList.remove(index);
                //mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        Log.v(TAG, "penkey is " + penKey);
        mFirebaseDatabase.getReference("product").child(penKey).child("production").
                orderByChild("productionDate").equalTo(dateString).addChildEventListener(mChildEventListener);
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
                save();

                break;
            case R.id.delete_production:
                showDeleteConfirmationDialog();
                break;
            case android.R.id.home:
                finish();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // if the production has'nt changed, continue with handling the back button press
        if (!mProductionHasChanged) {
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
                delete();

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_prod_save_button:
                save();
                break;

            case R.id.add_prod_delete_button:
                showDeleteConfirmationDialog();
                break;

            case R.id.add_prod_cancel_button:
                finish();
            default:
                break;
        }
    }


}