package harristech.smartinvestors;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import harristech.smartinvestors.data.FinancialContract;


public class FinancialActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_financial, new FinancialFragment())
                    .commit();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_financial, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();
        if (id == R.id.action_search) {
                startActivity(new Intent(this, SearchableActivity.class));
                return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    // TODO: Complete the detail fragment
    public static class FinancialFragment extends Fragment {
        private final static String LOG_TAG = FinancialFragment.class.getSimpleName();
        private final static String APP_HASH_TAG = "#smartInvestors# ";
        private StockInfo mStockInfo;
        private LinearLayout mLinearLayout;
        private LinearLayout mIncomeGraph;
        private LinearLayout mBalanceGraph;
        private LinearLayout mCashFlowGraph;
        private LinearLayout mFinancialLayout;
        private TextView mTextView;

        public FinancialFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_financial, container, false);
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String[] info = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
                mStockInfo = new StockInfo(info);
                ((TextView) rootView.findViewById(R.id.financial))
                        .setText(mStockInfo.getTicker() + " - " + mStockInfo.getCompany_name());
            }
            final CheckBox save = (CheckBox) rootView.findViewById(R.id.financial_chk);
            final String selection = FinancialContract.StockEntry.COLUMN_STOCK_TICKER + " = ?";
            final String[] selectionArgs = {mStockInfo.getTicker()};
            save.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    if (save.isChecked()) {
                        // TODO: New a request data task and insert it into DB
                        RequestFinancialData requestFinancialData =
                                new RequestFinancialData(getActivity(), mStockInfo);
                        requestFinancialData.addStock(mStockInfo);

                        Toast.makeText(getActivity(),
                                "Added " + mStockInfo.getTicker(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // TODO: Remove the data from DB
                        getActivity().getContentResolver().delete(
                                FinancialContract.StockEntry.CONTENT_URI,  // Uri to Query
                                selection, // selection
                                selectionArgs // selectionArgs
                        );

                        Toast.makeText(getActivity(),
                                "Unfollow " + mStockInfo.getTicker(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // Check with the database to set the checkbox
            Cursor stockCursor = getActivity().getContentResolver().query(
                    FinancialContract.StockEntry.CONTENT_URI,  // Uri to Query
                    null, // columns (projection)
                    selection, // selection
                    selectionArgs, // selectionArgs
                    null // sort order
            );
            if (stockCursor.moveToFirst()) {
                save.setChecked(true);
                stockCursor.close();
            } else {
                save.setChecked(false);
                stockCursor.close();
            }

            mLinearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_financial_linear);
            mIncomeGraph = (LinearLayout) rootView.findViewById(R.id.income);
            mBalanceGraph = (LinearLayout) rootView.findViewById(R.id.balance);
            mCashFlowGraph = (LinearLayout) rootView.findViewById(R.id.cashflow);
            mFinancialLayout = (LinearLayout) rootView.findViewById(R.id.financial_highlight);
            mTextView = (TextView) rootView.findViewById(R.id.no_data);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            fetchFinancial();
        }

        private void fetchFinancial() {
            RequestFinancialData requestFinancialData = new RequestFinancialData(getActivity(),
                    new LinearLayout[] {
                            mLinearLayout,
                            mIncomeGraph,
                            mBalanceGraph,
                            mCashFlowGraph,
                            mFinancialLayout
                    },
                    mTextView
            );
            requestFinancialData.execute(mStockInfo.getTicker());
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu
            inflater.inflate(R.menu.menu_financial, menu);
            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);
            // Get the provider and hold onto it to set/change the share intent
            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            // Attach an intent to this ShareActionProvider.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareCompanyIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null");
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                fetchFinancial();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private Intent createShareCompanyIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            String symbol = mStockInfo.getExchange_market() + ":"
                    + mStockInfo.getTicker();
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    APP_HASH_TAG + symbol);
            return shareIntent;
        }
    }
}
