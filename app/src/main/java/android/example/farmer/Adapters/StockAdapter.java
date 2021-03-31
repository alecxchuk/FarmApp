package android.example.farmer.Adapters;

import android.example.farmer.R;
import android.example.farmer.source.Stock;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_VIEW = 1;
    private static final int FIRST_LIST_ITEM_VIEW = 2;
    private static final int FIRST_LIST_HEADER_VIEW = 3;
    private static final String TAG = StockAdapter.class.getSimpleName();

    // Class variables for the List that holds task data and the Context
    private List<Stock> stockList = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        if (viewType == FOOTER_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_footer_list_item, parent, false);
            FooterViewHolder vh = new FooterViewHolder(v);
            return vh;

        } else if (viewType == FIRST_LIST_ITEM_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stocks_list_item, parent, false);
            FirstListItemViewHolder vh = new FirstListItemViewHolder(v);
            return vh;

        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_header_list_item, parent, false);
            FirstListHeaderViewHolder vh = new FirstListHeaderViewHolder(v);
            return vh;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof FirstListHeaderViewHolder) {
                FirstListHeaderViewHolder vh = (FirstListHeaderViewHolder) holder;

            } else if (holder instanceof FirstListItemViewHolder) {
                FirstListItemViewHolder vh = (FirstListItemViewHolder) holder;
                vh.bindViewFirstList(position);

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder vh = (FooterViewHolder) holder;
                vh.bindViewFooter(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Stock> getPen() {
        return stockList;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setPen(List<Stock> mStockList) {
        this.stockList = mStockList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int stockListSize = 0;

        if (stockList == null) return 0;


        if (stockList != null)
            stockListSize = stockList.size();

        if (stockListSize > 0)
            return 1 + stockListSize + 1;   // first list header, first list size, footer
        else if (stockListSize == 0)
            return 2;                       // second list header, second list size, footer

        else return 0;
    }

    @Override
    public int getItemViewType(int position) {

        int firstListSize = 0;

        if (stockList == null)
            return super.getItemViewType(position);


        if (stockList != null)
            firstListSize = stockList.size();
        Log.v(TAG, "stockList size is " + stockList.size());

        if (firstListSize > 0) {
            if (position == 0) return FIRST_LIST_HEADER_VIEW;
            else if (position == firstListSize + 1)
                return FOOTER_VIEW;
            else return FIRST_LIST_ITEM_VIEW;

        } else if (firstListSize == 0) {
            if (position == 0) return FIRST_LIST_HEADER_VIEW;
            else return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        // List items of first list
        private TextView stockDate;
        private TextView stockChicks;

        // List items of second list
        private TextView stockHens;
        private TextView stockCocks, stockType;

        // Element of footer view
        private TextView stockTotal, footerChicks, footerHens, footerCocks;

        public ViewHolder(final View itemView) {
            super(itemView);

            // Get the view of the elements of first list
            stockDate = itemView.findViewById(R.id.stock_date);
            stockChicks = itemView.findViewById(R.id.stock_chicks);

            stockHens = itemView.findViewById(R.id.stock_hens);
            stockCocks = itemView.findViewById(R.id.stock_cocks);
            stockType = itemView.findViewById(R.id.stock_type);

            // Get the view of the footer elements
            stockTotal = itemView.findViewById(R.id.stock_footer_total);
            footerChicks = itemView.findViewById(R.id.stock_footer_chicks);
            footerHens = itemView.findViewById(R.id.stock_footer_hens);
            footerCocks = itemView.findViewById(R.id.stock_footer_cocks);


        }


        public void bindViewFirstList(int pos) {

            // Decrease pos by 1 as there is a header view now.
            pos = pos - 1;

            final String stockDate = stockList.get(pos).getPenStockDate();
            final int stockChicks = stockList.get(pos).getChicks();
            final int stockHens = stockList.get(pos).getHens();
            final int stockCocks = stockList.get(pos).getCocks();
            final int stockType = stockList.get(pos).getStockType();
            Log.v(TAG,"chicks= " + stockChicks + "hens= " + stockHens + "cocks= " + stockCocks);


            this.stockDate.setText(stockDate);
            this.stockChicks.setText(String.valueOf(stockChicks));
            this.stockHens.setText(String.valueOf(stockHens));
            this.stockCocks.setText(String.valueOf(stockCocks));


            switch (stockType) {
                case 1:
                    this.stockType.setText(R.string.layers);
                    break;
                case 2:
                    this.stockType.setText(R.string.broilers);
                    break;
                case 3:
                    this.stockType.setText(R.string.turkey);
                    break;
                case 4:
                    this.stockType.setText(R.string.cockerel);
                    break;
                default:
                    this.stockType.setText(R.string.breed_unknown);
                    break;
            }

        }

        public void bindViewFooter(int pos) {

            //stockTotal.setText("This is footer");

            int totalChicks = stockList.stream().mapToInt(Stock::getChicks).sum();
            int totalHens = stockList.stream().mapToInt(Stock::getHens).sum();
            int totalCocks = stockList.stream().mapToInt(Stock::getCocks).sum();



            this.footerChicks.setText(String.valueOf(totalChicks));
            this.footerHens.setText(String.valueOf(totalHens));
            this.footerCocks.setText(String.valueOf(totalCocks));
        }
    }

    private class FirstListHeaderViewHolder extends ViewHolder {
        public FirstListHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class FirstListItemViewHolder extends ViewHolder {
        public FirstListItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class FooterViewHolder extends ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
