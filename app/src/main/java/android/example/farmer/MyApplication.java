package android.example.farmer;

import android.app.Activity;
import android.app.Application;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class MyApplication extends Application {
    private DatabaseSingleton databaseSingleton;
    private FirebaseDatabase mFirebaseDatabase;
    ;
    private DatabaseReference mPensDatabaseReference;
    DatabaseReference mProductionDatabaseReference;
    public static FirebaseMultiQuery firebaseMultiQuery;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
       /* databaseSingleton = DatabaseSingleton.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
        mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product");
        //FirebaseApp.initializeApp(this);
        firebaseMultiQuery = new FirebaseMultiQuery(mPensDatabaseReference, mProductionDatabaseReference);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        CompleteListener completeListener = new CompleteListener();
        allLoad.addOnCompleteListener(new Activity(), databaseSingleton  );*/

    }

}
