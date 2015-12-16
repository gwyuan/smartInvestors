package harristech.smartinvestors.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import harristech.smartinvestors.data.FinancialContract.BalanceEntry;
import harristech.smartinvestors.data.FinancialContract.CashFlowEntry;
import harristech.smartinvestors.data.FinancialContract.FinancialEntry;
import harristech.smartinvestors.data.FinancialContract.IncomeEntry;
import harristech.smartinvestors.data.FinancialContract.StockEntry;


/**
 * Created by henry on 12/5/14.
 */
public class FinancialProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FinancialDbHelper mOpenHelper;

    private static final int STOCK = 100;
    private static final int STOCK_ID = 101;
    private static final int INCOME = 200;
    private static final int INCOME_WITH_STOCK = 201;
    private static final int INCOME_WITH_STOCK_AND_QUARTER = 202;
    private static final int BALANCE = 300;
    private static final int BALANCE_WITH_STOCK = 301;
    private static final int BALANCE_WITH_STOCK_AND_QUARTER = 302;
    private static final int CASHFLOW = 400;
    private static final int CASHFLOW_WITH_STOCK = 401;
    private static final int CASHFLOW_WITH_STOCK_AND_QUARTER = 402;
    private static final int FINANCIAL = 500;
    private static final int FINANCIAL_WITH_STOCK = 501;
    private static final int FINANCIAL_WITH_STOCK_AND_DATE = 502;

    private static final SQLiteQueryBuilder sIncomeByStockTickerQueryBuilder;
    private static final SQLiteQueryBuilder sBalanceByStockTickerQueryBuilder;
    private static final SQLiteQueryBuilder sCashFlowByStockTickerQueryBuilder;
    private static final SQLiteQueryBuilder sFinancialByStockTickerQueryBuilder;

    static {
        sIncomeByStockTickerQueryBuilder = new SQLiteQueryBuilder();
        sBalanceByStockTickerQueryBuilder = new SQLiteQueryBuilder();
        sCashFlowByStockTickerQueryBuilder = new SQLiteQueryBuilder();
        sFinancialByStockTickerQueryBuilder = new SQLiteQueryBuilder();
        sIncomeByStockTickerQueryBuilder.setTables(
                StockEntry.TABLE_NAME + " INNER JOIN " +
                        IncomeEntry.TABLE_NAME +
                        " ON " + IncomeEntry.TABLE_NAME +
                        "." + IncomeEntry.COLUMN_STOCK_KEY +
                        " = " + StockEntry.TABLE_NAME +
                        "." + StockEntry._ID
        );
        sBalanceByStockTickerQueryBuilder.setTables(
                StockEntry.TABLE_NAME + " INNER JOIN " +
                        BalanceEntry.TABLE_NAME +
                        " ON " + BalanceEntry.TABLE_NAME +
                        "." + BalanceEntry.COLUMN_STOCK_KEY +
                        " = " + StockEntry.TABLE_NAME +
                        "." + StockEntry._ID
        );
        sCashFlowByStockTickerQueryBuilder.setTables(
                StockEntry.TABLE_NAME + " INNER JOIN " +
                        CashFlowEntry.TABLE_NAME +
                        " ON " + CashFlowEntry.TABLE_NAME +
                        "." + CashFlowEntry.COLUMN_STOCK_KEY +
                        " = " + StockEntry.TABLE_NAME +
                        "." + StockEntry._ID
        );
        sFinancialByStockTickerQueryBuilder.setTables(
                StockEntry.TABLE_NAME + " INNER JOIN " +
                        FinancialEntry.TABLE_NAME +
                        " ON " + FinancialEntry.TABLE_NAME +
                        "." + FinancialEntry.COLUMN_STOCK_KEY +
                        " = " + StockEntry.TABLE_NAME +
                        "." + StockEntry._ID
        );
    }

    private static final String sStockSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? ";
    private static final String sStockIncomeWithStartQuarterSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    IncomeEntry.COLUMN_QUARTER_TEXT + " >= ? ";
    private static final String sStockIncomeAndQuarterSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    IncomeEntry.COLUMN_QUARTER_TEXT + " = ? ";
    private static final String sStockBalanceWithStartQuarterSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    BalanceEntry.COLUMN_QUARTER_TEXT + " >= ? ";
    private static final String sStockBalanceAndQuarterSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    BalanceEntry.COLUMN_QUARTER_TEXT + " = ? ";
    private static final String sStockCashFlowWithStartQuarterSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    CashFlowEntry.COLUMN_QUARTER_TEXT + " >= ? ";
    private static final String sStockCashFlowAndQuarterSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    CashFlowEntry.COLUMN_QUARTER_TEXT + " = ? ";
    private static final String sStockFinancialWithStartDateSelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    FinancialEntry.COLUMN_DATE_TEXT + " >= ? ";
    private static final String sStockFinancialAndDaySelection =
            StockEntry.TABLE_NAME + "." +
                    StockEntry.COLUMN_STOCK_TICKER + " = ? AND " +
                    FinancialEntry.COLUMN_DATE_TEXT + " = ? ";

    private Cursor getBalanceByStockTicker(Uri uri, String[] projection, String sortOrder) {
        String stockTicker = BalanceEntry.getStockTickerFromUri(uri);
        String startQuarter = BalanceEntry.getStartQuarterFromUri(uri);
        String selection;
        String[] selectionArgs;

        if (startQuarter == null) {
            selection = sStockSelection;
            selectionArgs = new String[]{stockTicker};
        } else {
            selection = sStockBalanceWithStartQuarterSelection;
            selectionArgs = new String[]{stockTicker, startQuarter};
        }

        return sBalanceByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getBalanceByStockTickerAndQuarter(
            Uri uri, String[] projection, String sortOrder) {
        String stockTicker = BalanceEntry.getStockTickerFromUri(uri);
        String quarter = BalanceEntry.getQuarterFromUri(uri);
        String selection = sStockBalanceAndQuarterSelection;
        String[] selectionArgs = new String[]{stockTicker, quarter};

        return sBalanceByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCashFlowByStockTicker(Uri uri, String[] projection, String sortOrder) {
        String stockTicker = CashFlowEntry.getStockTickerFromUri(uri);
        String startQuarter = CashFlowEntry.getStartQuarterFromUri(uri);
        String selection;
        String[] selectionArgs;

        if (startQuarter == null) {
            selection = sStockSelection;
            selectionArgs = new String[]{stockTicker};
        } else {
            selection = sStockCashFlowWithStartQuarterSelection;
            selectionArgs = new String[]{stockTicker, startQuarter};
        }

        return sCashFlowByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCashFlowByStockTickerAndQuarter(
            Uri uri, String[] projection, String sortOrder) {
        String stockTicker = CashFlowEntry.getStockTickerFromUri(uri);
        String quarter = CashFlowEntry.getQuarterFromUri(uri);
        String selection = sStockCashFlowAndQuarterSelection;
        String[] selectionArgs = new String[]{stockTicker, quarter};

        return sCashFlowByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getFinancialByStockTicker(Uri uri, String[] projection, String sortOrder) {
        String stockTicker = FinancialEntry.getStockTickerFromUri(uri);
        String startDate = FinancialEntry.getStartDateFromUri(uri);
        String selection;
        String[] selectionArgs;

        if (startDate == null) {
            selection = sStockSelection;
            selectionArgs = new String[]{stockTicker};
        } else {
            selection = sStockFinancialWithStartDateSelection;
            selectionArgs = new String[]{stockTicker, startDate};
        }

        return sFinancialByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getFinancialByStockTickerAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String stockTicker = FinancialEntry.getStockTickerFromUri(uri);
        String date = FinancialEntry.getDateFromUri(uri);
        String selection = sStockFinancialAndDaySelection;
        String[] selectionArgs = new String[]{stockTicker, date};

        return sFinancialByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getIncomeByStockTicker(Uri uri, String[] projection, String sortOrder) {
        String stockTicker = IncomeEntry.getStockTickerFromUri(uri);
        String startQuarter = IncomeEntry.getStartQuarterFromUri(uri);
        String selection;
        String[] selectionArgs;

        if (startQuarter == null) {
            selection = sStockSelection;
            selectionArgs = new String[]{stockTicker};
        } else {
            selection = sStockIncomeWithStartQuarterSelection;
            selectionArgs = new String[]{stockTicker, startQuarter};
        }

        return sIncomeByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getIncomeByStockTickerAndQuarter(
            Uri uri, String[] projection, String sortOrder) {
        String stockTicker = IncomeEntry.getStockTickerFromUri(uri);
        String quarter = IncomeEntry.getQuarterFromUri(uri);
        String selection = sStockIncomeAndQuarterSelection;
        String[] selectionArgs = new String[]{stockTicker, quarter};

        return sIncomeByStockTickerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FinancialContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, FinancialContract.PATH_STOCK, STOCK);
        matcher.addURI(authority, FinancialContract.PATH_STOCK + "/#", STOCK_ID);

        matcher.addURI(authority, FinancialContract.PATH_INCOME, INCOME);
        matcher.addURI(authority, FinancialContract.PATH_INCOME + "/*",
                INCOME_WITH_STOCK);
        matcher.addURI(authority, FinancialContract.PATH_INCOME + "/*/*",
                INCOME_WITH_STOCK_AND_QUARTER);

        matcher.addURI(authority, FinancialContract.PATH_BALANCE, BALANCE);
        matcher.addURI(authority, FinancialContract.PATH_BALANCE + "/*",
                BALANCE_WITH_STOCK);
        matcher.addURI(authority, FinancialContract.PATH_BALANCE + "/*/*",
                BALANCE_WITH_STOCK_AND_QUARTER);

        matcher.addURI(authority, FinancialContract.PATH_CASHFLOW, CASHFLOW);
        matcher.addURI(authority, FinancialContract.PATH_CASHFLOW + "/*",
                CASHFLOW_WITH_STOCK);
        matcher.addURI(authority, FinancialContract.PATH_CASHFLOW + "/*/*",
                CASHFLOW_WITH_STOCK_AND_QUARTER);

        matcher.addURI(authority, FinancialContract.PATH_FINANCIAL, FINANCIAL);
        matcher.addURI(authority, FinancialContract.PATH_FINANCIAL + "/*",
                FINANCIAL_WITH_STOCK);
        matcher.addURI(authority, FinancialContract.PATH_FINANCIAL + "/*/*",
                FINANCIAL_WITH_STOCK_AND_DATE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FinancialDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case STOCK_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StockEntry.TABLE_NAME,
                        projection,
                        StockEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case STOCK: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StockEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case INCOME_WITH_STOCK_AND_QUARTER: {
                retCursor = getIncomeByStockTickerAndQuarter(uri, projection, sortOrder);
                break;
            }
            case INCOME_WITH_STOCK: {
                retCursor = getIncomeByStockTicker(uri, projection, sortOrder);
                break;
            }
            case INCOME: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IncomeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case BALANCE_WITH_STOCK_AND_QUARTER: {
                retCursor = getBalanceByStockTickerAndQuarter(uri, projection, sortOrder);
                break;
            }
            case BALANCE_WITH_STOCK: {
                retCursor = getBalanceByStockTicker(uri, projection, sortOrder);
                break;
            }
            case BALANCE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BalanceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CASHFLOW_WITH_STOCK_AND_QUARTER: {
                retCursor = getCashFlowByStockTickerAndQuarter(uri, projection, sortOrder);
                break;
            }
            case CASHFLOW_WITH_STOCK: {
                retCursor = getCashFlowByStockTicker(uri, projection, sortOrder);
                break;
            }
            case CASHFLOW: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CashFlowEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FINANCIAL_WITH_STOCK_AND_DATE: {
                retCursor = getFinancialByStockTickerAndDate(uri, projection, sortOrder);
                break;
            }
            case FINANCIAL_WITH_STOCK: {
                retCursor = getFinancialByStockTicker(uri, projection, sortOrder);
                break;
            }
            case FINANCIAL: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FinancialEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return StockEntry.CONTENT_TYPE;
            case STOCK_ID:
                return StockEntry.CONTENT_ITEM_TYPE;
            case INCOME_WITH_STOCK_AND_QUARTER:
                return IncomeEntry.CONTENT_ITEM_TYPE;
            case INCOME_WITH_STOCK:
                return IncomeEntry.CONTENT_TYPE;
            case INCOME:
                return IncomeEntry.CONTENT_TYPE;
            case BALANCE_WITH_STOCK_AND_QUARTER:
                return BalanceEntry.CONTENT_ITEM_TYPE;
            case BALANCE_WITH_STOCK:
                return BalanceEntry.CONTENT_TYPE;
            case BALANCE:
                return BalanceEntry.CONTENT_TYPE;
            case CASHFLOW_WITH_STOCK_AND_QUARTER:
                return CashFlowEntry.CONTENT_ITEM_TYPE;
            case CASHFLOW_WITH_STOCK:
                return CashFlowEntry.CONTENT_TYPE;
            case CASHFLOW:
                return CashFlowEntry.CONTENT_TYPE;
            case FINANCIAL_WITH_STOCK_AND_DATE:
                return CashFlowEntry.CONTENT_ITEM_TYPE;
            case FINANCIAL_WITH_STOCK:
                return CashFlowEntry.CONTENT_TYPE;
            case FINANCIAL:
                return CashFlowEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case BALANCE: {
                long _id = db.insert(BalanceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BalanceEntry.buildBalanceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CASHFLOW: {
                long _id = db.insert(CashFlowEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CashFlowEntry.buildCashFlowUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case FINANCIAL: {
                long _id = db.insert(FinancialEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = FinancialEntry.buildFinancialUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case INCOME: {
                long _id = db.insert(IncomeEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = IncomeEntry.buildIncomeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STOCK: {
                long _id = db.insert(StockEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = StockEntry.buildStockUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case BALANCE:
                rowsDeleted = db.delete(BalanceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CASHFLOW:
                rowsDeleted = db.delete(CashFlowEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FINANCIAL:
                rowsDeleted = db.delete(FinancialEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INCOME:
                rowsDeleted = db.delete(IncomeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOCK:
                rowsDeleted = db.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case BALANCE:
                rowsUpdated = db.update(BalanceEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CASHFLOW:
                rowsUpdated = db.update(CashFlowEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case FINANCIAL:
                rowsUpdated = db.update(FinancialEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case INCOME:
                rowsUpdated = db.update(IncomeEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case STOCK:
                rowsUpdated = db.update(StockEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case BALANCE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BalanceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case CASHFLOW:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CashFlowEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case FINANCIAL:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(FinancialEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case INCOME:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IncomeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
