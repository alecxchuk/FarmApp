package android.example.farmer.Adapters;

import android.content.Context;
import android.example.farmer.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BreedSourceAdapter extends RecyclerView.Adapter<BreedSourceAdapter.BreedSourceViewHolder> {
    private static final String TAG = BreedSourceAdapter.class.getSimpleName();
    private Context mContext;
    private List<String> mList;
    // variable to store reference to list item clickListener
    final private ListItemClickListener mOnClickListener;

    public BreedSourceAdapter(Context context, ListItemClickListener mOnClickListener) {
        mContext = context;

        this.mOnClickListener = mOnClickListener;
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    @NonNull
    @Override
    public BreedSourceAdapter.BreedSourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pen_breed_list_item, parent,
                false);
        return new BreedSourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreedSourceAdapter.BreedSourceViewHolder holder, int position) {
        // get breed stored in arrayList.
        String breed = mList.get(position);
        // set breed to textView
        holder.textView.setText(breed);
        // call method to number each item in the recycler view.
        holder.bind(position + 1);
    }

    public void setBreed(List<String> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        Log.d(TAG, "breedList size is " + mList.size());
        return mList.size();

    }


    public class BreedSourceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Variable for textView
        private TextView textView, number;

        public BreedSourceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Reference textViews in list item layout
            textView = itemView.findViewById(R.id.pen_breed_list_item_breed);
            number = itemView.findViewById(R.id.pen_breed_list_item_breed_no);
            itemView.setOnClickListener(this);
        }

        /**
         * Method to number each item in the recycler view
         *
         * @param listIndex
         */
        void bind(int listIndex) {
            number.setText((String.valueOf(listIndex)));
        }

        @Override
        public void onClick(View v) {
            // get item that was clicked
            int clickedPosition = getAdapterPosition();
            //
            mOnClickListener.onListItemClick(clickedPosition);

        }
    }
}
