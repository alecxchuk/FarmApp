package android.example.farmer.Adapters;

import android.content.Context;
import android.example.farmer.R;
import android.example.farmer.source.FeedEntry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Class variables for the List that holds task data and the Context
    private List<FeedEntry> mFeedEntries;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    /**
     * Constructor for the PenAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public FeedAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.feed_list_item, parent, false);

        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        // Determine the values of the wanted data
        FeedEntry feedEntry = mFeedEntries.get(position);
        String feedBrand = feedEntry.getFeedBrand();
        int feedType = feedEntry.getFeedType();
        String status = feedEntry.getStatus();
        int quantity = feedEntry.getQuantity();





        //Set values
        holder.Brand.setText(String.valueOf(feedBrand));
        //holder.type.setText(String.valueOf(feedType));
        holder.status.setText(String.valueOf(status));

        holder.quantity.setText(String.valueOf(quantity));

        switch (feedType) {
            case 1:
                holder.type.setText(R.string.broiler_starter);
                break;
            case 2:
                holder.type.setText(R.string.chicks_marsh);
                break;
            case 3:
                holder.type.setText(R.string.growers_marsh);
                break;
            case 4:
                holder.type.setText(R.string.layers_marsh);
                break;
            default:
                holder.type.setText(R.string.feed_unknown);
                break;
        }
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mFeedEntries == null) {
            return 0;
        }
        return mFeedEntries.size();
    }

    public List<FeedEntry> getFeed() {
        return mFeedEntries;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setFeed(List<FeedEntry> feedEntries) {
        mFeedEntries = feedEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(String startDate);
    }

    class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView Brand, type, quantity, start, end, status;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            Brand = itemView.findViewById(R.id.feed_item_feedBrand);
            type = itemView.findViewById(R.id.feed_item_feedType);
            quantity = itemView.findViewById(R.id.feed_item_quantity);
            start = itemView.findViewById(R.id.feed_item_startDate);
            end = itemView.findViewById(R.id.feed_item_endDate);
            status = itemView.findViewById(R.id.feed_item_status);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            //String date = mFeedEntries.get(getAdapterPosition()).getStartDate();
            //mItemClickListener.onItemClickListener(date);
        }
    }
}
