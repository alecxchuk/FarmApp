package android.example.farmer;

import android.example.farmer.source.PenEntry;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
    private static final String TAG =CompleteListener.class.getSimpleName() ;
    public static List<PenEntry> mPenEntries = new ArrayList<>();
    private static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();;

    private  static   DatabaseReference mPensDatabaseReference=mFirebaseDatabase.getReference().child("pens");
    @Override
    public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
        if (task.isSuccessful()) {
            final Map<DatabaseReference, DataSnapshot> result = task.getResult();

            // Look up DataSnapshot objects using the same DatabaseReferences you passed into FirebaseMultiQuery
            DataSnapshot snapshot = result.get(mPensDatabaseReference);

            if (snapshot==null){
                Log.v(TAG, " snapshot is empty");
                return;
            }
            for (DataSnapshot p : snapshot.getChildren()) {
                Log.v(TAG, "map list is " + p);
                PenEntry penEntry = p.getValue(PenEntry.class);
                mPenEntries.add(penEntry);
                Pens.mAdapter.setPen(mPenEntries);
                Log.v(TAG, "pens list is " + penEntry);
            }

        } else {
            //exception = task.getException();
            // log the error or whatever you need to do
        }
        // Do stuff with views
        //updateUi();

    }

}

