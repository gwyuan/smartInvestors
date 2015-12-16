package harristech.smartinvestors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;

import harristech.smartinvestors.data.FinancialContract;


/**
 * Created by henry on 11/14/14.
 */
public class SearchableActivity extends Activity implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    private ListView mListView;
    private SearchView searchView;
    private String requestJsonStr = null;
    private SearchListAdapter mSearchAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = (SearchView) findViewById(R.id.search);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        mListView = (ListView) findViewById(R.id.list_search);

        mSearchAdapter = new SearchListAdapter(
                // Current context (this activity)
                this,
                // ID of list item layout
                R.layout.search_item,
                // ID of the textview to populate
                R.id.search_text,
                // Forecast data
                new ArrayList<StockInfo>());

        // Get a reference to the ListView, and attach this adapter to it
        mListView.setAdapter(mSearchAdapter);

    }

    public class DoMySearch extends AsyncTask<String, Void, ArrayList<StockInfo>> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        protected ArrayList<StockInfo> doInBackground(String... requestTerm) {

            final String LOG_TAG = Connection.class.getSimpleName();
            try {
                final String urlBase = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=";
                String builtUri = urlBase + requestTerm[0]
                        + "&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
                URL url = new URL(builtUri);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    requestJsonStr = null;
                }
                requestJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
                requestJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
            }

            try {
                return getStrFromJson(requestJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<StockInfo> result) {
            if(result != null) {
                mSearchAdapter.clear();
                for (StockInfo oneComInfo : result) {
                    mSearchAdapter.add(oneComInfo);
                }
            }
        }
    }

    private ArrayList<StockInfo> getStrFromJson(String str) throws JSONException {
        if (str == null) {
            Log.d("JSON parse error" ,"JSON string null");
            return null;
        }
        int j = 0;
        while (str.charAt(j) != '{') {
            ++j;
        }
        String jsonStr = str.substring(j);

        JSONObject stickerJson = new JSONObject(jsonStr);
        JSONArray resultArr = stickerJson.getJSONObject("ResultSet").getJSONArray("Result");

        ArrayList<StockInfo> stockInfos = new ArrayList<>();
        for (int i = 0; i < resultArr.length(); i++) {
            String symbol = resultArr.getJSONObject(i).getString("symbol");
            String name = resultArr.getJSONObject(i).getString("name");
            String exchDisp = resultArr.getJSONObject(i).getString("exchDisp");
            String typeDisp = resultArr.getJSONObject(i).getString("typeDisp");
            StockInfo stockInfo = new StockInfo(symbol, name, exchDisp, typeDisp);
            stockInfos.add(stockInfo);
        }

        return stockInfos;
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        DoMySearch doMySearch = new DoMySearch();
        doMySearch.execute(query);

        return true;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        DoMySearch doMySearch = new DoMySearch();
        doMySearch.execute(newText);

        return true;
    }

    /**
     * The user is attempting to close the SearchView.
     *
     * @return true if the listener wants to override the default behavior of clearing the
     * text field and dismissing it, false otherwise.
     */
    @Override
    public boolean onClose() {
        return false;
    }

    /**
     * Customer Adapter
     */
    public class SearchListAdapter extends ArrayAdapter<StockInfo> {
        private ArrayList<StockInfo> search_list;
        private Context mContext;
        /**
         * Constructor
         * @param context            The current context.
         * @param resource           The resource ID for a layout file containing a layout to use when
         *                           instantiating views.
         * @param textViewResourceId The id of the TextView within the layout resource to be populated
         * @param list               The objects to represent in the ListView.
         */
        public SearchListAdapter(Context context, int resource, int textViewResourceId,
                                 ArrayList<StockInfo> list) {
            super(context, resource, textViewResourceId, list);
            mContext = context;
            search_list = list;
        }

        private class ViewHolder {
            TextView stockInfo;
            CheckBox autoSave;
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         * @param convertView
         * @param parent
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final String selection = FinancialContract.StockEntry.COLUMN_STOCK_TICKER + " = ?";
            final String[] selectionArgs = {search_list.get(position).getTicker()};

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.search_item, null);

                holder = new ViewHolder();
                holder.stockInfo = (TextView) convertView.findViewById(R.id.search_text);
                holder.autoSave = (CheckBox) convertView.findViewById(R.id.search_chk);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.autoSave.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    if (holder.autoSave.isChecked()) {
                        // Do the RequestData task and add the data into DB
                        StockInfo info = search_list.get(position);
                        // TODO: New a request data task and insert it into DB
                        RequestFinancialData requestFinancialData =
                                new RequestFinancialData(getContext(), info);
                        requestFinancialData.addStock(info);

                        Toast.makeText(getApplicationContext(),
                                "Added " + info.getTicker(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // TODO: Remove the data from DB
                        StockInfo info = search_list.get(position);
                        mContext.getContentResolver().delete(
                                FinancialContract.StockEntry.CONTENT_URI,  // Uri to Query
                                selection, // selection
                                selectionArgs // selectionArgs
                        );

                        Toast.makeText(getApplicationContext(),
                                "Unfollow " + info.getTicker(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.stockInfo.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    StockInfo info = search_list.get(position);
                    Toast.makeText(getApplicationContext(),
                            "Retrieving financial data of "
                                    + info.getTicker(),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, FinancialActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, new String[]{
                                    info.getTicker(),
                                    info.getCompany_name(),
                                    info.getExchange_market(),
                                    info.getStock_type()
                            });
                    startActivity(intent);
                }
            });

            // Check with the database to set the checkbox
            Cursor stockCursor = mContext.getContentResolver().query(
                    FinancialContract.StockEntry.CONTENT_URI,  // Uri to Query
                    null, // columns (projection)
                    selection, // selection
                    selectionArgs, // selectionArgs
                    null // sort order
            );
            if (stockCursor.moveToFirst()) {
                holder.autoSave.setChecked(true);
                stockCursor.close();
            } else {
                holder.autoSave.setChecked(false);
                stockCursor.close();
            }
            holder.stockInfo.setText(search_list.get(position).getStockInfo());

            return convertView;
        }

    }

}


