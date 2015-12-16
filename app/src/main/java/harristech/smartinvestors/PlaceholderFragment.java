package harristech.smartinvestors;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import harristech.smartinvestors.data.FinancialContract.StockEntry;

/**
 * Created by henry on 11/14/14.
 */
public class PlaceholderFragment extends Fragment {
    private FavorListAdapter mFavorListAdapter;
    private StockInfo[] stockInfos;

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mFavorListAdapter = new FavorListAdapter(
                getActivity(),              // Current context (this activity)
                R.layout.list_item_favor,   // Layout for a single row
                new ArrayList<StockInfo>()
                );

        // Get a reference to the ListView, and attach this adapter to it
        ListView listView = (ListView) rootView.findViewById(R.id.list_favor);
        listView.setAdapter(mFavorListAdapter);

        // Create the click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                StockInfo stockInfo = stockInfos[position];

                Intent intent = new Intent(getActivity(), FinancialActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, new String[]{
                                stockInfo.getTicker(),
                                stockInfo.getCompany_name(),
                                stockInfo.getExchange_market(),
                                stockInfo.getStock_type()
                        });
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveStock();
    }

    private void retrieveStock() {
        String sortOrder = StockEntry.COLUMN_STOCK_TICKER + " ASC";
        Cursor stockCursor = getActivity().getContentResolver().query(
                StockEntry.CONTENT_URI,  // Uri to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                sortOrder // sort order
        );
        if (stockCursor.moveToFirst()){
            stockInfos = new StockInfo[stockCursor.getCount()];
            int position = 0;
            mFavorListAdapter.clear();
            do{
                // do what ever you want here
                String ticker = stockCursor.getString(
                        stockCursor.getColumnIndex(StockEntry.COLUMN_STOCK_TICKER));
                String company = stockCursor.getString(
                        stockCursor.getColumnIndex(StockEntry.COLUMN_COMPANY_NAME)
                );
                String exchange = stockCursor.getString(
                        stockCursor.getColumnIndex(StockEntry.COLUMN_EXCH_DISP)
                );
                String type = stockCursor.getString(
                        stockCursor.getColumnIndex(StockEntry.COLUMN_TYPE_DISP)
                );
                StockInfo stockInfo = new StockInfo(ticker, company, exchange, type);
                stockInfos[position] = stockInfo;
                ++position;
                mFavorListAdapter.add(stockInfo);
            }while(stockCursor.moveToNext());
        } else {
            mFavorListAdapter.clear();
            ContentValues stockValues = new ContentValues();
            stockValues.put(StockEntry.COLUMN_STOCK_TICKER, "SFTBY");
            stockValues.put(StockEntry.COLUMN_COMPANY_NAME, "Softbank Corp.");
            stockValues.put(StockEntry.COLUMN_EXCH_DISP, "OTCMKS");
            stockValues.put(StockEntry.COLUMN_TYPE_DISP, "Equity");
            getActivity().getContentResolver()
                    .insert(StockEntry.CONTENT_URI, stockValues);
            stockValues.put(StockEntry.COLUMN_STOCK_TICKER, "YHOO");
            stockValues.put(StockEntry.COLUMN_COMPANY_NAME, "Yahoo! Inc.");
            stockValues.put(StockEntry.COLUMN_EXCH_DISP, "NASDAQ");
            stockValues.put(StockEntry.COLUMN_TYPE_DISP, "Equity");
            getActivity().getContentResolver()
                    .insert(StockEntry.CONTENT_URI, stockValues);
            stockInfos = new StockInfo[]{
                    new StockInfo(new String[]{"SFTBY", "Softbank Corp.", "OTCMKS", "Equity"}),
                    new StockInfo(new String[]{"YHOO", "Yahoo! Inc.", "NASDAQ", "Equity"})
            };
            mFavorListAdapter.add(stockInfos[0]);
            mFavorListAdapter.add(stockInfos[1]);
        }
        stockCursor.close();
    }

    /**
     * Customer Adapter
     */
    public class FavorListAdapter extends ArrayAdapter<StockInfo> {
        private Context mContext;
        private ArrayList<StockInfo> favor_list;
        /**
         * Constructor
         * @param context            The current context.
         * @param resource           The resource ID for a layout file containing a layout to use when
         *                           instantiating views.
         * @param list               The objects to represent in the ListView.
         */

        public FavorListAdapter(Context context, int resource, ArrayList<StockInfo> list) {
            super(context, resource, list);
            mContext = context;
            favor_list = list;
        }


        private class ViewHolder {
            TextView symbol;
            TextView company;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.list_item_favor, parent, false);

                holder = new ViewHolder();
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.symbol = (TextView) convertView.findViewById(R.id.list_item_symbol);
            holder.company = (TextView) convertView.findViewById(R.id.list_item_company);
            holder.symbol.setText(favor_list.get(position).getTicker());
            holder.company.setText(favor_list.get(position).getCompany_name());
            return convertView;
        }
    }
}
