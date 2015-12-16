package harristech.smartinvestors;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import harristech.smartinvestors.data.FinancialContract.FinancialEntry;
import harristech.smartinvestors.data.FinancialContract.BalanceEntry;
import harristech.smartinvestors.data.FinancialContract.CashFlowEntry;
import harristech.smartinvestors.data.FinancialContract.IncomeEntry;
import harristech.smartinvestors.data.FinancialContract.StockEntry;
import harristech.smartinvestors.data.FinancialDbHelper;

/**
 * Created by henry on 12/5/14.
 */
public class TestProvider extends AndroidTestCase {
    private static final String LOG_TAG = TestProvider.class.getSimpleName();
    private static final String TEST_STICKER = "GOOGL";
    private static final String TEST_QUARTER = "201409";

    public void deleteAllRecords(){
        mContext.getContentResolver().delete(BalanceEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(CashFlowEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(FinancialEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(StockEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(IncomeEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(
                BalanceEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                CashFlowEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                FinancialEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
        cursor = mContext.getContentResolver().query(
                IncomeEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {

        FinancialDbHelper dbHelper = new FinancialDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Create a new table of values
        ContentValues stockInfo = createStockInfo();
        Uri insertStockUri = mContext.getContentResolver().insert(StockEntry.CONTENT_URI, stockInfo);
        long stockRowId = ContentUris.parseId(insertStockUri);
        // Verify we got a row back
        assertTrue(stockRowId != -1);
        // A cursor is your primary interface to the query results.
        Cursor stockCursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,  // Uri to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        validateCursor(stockCursor, stockInfo);

        ContentValues incomeValues = createIncomeValues(stockRowId);
        Uri insertIncomeUri = mContext.getContentResolver()
                .insert(IncomeEntry.CONTENT_URI, incomeValues);
        assertTrue(insertIncomeUri != null);

        // A cursor is your primary interface to the query results.
        Cursor incomeCursor = mContext.getContentResolver().query(
                IncomeEntry.CONTENT_URI,  // Uri to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor(incomeCursor, incomeValues);
        incomeCursor.close();

        incomeCursor = mContext.getContentResolver().query(
                IncomeEntry.buildIncomeStockWithQuarter(TEST_STICKER, TEST_QUARTER),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor(incomeCursor, incomeValues);
        incomeCursor.close();

        ContentValues balanceValues = createBalanceValues(stockRowId);
        long balanceRowId = db.insert(BalanceEntry.TABLE_NAME, null, balanceValues);
        assertTrue(balanceRowId != -1);
        // A cursor is your primary interface to the query results.
        Cursor balanceCursor = mContext.getContentResolver().query(
                BalanceEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor(balanceCursor, balanceValues);

        ContentValues cashValues = createCashValues(stockRowId);
        long cashRowId = db.insert(CashFlowEntry.TABLE_NAME, null, cashValues);
        assertTrue(cashRowId != -1);
        // A cursor is your primary interface to the query results.
        Cursor cashCursor = mContext.getContentResolver().query(
                CashFlowEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor(cashCursor, cashValues);

        dbHelper.close();
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(IncomeEntry.CONTENT_URI);
        assertEquals(IncomeEntry.CONTENT_TYPE, type);

        String testStock = TEST_STICKER;
        type = mContext.getContentResolver().getType(IncomeEntry.buildIncomeStock(testStock));
        assertEquals(IncomeEntry.CONTENT_TYPE, type);

        String testQuarter = TEST_QUARTER;
        type = mContext.getContentResolver()
                .getType(IncomeEntry.buildIncomeStockWithQuarter(testStock, testQuarter));
        assertEquals(IncomeEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(StockEntry.CONTENT_URI);
        assertEquals(StockEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(StockEntry.buildStockUri(1L));
        assertEquals(StockEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testUpdateStock() {
        deleteAllRecords();

        // Create a new map of values, where column names are the keys
        ContentValues values = createStockInfo();
        Uri stockUri = mContext.getContentResolver().insert(StockEntry.CONTENT_URI, values);
        long stockRowId = ContentUris.parseId(stockUri);
        // Verify we got a row back
        assertTrue(stockRowId != -1);
        Log.d(LOG_TAG, "New row id: " + stockRowId);

        ContentValues values1 = new ContentValues(values);
        values1.put(StockEntry._ID, stockRowId);
        values1.put(StockEntry.COLUMN_STOCK_TICKER, "GOOG");
        mContext.getContentResolver().update(
                StockEntry.CONTENT_URI,
                values1,
                null,
                null
        );

        Cursor stockCursor = mContext.getContentResolver().query(
                StockEntry.buildStockUri(stockRowId),  // Uri to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        validateCursor(stockCursor, values1);
    }

    static ContentValues createStockInfo() {
        // Test data
        String testCompanyName = "Google";
        String testStockTicker = TEST_STICKER;
        String testExchDisp = "NASDAQ";
        String testTypeDisp = "Equity";

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_COMPANY_NAME, testCompanyName);
        values.put(StockEntry.COLUMN_STOCK_TICKER, testStockTicker);
        values.put(StockEntry.COLUMN_EXCH_DISP, testExchDisp);
        values.put(StockEntry.COLUMN_TYPE_DISP, testTypeDisp);

        return values;
    }

    static ContentValues createIncomeValues(long stockRowId) {
        ContentValues values = new ContentValues();

        String testQuarter = TEST_QUARTER;
        String testUnit = "thousands";
        String testTotalRevenue = "16,523,000";

        values.put(IncomeEntry.COLUMN_STOCK_KEY, stockRowId);
        values.put(IncomeEntry.COLUMN_QUARTER_TEXT, testQuarter);
        values.put(IncomeEntry.COLUMN_UNIT, testUnit);
        values.put(IncomeEntry.COLUMN_TOTAL_REVENUE, testTotalRevenue);

        return values;
    }

    static ContentValues createBalanceValues(long stockRowId) {
        ContentValues values = new ContentValues();

        String testQuarter = TEST_QUARTER;
        String testUnit = "thousands";
        String testTotalAssets = "125,781,000";

        values.put(BalanceEntry.COLUMN_STOCK_KEY, stockRowId);
        values.put(BalanceEntry.COLUMN_QUARTER_TEXT, testQuarter);
        values.put(BalanceEntry.COLUMN_UNIT, testUnit);
        values.put(BalanceEntry.COLUMN_TOTAL_ASSETS, testTotalAssets);

        return values;
    }

    static ContentValues createCashValues(long stockRowId) {
        ContentValues values = new ContentValues();

        String testQuarter = TEST_QUARTER;
        String testUnit = "thousands";
        String testNetIncome = "2,813,000";

        values.put(CashFlowEntry.COLUMN_STOCK_KEY, stockRowId);
        values.put(CashFlowEntry.COLUMN_QUARTER_TEXT, testQuarter);
        values.put(CashFlowEntry.COLUMN_UNIT, testUnit);
        values.put(CashFlowEntry.COLUMN_NET_INCOME, testNetIncome);

        return values;
    }

    static void validateCursor(Cursor valueCursor, ContentValues values) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = values.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertTrue(idx != -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
