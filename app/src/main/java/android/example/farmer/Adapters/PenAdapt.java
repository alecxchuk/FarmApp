package android.example.farmer.Adapters;

import android.content.Context;
import android.example.farmer.R;
import android.example.farmer.source.PenEntry;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PenAdapt extends RecyclerView.Adapter<PenAdapt.PenViewHolder> {
    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";
    private static final String TAG = PenAdapt.class.getSimpleName();

    // Member variable to handle item clicks
    final private PenAdapt.ItemClickListener mItemClickListener;
    // Class variables for the List that holds task data and the Context
    private List<PenEntry> mPenEntries;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    /**
     * Constructor for the PenAdapter that initializes the Context.
     *
     * @param context     the current Context
     * @param listener    the ItemClickListener
     * @param
     */
    public PenAdapt(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
        //this.mPenEntries = mPenEntries;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @NonNull
    @Override
    public PenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.pen_list_item, parent, false);

        return new PenViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(PenViewHolder holder, int position) {
        // Determine the values of the wanted data
        PenEntry penEntry = mPenEntries.get(position);
        String penName = penEntry.getPenName();
        String penBreed = penEntry.getPenBreed();


        //Set values
        holder.penNameTextView.setText(String.valueOf(penName));
        holder.penBreedTextView.setText(String.valueOf(penBreed));

    }


    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mPenEntries == null) {
            return 0;
        }
        Log.v(TAG, "SIZE IS" + mPenEntries.size());
        return mPenEntries.size();
    }

    public List<PenEntry> getPen() {
        return mPenEntries;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setPen(List<PenEntry> penEntries) {
        mPenEntries = penEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId, String name);
    }

    // Inner class for creating ViewHolders
    class PenViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView penNameTextView, penBreedTextView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public PenViewHolder(View itemView) {
            super(itemView);
            penNameTextView = itemView.findViewById(R.id.penList_pen_name);
            penBreedTextView = itemView.findViewById(R.id.penList_pen_breed);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = getAdapterPosition();
            String name = mPenEntries.get(getAdapterPosition()).getPenName();
            mItemClickListener.onItemClickListener(elementId,name);
        }
    }
}
