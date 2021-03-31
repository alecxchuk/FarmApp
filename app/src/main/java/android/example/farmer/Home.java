package android.example.farmer;

import android.app.Activity;
import android.example.farmer.ViewModel.MainHomeViewModel;
import android.example.farmer.source.PenEntry;
import android.example.farmer.ui.home.HomeFragment;
import android.example.farmer.ui.home.HomeViewModel;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Map;

public class Home extends AppCompatActivity {

    private static final String TAG = Home.class.getSimpleName();
    // Instance variable for child event listener.
    private ChildEventListener mChildEventListener;

    private HomeViewModel homeViewModel;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_pen, R.id.nav_production, R.id.nav_sales, R.id.nav_medication,
                R.id.nav_statistics)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        databaseSingleton = DatabaseSingleton.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
        mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product");
        mFeedingDatabaseReference = mFirebaseDatabase.getReference().child("feeding");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private DatabaseSingleton databaseSingleton;
    private static FirebaseDatabase mFirebaseDatabase;
    ;
    public static DatabaseReference mPensDatabaseReference;
    public static DatabaseReference mProductionDatabaseReference,mFeedingDatabaseReference;
    public static FirebaseMultiQuery firebaseMultiQuery;


    @Override
    protected void onStart() {
        super.onStart();
        oneQuery();
    }

    private void oneQuery() {
        //firebaseMultiQuery = new FirebaseMultiQuery(mPensDatabaseReference, mProductionDatabaseReference);
        firebaseMultiQuery=FirebaseMultiQuery.getInstance();
        firebaseMultiQuery.setRefs(mPensDatabaseReference,mProductionDatabaseReference,mFeedingDatabaseReference);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(new Activity(), databaseSingleton  /*databaseSingleton.new AllOnCompleteListener()*/);
    }


    @Override
    protected void onStop() {
        super.onStop();
        firebaseMultiQuery.stop();
    }
}