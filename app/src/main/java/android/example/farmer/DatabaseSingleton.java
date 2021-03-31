package android.example.farmer;

import android.example.farmer.source.FeedEntry;
import android.example.farmer.source.PenEntry;
import android.example.farmer.source.ProductionEntry;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseSingleton implements Serializable, OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
    private static final String TAG = DatabaseSingleton.class.getSimpleName();
    private static volatile DatabaseSingleton sSoleInstance;

    private List<PenEntry> mPenEntries = new ArrayList<>();
    public MutableLiveData<List<PenEntry>> entry = new MutableLiveData<>();
    public MutableLiveData<List<String>> keyEntry = new MutableLiveData<>();

    public MutableLiveData<List<List<ProductionEntry>>> productionEntry = new MutableLiveData<>();
    public MutableLiveData<List<String>> productionKeyEntry = new MutableLiveData<>();

    public MutableLiveData<List<List<FeedEntry>>> feedEntry = new MutableLiveData<>();
    public MutableLiveData<List<String>> feedKeyEntry = new MutableLiveData<>();

    public List<String> penKeyList = new ArrayList<>();

    private static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mPensDatabaseReference = mFirebaseDatabase.getReference().child("pens");
    //FirebaseMultiQuery firebaseMultiQuery= new FirebaseMultiQuery(mPensDatabaseReference);
    private DatabaseReference mProductionDatabaseReference = mFirebaseDatabase.getReference().child("product");
    private DatabaseReference mFeedingDatabaseReference = mFirebaseDatabase.getReference().child("feeding");


    //private constructor. you are not allowing other class to create a new instance
    private DatabaseSingleton() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }


    public static DatabaseSingleton getInstance() {

        //Double check locking pattern
        if (sSoleInstance == null) { //Check for the first time

            synchronized (DatabaseSingleton.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (sSoleInstance == null) sSoleInstance = new DatabaseSingleton();
            }
        }
        return sSoleInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected DatabaseSingleton readResolve() {
        return getInstance();
    }

    public List<PenEntry> getPenEntry() {
        return this.mPenEntries;
    }

    public MutableLiveData<List<PenEntry>> setPenEntry() {
        return entry;
    }

    public MutableLiveData<List<String>> getKeyList() {
        return keyEntry;
    }

    public MutableLiveData<List<List<ProductionEntry>>> setProductionEntry() {
        return productionEntry;
    }

    public MutableLiveData<List<String>> getProductionKeyList() {
        return productionKeyEntry;
    }

    public MutableLiveData<List<List<FeedEntry>>> setFeedEntry() {
        return feedEntry;
    }

    public MutableLiveData<List<String>> getFeedKeyList() {
        return feedKeyEntry;
    }

    /*public class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {

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
    }*/


    @Override
    public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
        if (task.isSuccessful()) {
            mProductionDatabaseReference.keepSynced(true);
            final Map<DatabaseReference, DataSnapshot> result = task.getResult();



            // Look up DataSnapshot objects using the same DatabaseReferences you passed into FirebaseMultiQuery
            DataSnapshot snapshot = result.get(mPensDatabaseReference);
            Log.v(TAG, "zzzzzz is " + snapshot);


            if (snapshot == null) {
                Log.v(TAG, " snapshot is empty");
                return;
            }
            for (DataSnapshot p : snapshot.getChildren()) {
                Log.v(TAG, "map list is " + p);

                PenEntry penEntry = p.getValue(PenEntry.class);

                mPenEntries.add(penEntry);
                entry.setValue(mPenEntries);


                // list that contains the keys for each pen.
                penKeyList.add(p.getKey());
                keyEntry.setValue(penKeyList);
            }

            /**
             * Production
             */
            DataSnapshot uu = result.get(mProductionDatabaseReference);
            List<String> keys = new ArrayList<>();
            DataSnapshot ggg = null;
            List<DataSnapshot> qqq = new ArrayList<>();
            List<ProductionEntry> ooo = new ArrayList<>();
            List<DataSnapshot> opop = new ArrayList<>();


            for (DataSnapshot prods : uu.getChildren()) {
                opop.add(prods);
                keys.add(prods.getKey());
            }

            for (DataSnapshot dataSnapshots : opop) {
                for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {

                    qqq.add(dataSnapshot);
                }
            }

            List<List<ProductionEntry>> biggerProductionList = new ArrayList<>();

            List<ProductionEntry> smallerProductionList;
            
            for (int i =0;i<qqq.size();i++){
                DataSnapshot abc = qqq.get(i);
                smallerProductionList= new ArrayList<>();

                for (DataSnapshot xyz:abc.getChildren()){

                    ProductionEntry entry = xyz.getValue(ProductionEntry.class);
                    smallerProductionList.add(entry);
                }
                biggerProductionList.add(smallerProductionList);
            }
            productionEntry.setValue(biggerProductionList);



            /*for (DataSnapshot prod : uu.getChildren()) {
                for (DataSnapshot qq : prod.getChildren()) {
                    for (DataSnapshot zz : qq.getChildren()) {
                        ProductionEntry entry = zz.getValue(ProductionEntry.class);

                        mProduction.add(entry);
                        productionEntry.setValue(mProduction);
                        Log.v(TAG, "aaaa is " + entry.getTotalProduction());


                        List<String> productionKeys = new ArrayList<>();
                        productionKeyEntry.setValue(productionKeys);
                    }
                }


            }*/

            /**
             * Feed
             */
            DataSnapshot qq = result.get(mFeedingDatabaseReference);
            List<DataSnapshot> zzz = new ArrayList<>();
            List<DataSnapshot> popo = new ArrayList<>();
            List<String> feedKeys = new ArrayList<>();
            assert qq != null;
            for (DataSnapshot prods : qq.getChildren()) {
                popo.add(prods);
                //keys.add(prods.getKey());
            }
            for (DataSnapshot dataSnapshots : popo) {
                for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {

                    zzz.add(dataSnapshot);
                }
            }
            List<List<FeedEntry>> biggerFeedList = new ArrayList<>();

            List<FeedEntry> smallerFeedList;

            for (int i =0;i<zzz.size();i++){
                DataSnapshot abc = zzz.get(i);
                smallerFeedList= new ArrayList<>();

                for (DataSnapshot xyz:abc.getChildren()){

                    FeedEntry entry = xyz.getValue(FeedEntry.class);
                    smallerFeedList.add(entry);
                    feedKeys.add(xyz.getKey());
                    Log.v(TAG, "aaaa is " + xyz.getKey());
                }
                biggerFeedList.add(smallerFeedList);
            }
            feedEntry.setValue(biggerFeedList);
            feedKeyEntry.setValue(feedKeys);
           /*for (int k=0;k<biggerFeedList.get(0).size();k++) {
                Log.v(TAG, "aaaa is " + biggerFeedList.get(0).get(k).getFeedBrand());
            }*/

        } else {
            //exception = task.getException();
            // log the error or whatever you need to do
        }
        // Do stuff with views
        //updateUi();

    }


}
