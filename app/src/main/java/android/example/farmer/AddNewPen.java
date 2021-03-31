package android.example.farmer;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.farmer.Adapters.StockAdapter;
import android.example.farmer.source.PenEntry;
import android.example.farmer.source.Stock;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNewPen extends AppCompatActivity {
    // String key for referencing data in shared preferences.
    private static final String GSON_DEFAULT = "newPenDefault";
    private static final String TAG = AddNewPen.class.getSimpleName();

    private long mLastClickTime = 0;

    // Calender field to instantiate calender class
    private Calendar stockDate;

    // variable for calendar image reference
    //private ImageView calenderImage;

    // variable for dateTextView reference
    private TextView AddDetaildateTextView;

    // variable to store dateString
    private String dateString;
    // variable to store object as string
    private String studentDataObjectAsAString;
    // variable to store pen push key
    private String penkey;

    // Variables for editText reference
    private EditText nameEditText, breedEditText, ageEditText, numberEditText, vaccineEditText, birdSourceEditText;
    private Button addDetailButton;

    // Instance variable fields for firebase database and reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPensDatabaseReference;
    private DatabaseReference mProductionDatabaseReference;

    // Instance variable for child event listener.
    private ChildEventListener mChildEventListener;
    private List<Stock> stockArrayList;
    private StockAdapter stockAdapter;
    private RecyclerView stockRecycler;
    private int no_of_chicks;
    private int no_of_hens;
    private int no_of_cocks;
    private int number;
    private PenEntry penEntry2;
    private List<Stock> numberList = new ArrayList<>();

    // spinner breed
    private Spinner mBreedSpinner;

    /**
     * type of the feed. The possible values are:
     * 0 for unknown Breed, 1 for Breed layers, 2 for Breed broilers, 3 for Breed turkey, 4 for Breed cockerel.
     */
    private int mBreedType;

    /*Possible values for the feed type*/
    public static final int BREED_UNKNOWN = 0;
    public static final int BREED_LAYERS = 1;
    public static final int BREED_BROILERS = 2;
    public static final int BREED_TURKEY = 3;
    public static final int BREED_COCKEREL = 4;
    private EditText lossesEditText;
    private int birdLosses;
    private TextView addNewDate;
    public ConstraintLayout constraintLayout;

    private PenBreedViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);
        // contains references for all the views in the xml layout.
        initViews();

        // Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // reference specific part of the database
        mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
        mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product");

        // Check if its a new pen
        // if it its not populate ui
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("key")) {
            Gson gson = new Gson();
            //studentDataObjectAsAString = getIntent().getStringExtra("key");
            showPreferences("key");
            PenEntry penEntry = gson.fromJson(studentDataObjectAsAString, PenEntry.class);
            populateUI(penEntry);
        }
        // launch date picker and store date in dateString variable
        addNewDate.setOnClickListener(v -> pickDate(addNewDate));
        //pickDate();

        // set date to current
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy",
                Locale.getDefault());

        dateString = df.format(date);
        addNewDate.setText(dateString);

        numberListener();
        //attachDatabaseReadListener();
        losses(lossesEditText);
        losses(numberEditText);

        stockAdapter = new StockAdapter();
        stockAdapter.setPen(stockArrayList);
        // Assign Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        stockRecycler.setLayoutManager(layoutManager);
        stockRecycler.setHasFixedSize(true);
        stockRecycler.setAdapter(stockAdapter);


        //addDetail();

        breedEditText.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().add(R.id.addNew_frag_container,
                    new PenBreed()).addToBackStack(null).commit();
            constraintLayout.setVisibility(View.GONE);

            // mis-clicking prevention, using threshold of 1000 ms
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
        });

        /*breedEditText.setOnTouchListener((v, event) -> {
            // this disables the keyboard in an edit text
            int inType = breedEditText.getInputType(); // backup the input type
            breedEditText.setInputType(InputType.TYPE_NULL); // disable soft input
            breedEditText.onTouchEvent(event); // call native handler
            breedEditText.setInputType(inType); // restore input type
            return true;
        });*/

        model = new ViewModelProvider(this).get(PenBreedViewModel.class);
        model.getBreed().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                breedEditText.setText(s);
                Toast.makeText(AddNewPen.this, "breed is" + s, Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
            constraintLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Helper method containing references to views in xml layout.
     */
    private void initViews() {
        // find reference for edit text
        nameEditText = findViewById(R.id.addNew_editText_pen_name);
        breedEditText = findViewById(R.id.addNew_editText_breed);
        ageEditText = findViewById(R.id.addNew_editText_age);
        numberEditText = findViewById(R.id.addNew_editText_number);
        lossesEditText = findViewById(R.id.addNew_editText_losses);
        //vaccineEditText = findViewById(R.id.addNew_editText_vaccine);
        constraintLayout = findViewById(R.id.addNew_container);
        birdSourceEditText = findViewById(R.id.addNew_editText_bird_source);

        addNewDate = findViewById(R.id.addNew_stock_date);
        AddDetaildateTextView = findViewById(R.id.addNew_date_textView);
        //addDetailButton = findViewById(R.id.addNew_add_detail_button);
        stockArrayList = new ArrayList<>();
        stockRecycler = findViewById(R.id.stock_details_recycler);
    }

    /**
     * Helper method to populate UI with data from PenEntry object
     *
     * @param pen PenEntry object
     */
    private void populateUI(PenEntry pen) {
        if (pen == null) {
            return;
        }

        nameEditText.setText(pen.getPenName());
        breedEditText.setText(pen.getPenBreed());
        ageEditText.setText(String.valueOf(pen.getAge()));
        AddDetaildateTextView.setText(pen.getStockDate());
        numberEditText.setText(String.valueOf(pen.getNumber()));

        Stock stock = new Stock();
        stock.setChicks(pen.getNo_of_chicks());

    }

    /**
     * Helper method to retrieve data from shared preference with a key.
     *
     * @param key for referencing particular data in shared preferences.
     */
    private void showPreferences(String key) {
        SharedPreferences sharedPreferences = this
                .getSharedPreferences("key", Context.MODE_PRIVATE);
        studentDataObjectAsAString = sharedPreferences.getString(key, GSON_DEFAULT);
    }

    /**
     * Helper method to add a new pen into firebase database.
     */
    private void savePen() {
        // get text from EditText views
        String nameString = nameEditText.getText().toString().trim();
        String breedString = breedEditText.getText().toString().trim();
        int age = Integer.parseInt(ageEditText.getText().toString().trim());
        String birdSource = birdSourceEditText.getText().toString().trim();


        // Constructor of Production Entry class
        final PenEntry pen = new PenEntry(nameString, breedString, birdSource, dateString, age, number,
                birdLosses, no_of_chicks, no_of_hens, no_of_cocks);
        // push key for pen
        penkey = mPensDatabaseReference.push().getKey();
        // reference database and add data.
        mPensDatabaseReference.child(penkey).setValue(pen);
        mPensDatabaseReference.child(penkey).child("num").child(dateString).setValue(penEntry2);


        mProductionDatabaseReference.child(penkey).child("production")/*.push().setValue(pen)*/;
        // save in shared preference, with key
        SavePreferences(nameString, penkey);
        Toast.makeText(this, "penKey is " + penkey, Toast.LENGTH_LONG).show();
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

    public String getAge() {
        int age = 0;
        //stockDate = Calendar.getInstance();
        Calendar todayDate = Calendar.getInstance();
        //stockDate.set(year, month, day);

        long diff = todayDate.getTimeInMillis() - stockDate.getTimeInMillis();
        long week = 604800000;


        if (diff >= week) {
            age = (int) (diff / (1000 * 60 * 60 * 24 * 7));
        } else {
            age = (int) (diff / (1000 * 60 * 60 * 24));
        }


        if (todayDate.get(Calendar.DAY_OF_YEAR) < this.stockDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        String ageS = Integer.toString(age);


        return ageS;

    }

    private void numberListener() {
        //lossesEditText.setOnClickListener(v -> addDetail());
        //addDetailButton.setOnClickListener(v -> addDetail());
    }

    private void losses(View editText) {
        editText.setOnClickListener(v -> {

            LayoutInflater factory = LayoutInflater.from(this);

            //text_entry is an Layout XML file containing three edit text fields to display in alert dialog
            final View textEntryView = factory.inflate(R.layout.new_pen_number_dialog, null);
            final EditText input1 = textEntryView.findViewById(R.id.number_dialog_chicks);
            final EditText input2 = textEntryView.findViewById(R.id.number_dialog_hens);
            final EditText input3 = textEntryView.findViewById(R.id.number_dialog_cocks);
            final TextView dialogDate = textEntryView.findViewById(R.id.number_dialog_date);
            mBreedSpinner = textEntryView.findViewById(R.id.add_detail_breed_spinner);


            setupSpinner();

            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy",
                    Locale.getDefault());
            //DateFormat df = SimpleDateFormat.getDateInstance();

            String dateString = df.format(date);

            dialogDate.setText(dateString);

            // TODO SET DATE PICKER ON DIALOG DATE TEXTVIEW
            dialogDate.setOnClickListener(v1 -> {
                pickDate(dialogDate);
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Test for preventing dialog close").setView(textEntryView);
            builder.setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
//Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    int output1;
                    int output2;
                    int output3;
                    // check if edit text is empty, if it is set text to 0.
                    if (input1.getText().toString().matches("")) {
                        input1.setText("0");
                    }
                    if (input2.getText().toString().matches("")) {
                        input2.setText("0");
                    }
                    if (input3.getText().toString().matches("")) {
                        input3.setText("0");
                    }
                    // get text from edit text fields
                    output1 = Integer.parseInt(input1.getText().toString());
                    output2 = Integer.parseInt(input2.getText().toString());
                    output3 = Integer.parseInt(input3.getText().toString());

                    // Check nothing has been inputed
                    if (output1 == 0 && output2 == 0 && output3 == 0) {
                        dialog.dismiss();
                        return;
                    }


                    // Check if it was the losses edit text clicked
                    if (editText == lossesEditText) {

                        if (output1 > no_of_chicks || output2 > no_of_hens || output3 > no_of_cocks) {
                            Toast.makeText(AddNewPen.this, "Try again", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // Convert outputs to negative integers
                        output1 = -output1;
                        output2 = -output2;
                        output3 = -output3;


                        // calculate the total number of birds lost
                        birdLosses = birdLosses + output1 + output2 + output3;

                        // total number of chicks, hens and cocks left
                        no_of_chicks = no_of_chicks + output1;
                        no_of_hens = no_of_hens + output2;
                        no_of_cocks = no_of_cocks + output3;

                        // Total number of birds left.
                        number = no_of_chicks + no_of_hens + no_of_cocks;


                    }

                    // store in java object
                    Stock stock = new Stock();
                    stock.setChicks(output1);
                    stock.setHens(output2);
                    stock.setCocks(output3);
                    stock.setpenStockDate(dateString);
                    stock.setStockType(mBreedType);


                    // populate list
                    stockArrayList.add(stock);

                    // get the adapter and notify it that data has changed
                    StockAdapter adapter = (StockAdapter) stockRecycler.getAdapter();
                    adapter.notifyDataSetChanged();

                    // Check if the add detail button clicked
                    if (editText == numberEditText) {

                        // Total number of chicks, hens and cocks.
                        no_of_chicks = stockArrayList.stream().mapToInt(Stock::getChicks).sum();
                        no_of_hens = stockArrayList.stream().mapToInt(Stock::getHens).sum();
                        no_of_cocks = stockArrayList.stream().mapToInt(Stock::getCocks).sum();
                        // Total number of birds
                        number = no_of_chicks + no_of_hens + no_of_cocks;
                    }


                    if (!(no_of_chicks < 0 || no_of_hens < 0 || no_of_cocks < 0)) {
                        wantToCloseDialog = true;
                        // set the total number of birds on the number edit text.
                        numberEditText.setText(String.valueOf(number));
                        // set total losses on losses edit text.
                        lossesEditText.setText(String.valueOf(-birdLosses));
                        penEntry2 = new PenEntry(output1, output2, output3);
                    }


                    // we want to close the dialog now

                    if (wantToCloseDialog)
                        dialog.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });

            /*final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert./*setIcon(R.drawable.icon).setTitle("EntertheText:").setView(textEntryView).setPositiveButton("Save",
                    (dialog, whichButton) -> {
                        int output1;
                        int output2;
                        int output3;
                        // check if edit text is empty, if it is set text to 0.
                        if (input1.getText().toString().matches("")) {
                            input1.setText(0);
                        }
                        if (input2.getText().toString().matches("")) {
                            input2.setText("0");
                        }
                        if (input3.getText().toString().matches("")) {
                            input3.setText("0");
                        }
                        // get text from edit text fields
                        output1 = Integer.parseInt(input1.getText().toString());
                        output2 = Integer.parseInt(input2.getText().toString());
                        output3 = Integer.parseInt(input3.getText().toString());

                        if (output1 == 0 && output2 == 0 && output3 == 0) {
                            return;
                        }

                        // Check if it was the losses edit text clicked
                        if (editText == lossesEditText) {
                            // Convert outputs to negative integers
                            output1 = -output1;
                            output2 = -output2;
                            output3 = -output3;

                            // calculate the total number of birds lost
                            birdLosses = output1 + output2 + output3;

                            // total number of chicks, hens and cocks left

                            no_of_chicks = no_of_chicks + output1;
                            no_of_hens = no_of_hens + output2;
                            no_of_cocks = no_of_cocks + output3;

                            // Total number of birds left.
                            number = no_of_chicks + no_of_hens + no_of_cocks;

                        }

                        // store in java object
                        Stock stock = new Stock();
                        stock.setChicks(output1);
                        stock.setHens(output2);
                        stock.setCocks(output3);
                        stock.setpenStockDate(dateString);
                        stock.setStockType(mBreedType);


                        // get the adapter and notify it that data has changed
                        StockAdapter adapter = (StockAdapter) stockRecycler.getAdapter();
                        adapter.notifyDataSetChanged();

                        // Check if the add detail button clicked
                        if (editText == numberEditText) {
                            // Total number of chicks, hens and cocks.
                            no_of_chicks = stockArrayList.stream().mapToInt(Stock::getChicks).sum();
                            no_of_hens = stockArrayList.stream().mapToInt(Stock::getHens).sum();
                            no_of_cocks = stockArrayList.stream().mapToInt(Stock::getCocks).sum();
                            // Total number of birds
                            number = no_of_chicks + no_of_hens + no_of_cocks;
                        }


                        if (!(no_of_chicks < 0 || no_of_hens < 0 || no_of_cocks < 0)) {
                            // populate list
                            stockArrayList.add(stock);
                            // set the total number of birds on the number edit text.
                            numberEditText.setText(String.valueOf(number));
                            // set total losses on losses edit text.
                            lossesEditText.setText(String.valueOf(-birdLosses));
                            penEntry2 = new PenEntry(output1, output2, output3);
                        } else {

                        }
*/
            /* User clicked OK so do some stuff */
                    /*}).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            /*
                             * User clicked cancel so do some stuff
                             */
                            /*dialog.cancel();
                        }
                    });
            alert.show();*/
        });
    }


    private void addDetail() {

        LayoutInflater factory = LayoutInflater.from(this);

        //text_entry is an Layout XML file containing three edit text fields to display in alert dialog
        final View textEntryView = factory.inflate(R.layout.new_pen_number_dialog, null);
        final EditText input1 = textEntryView.findViewById(R.id.number_dialog_chicks);
        final EditText input2 = textEntryView.findViewById(R.id.number_dialog_hens);
        final EditText input3 = textEntryView.findViewById(R.id.number_dialog_cocks);
        final TextView dialogDate = textEntryView.findViewById(R.id.number_dialog_date);
        mBreedSpinner = textEntryView.findViewById(R.id.add_detail_breed_spinner);


        setupSpinner();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy",
                Locale.getDefault());
        //DateFormat df = SimpleDateFormat.getDateInstance();

        String dateString = df.format(date);

        dialogDate.setText(dateString);

        // TODO SET DATE PICKER ON DIALOG DATE TEXTVIEW
            /*dialogDate.setOnClickListener(v1 -> {
                pickDate();
            });*/

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert./*setIcon(R.drawable.icon).*/setTitle("EntertheText:").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int output1;
                        int output2;
                        int output3;
                        // check if edit text is empty, if it is set text to 0.
                        if (input1.getText().toString().matches("")) {
                            input1.setText(0);
                        }
                        if (input2.getText().toString().matches("")) {
                            input2.setText("0");
                        }
                        if (input3.getText().toString().matches("")) {
                            input3.setText("0");
                        }
                        // get text from edit text fields
                        output1 = Integer.parseInt(input1.getText().toString());
                        output2 = Integer.parseInt(input2.getText().toString());
                        output3 = Integer.parseInt(input3.getText().toString());

                        if (output1 == 0 && output2 == 0 && output3 == 0) {
                            return;
                        }


                        // store in java object
                        Stock stock = new Stock();
                        stock.setChicks(output1);
                        stock.setHens(output2);
                        stock.setCocks(output3);
                        stock.setpenStockDate(dateString);
                        stock.setStockType(mBreedType);
                        Toast.makeText(AddNewPen.this.getApplicationContext(), "breed is " + mBreedType, Toast.LENGTH_LONG).show();

                        // populate list
                        stockArrayList.add(stock);

                        // get the adapter and notify it that data has changed
                        StockAdapter adapter = (StockAdapter) stockRecycler.getAdapter();
                        adapter.notifyDataSetChanged();


                        no_of_chicks = stockArrayList.stream().mapToInt(Stock::getChicks).sum();
                        no_of_hens = stockArrayList.stream().mapToInt(Stock::getHens).sum();
                        no_of_cocks = stockArrayList.stream().mapToInt(Stock::getCocks).sum();
                        number = no_of_chicks + no_of_hens + no_of_cocks;

                        numberEditText.setText(String.valueOf(number));
                        penEntry2 = new PenEntry(output1, output2, output3);








                        /* User clicked OK so do some stuff */
                    }
                }).setNegativeButton("Cancel",
                (dialog, whichButton) -> {
                    /*
                     * User clicked cancel so do some stuff
                     */
                    dialog.cancel();
                });
        alert.show();

    }




    /**
     * Setup the dropdown spinner that allows the user to select the type of the feed.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter breedSpinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.array_breed_types, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        breedSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mBreedSpinner.setAdapter(breedSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mBreedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.layers))) {
                        // Broilers starter
                        mBreedType = BREED_LAYERS; // layers
                    } else if (selection.equals(getString(R.string.broilers))) {
                        mBreedType = BREED_BROILERS; // broilers
                    } else if (selection.equals(getString(R.string.turkey))) {
                        mBreedType = BREED_TURKEY; // turkey
                    } else if (selection.equals(getString(R.string.cockerel))) {
                        mBreedType = BREED_COCKEREL; // cockerel
                    } else {
                        mBreedType = BREED_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBreedType = 0; // Unknown
            }
        });
    }


    /**
     * Method for date picker dialog.
     */
    private void pickDate(TextView textView) {
        // Instantiate calendar
        stockDate = Calendar.getInstance();
        // Initialize DatePickerDialog
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // store year, month and dayOfMonth in stockDate
                    stockDate.set(Calendar.YEAR, year);
                    stockDate.set(Calendar.MONTH, month);
                    stockDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel(textView);
                }, stockDate.get(Calendar.YEAR), stockDate.get(Calendar.MONTH),
                stockDate.get(Calendar.DAY_OF_MONTH));

        // date to be picked should not exceed present date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        // clickListener on calendarImage to launch date picker
        //addNewDate.setOnClickListener(v -> datePickerDialog.show());

        datePickerDialog.show();
    }

    /**
     * Helper method to get string format of the date.
     */
    private void updateLabel(TextView textView) {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        dateString = sdf.format(stockDate.getTime());
        textView.setText(dateString);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

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
                Stock penEntry = snapshot.getValue(Stock.class);
                // add data to mPenEntries ArrayList.
                numberList.add(penEntry);
                // list that contains the keys for each pen.
                //keyList.add(snapshot.getKey());
                // Add arrayList to PenAdapter.
                stockAdapter.setPen(numberList);
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
                /*int index = keyList.indexOf(snapshot.getKey());
                mPenEntries.remove(index);
                keyList.remove(index);
                mAdapter.notifyDataSetChanged();*/
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
                // save pen
                savePen();
                // Exit Activity
                finish();
                break;
            case R.id.delete_production:
                // delete pen
                //deletePen();
        }
        return super.onOptionsItemSelected(item);
    }
}