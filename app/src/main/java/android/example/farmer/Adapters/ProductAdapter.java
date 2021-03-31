package android.example.farmer.Adapters;

import android.content.Context;
import android.example.farmer.R;
import android.example.farmer.source.ProductionEntry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductionViewHolder> {
    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Class variables for the List that holds task data and the Context
    private List<ProductionEntry> mProductionEntries;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  the current Context
     *
     */
    public ProductAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public ProductionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.production_list_item, parent, false);

        return new ProductionViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(ProductionViewHolder holder, int position) {
        // Determine the values of the wanted data
        ProductionEntry productionEntry = mProductionEntries.get(position);
        int firstProduction = productionEntry.getFirstProduction();
        int secondProduction = productionEntry.getSecondProduction();
        int thirdProduction = productionEntry.getThirdProduction();
        int totalProduction = productionEntry.getTotalProduction();

        String date = productionEntry.getProductionDate();

        //Set values
        holder.firstProductionView.setText(String.valueOf(firstProduction));
        holder.secondProductionView.setText(String.valueOf(secondProduction));
        holder.thirdProductionView.setText(String.valueOf(thirdProduction));
        holder.totalProductionView.setText(String.valueOf(totalProduction));
        holder.dateView.setText(date);
    }


    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mProductionEntries == null) {
            return 0;
        }
        return mProductionEntries.size();
    }

    public List<ProductionEntry> getProduction() {
        return mProductionEntries;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setProduction(List<ProductionEntry> productionEntries) {
        mProductionEntries = productionEntries;
        notifyDataSetChanged();
    }


    public interface ItemClickListener {
        void onItemClickListener(String date);
    }

    // Inner class for creating ViewHolders
    class ProductionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView firstProductionView, secondProductionView, thirdProductionView, totalProductionView;
        TextView dateView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public ProductionViewHolder(View itemView) {
            super(itemView);

            firstProductionView = itemView.findViewById(R.id.produc_list_item_first);
            secondProductionView = itemView.findViewById(R.id.produc_list_item_second);
            thirdProductionView = itemView.findViewById(R.id.produc_list_item_third);
            totalProductionView = itemView.findViewById(R.id.produc_list_item_total);
            dateView = itemView.findViewById(R.id.produc_list_item_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String productionDate = mProductionEntries.get(getAdapterPosition()).getProductionDate();
            mItemClickListener.onItemClickListener(productionDate);

        }
    }
}
