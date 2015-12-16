package harristech.smartinvestors;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import harristech.smartinvestors.data.FinancialContract.IncomeEntry;
import harristech.smartinvestors.data.FinancialContract.StockEntry;
import harristech.smartinvestors.data.FinancialDbHelper;

/**
 * Created by henry on 12/5/14.
 */
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable{
        mContext.deleteDatabase(FinancialDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new FinancialDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        FinancialDbHelper dbHelper = new FinancialDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Create a new table of values
        ContentValues stockInfo = createStockInfo();
        long stockRowId = db.insert(StockEntry.TABLE_NAME, null, stockInfo);
        // Verify we got a row back
        assertTrue(stockRowId != -1);
        // A cursor is your primary interface to the query results.
        Cursor stockCursor = db.query(
                StockEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        validateCursor(stockCursor, stockInfo);

        ContentValues incomeValues = createIncomeValues(stockRowId);
        long incomeRowId = db.insert(IncomeEntry.TABLE_NAME, null, incomeValues);
        assertTrue(incomeRowId != -1);
        // A cursor is your primary interface to the query results.
        Cursor incomeCursor = db.query(
                IncomeEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        validateCursor(incomeCursor, incomeValues);

        dbHelper.close();
    }

    static ContentValues createStockInfo() {
        // Test data
        String testCompanyName = "Google";
        String testStockTicker = "GOOGL";
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

        String testQuarter = "201409";
        String testUnit = "thousands";
        String testTotalRevenue = "16,523,000";

        values.put(IncomeEntry.COLUMN_STOCK_KEY, stockRowId);
        values.put(IncomeEntry.COLUMN_QUARTER_TEXT, testQuarter);
        values.put(IncomeEntry.COLUMN_UNIT, testUnit);
        values.put(IncomeEntry.COLUMN_TOTAL_REVENUE, testTotalRevenue);

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
