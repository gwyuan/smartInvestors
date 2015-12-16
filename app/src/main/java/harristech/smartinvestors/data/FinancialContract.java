package harristech.smartinvestors.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by henry on 11/21/14.
 */
public class FinancialContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "harristech.smartinvestors";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible paths
    public static final String PATH_STOCK = "stock";
    public static final String PATH_FINANCIAL = "financial";
    public static final String PATH_INCOME = "income";
    public static final String PATH_BALANCE = "balance";
    public static final String PATH_CASHFLOW = "cashflow";

    // Inner class that defines the table contents of the stock table
    public static final class StockEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOCK).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        // Table name
        public static final String TABLE_NAME = "stock";

        // The stock ticker string is what will be sent to Quandl
        // as the stock query.
        public static final String COLUMN_STOCK_TICKER = "stock_ticker";

        // Human readable stock string (company name), provided by API.
        public static final String COLUMN_COMPANY_NAME = "company_name";

        // Stock exchange market and type as returned by yahoo
        public static final String COLUMN_EXCH_DISP = "exch_disp";
        public static final String COLUMN_TYPE_DISP = "type_disp";

        public static Uri buildStockUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // Inner class that defines the table contents of the stock's financial table
    public static final class FinancialEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FINANCIAL).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_FINANCIAL;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_FINANCIAL;

        public static final String TABLE_NAME = "financial";

        // Column with the foreign key into the stock table.
        public static final String COLUMN_STOCK_KEY = "stock_id";
        // Date, stored as Text with format yyyy-mm-dd
        public static final String COLUMN_DATE_TEXT = "date";
        // TODO: Financial data column insert at here
        public static final String COLUMNN_PE = "pe";

        public static Uri buildFinancialUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFinancialStock(String stockTicker) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).build();
        }

        public static Uri buildFinancialStockWithStartDate(
                String stockTicker, String startDate) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker)
                    .appendQueryParameter(COLUMN_DATE_TEXT, startDate).build();
        }

        public static Uri buildFinancialStockWithDate(String stockTicker, String date) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).appendPath(date).build();
        }

        public static String getStockTickerFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATE_TEXT);
        }
    }

    // Income Statement
    public static final class IncomeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INCOME).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_INCOME;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_INCOME;

        public static final String TABLE_NAME = "income_statement";

        // Column with the foreign key into the stock table.
        public static final String COLUMN_STOCK_KEY = "stock_id";
        // Date, stored as Text with format yyyy-MM
        public static final String COLUMN_QUARTER_TEXT = "quarter";
        // Unit of statement
        public static final String COLUMN_UNIT = "unit";
        // TODO: Income statement column insert at here
        public static final String COLUMN_TOTAL_REVENUE = "totalrevenue";

        public static Uri buildIncomeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildIncomeStock(String stockTicker) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).build();
        }

        public static Uri buildIncomeStockWithStartQuarter(
                String stockTicker, String startQuarter) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker)
                    .appendQueryParameter(COLUMN_QUARTER_TEXT, startQuarter).build();
        }

        public static Uri buildIncomeStockWithQuarter(String stockTicker, String quarter) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).appendPath(quarter).build();
        }

        public static String getStockTickerFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getQuarterFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartQuarterFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_QUARTER_TEXT);
        }
    }

    // Balance Sheet
    public static final class BalanceEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BALANCE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BALANCE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BALANCE;

        public static final String TABLE_NAME = "balance_sheet";

        // Column with the foreign key into the stock table.
        public static final String COLUMN_STOCK_KEY = "stock_id";
        // Date, stored as Text with format yyyy-MM
        public static final String COLUMN_QUARTER_TEXT = "quarter";
        // Unit of statement
        public static final String COLUMN_UNIT = "unit";
        // TODO: Balance sheet column insert at here
        public static final String COLUMN_TOTAL_ASSETS = "totalassets";

        public static Uri buildBalanceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildBalanceStock(String stockTicker) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).build();
        }

        public static Uri buildBalanceStockWithStartQuarter(
                String stockTicker, String startQuarter) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker)
                    .appendQueryParameter(COLUMN_QUARTER_TEXT, startQuarter).build();
        }

        public static Uri buildBalanceStockWithQuarter(String stockTicker, String quarter) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).appendPath(quarter).build();
        }

        public static String getStockTickerFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getQuarterFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartQuarterFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_QUARTER_TEXT);
        }
    }

    // Cash Flow
    public static final class CashFlowEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CASHFLOW).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CASHFLOW;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CASHFLOW;

        public static final String TABLE_NAME = "cash_flow";

        // Column with the foreign key into the stock table.
        public static final String COLUMN_STOCK_KEY = "stock_id";
        // Date, stored as Text with format yyyy-MM
        public static final String COLUMN_QUARTER_TEXT = "quarter";
        // Unit of statement
        public static final String COLUMN_UNIT = "unit";
        // TODO: Cash flow column insert at here
        public static final String COLUMN_NET_INCOME = "netincome";

        public static Uri buildCashFlowUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCashFlowStock(String stockTicker) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).build();
        }

        public static Uri buildCashFlowStockWithStartQuarter(
                String stockTicker, String startQuarter) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker)
                    .appendQueryParameter(COLUMN_QUARTER_TEXT, startQuarter).build();
        }

        public static Uri buildCashFlowStockWithQuarter(String stockTicker, String quarter) {
            return CONTENT_URI.buildUpon().appendPath(stockTicker).appendPath(quarter).build();
        }

        public static String getStockTickerFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getQuarterFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartQuarterFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_QUARTER_TEXT);
        }
    }
}
