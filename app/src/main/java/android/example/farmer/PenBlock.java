package android.example.farmer;

import android.content.Intent;
import android.example.farmer.Fragments.FeedFragment;
import android.example.farmer.Fragments.PenData;
import android.example.farmer.Fragments.ProductionFragment;
import android.example.farmer.ui.medication.MedicationFragment;
import android.example.farmer.ui.sales.SalesFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PenBlock extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PenBlock.class.getSimpleName();
    // String key for referencing data in shared preferences.
    public static String PEN_KEY = "penKey";

    // String variable to store penKey
    private String penKey;

    // variables to store textViews and layout
    private TextView infoTextView, producTextView, feedTextView, medicTextView, salesTextView,
            vaccinationTextView, expensesTextView;

    // variable for floating action button
    private FloatingActionButton actionButton;

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pen_block);

        // String extra from intent and stored in penKey string variable
        penKey = getIntent().getStringExtra("key");

        // contains references for all the views in the xml layout.
        initViews();
        /*
         * open Fragments
         */

        openFrag(infoTextView, new PenData());
        openFrag(producTextView, new ProductionFragment());
        openFrag(feedTextView, new FeedFragment());
        //openFrag(medicTextView,);

        // set click listener on views
        producTextView.setOnClickListener(this);
        feedTextView.setOnClickListener(this);
        medicTextView.setOnClickListener(this);
        expensesTextView.setOnClickListener(this);
        salesTextView.setOnClickListener(this);
        vaccinationTextView.setOnClickListener(this);


    }

    /**
     * Helper method containing references to views in xml layout.
     */
    private void initViews() {
        // get reference to textViews in layout
        infoTextView = findViewById(R.id.pen_info);
        producTextView = findViewById(R.id.pen_production);
        feedTextView = findViewById(R.id.pen_feed);
        medicTextView = findViewById(R.id.pen_medic);
        linearLayout = findViewById(R.id.pen_block_layout);
        expensesTextView = findViewById(R.id.pen_expenses);
        salesTextView = findViewById(R.id.pen_sales);
        vaccinationTextView = findViewById(R.id.pen_vaccination);
        actionButton=findViewById(R.id.floatingActionButton);

    }

    /**
     * Helper method to launch fragments
     *
     * @param view     we want to set click listener on
     * @param fragment desired fragment to be launched.
     */
    private void openFrag(View view, final Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString(PEN_KEY, penKey);
        fragment.setArguments(bundle);
        view.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().add(R.id.pen_block_frag_container,
                    fragment).addToBackStack(null).commit();
            linearLayout.setVisibility(View.GONE);
        });
    }


    /**
     * Perform the deletion of a pen from the database.
     */
    private void deletePen() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mPensDatabaseReference = database.getReference().child("pens")
                .child(penKey);
        mPensDatabaseReference.removeValue();
        finish();
    }

    // return to activity when back is pressed
    @Override
    public void onBackPressed() {
        // when back is pressed, return to activity and set layout to be visible
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return;
        }

        super.onBackPressed();
        linearLayout.setVisibility(View.VISIBLE);
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.pen_block_menu_edit);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pen_block_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.pen_block_menu_delete:
                // Delete pen
                deletePen();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pen_production:
                openFrag(producTextView, new ProductionFragment());
                break;
            case R.id.pen_feed:
                openFrag(feedTextView, new FeedFragment());
                break;
            case R.id.pen_sales:
                openFrag(salesTextView, new SalesFragment());
                break;
            case R.id.pen_expenses:
                //openFrag(expensesImageView, new ExpensesFragment());
                break;
            case R.id.pen_medic:
                openFrag(medicTextView, new MedicationFragment());
                break;
            case R.id.pen_vaccination:
                //openFrag(vaccinationImageView,new VaccinationFragment());
                break;
            case R.id.home_feeding_layout:
                Intent intent = new Intent(this, AddNewPen.class);
                startActivity(intent);
            default:
                break;

        }
    }
}