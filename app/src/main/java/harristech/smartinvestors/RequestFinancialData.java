package harristech.smartinvestors;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import harristech.smartinvestors.data.FinancialContract;
import harristech.smartinvestors.data.FinancialContract.StockEntry;

;

/**
 * Created by henry on 11/30/14.
 */
public class RequestFinancialData extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = RequestFinancialData.class.getSimpleName();
    private final static String requestUriBase = "https://query.yahooapis.com/v1/public/yql?q=" +
            "SELECT%20*%20FROM%20yahoo.finance.";
    private final static String requestSelection = "%20WHERE%20symbol%3D%22";
    private final static String requestUriSuffix = "%22&format=json&diagnostics=true&env=store" +
            "%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

    private final static String urlBase = "http://finance.yahoo.com/q/";

    private final Context mContext;
    private StockInfo info;
    private LinearLayout mLinearLayout;
    private LinearLayout mIncomeLayout;
    private LinearLayout mBalanceLayout;
    private LinearLayout mCashFlowLayout;
    private LinearLayout mFinancialLayout;
    private TextView mTextView;

    public RequestFinancialData(Context context, LinearLayout[] linearLayouts, TextView textView) {
        mContext = context;
        mLinearLayout = linearLayouts[0];
        mIncomeLayout = linearLayouts[1];
        mBalanceLayout = linearLayouts[2];
        mCashFlowLayout = linearLayouts[3];
        mFinancialLayout = linearLayouts[4];
        mTextView = textView;
    }
    public RequestFinancialData(Context context, StockInfo stockInfo) {
        mContext = context;
        info = stockInfo;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        String query = params[0];

        if (mLinearLayout == null) {
            long stockId = addStock(info);
        } else {
            mIncomeLayout.removeAllViewsInLayout();
            mBalanceLayout.removeAllViewsInLayout();
            mCashFlowLayout.removeAllViewsInLayout();
            mFinancialLayout.removeAllViewsInLayout();
            FetchFromYQL fetchKeyStats = new FetchFromYQL(query, "keystats");
            fetchKeyStats.execute();
            FetchFromYQL fetchIncome = new FetchFromYQL(query, "incomestatement");
            fetchIncome.execute();
            FetchFromYQL fetchBalance = new FetchFromYQL(query, "balancesheet");
            fetchBalance.execute();
            FetchFromYQL fetchCashFlow = new FetchFromYQL(query, "cashflow");
            fetchCashFlow.execute();
        }

        return null;
    }

    private class FetchFromYQL extends AsyncTask<String, Void, Map<String, String[]>> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String requestJsonStr = null;
        Map<String, String[]> map = new HashMap<>();
        String query;
        String requestTerm;
        String[] resultTerms;
        String result;


        public FetchFromYQL(String symbol, String term) {
            query = symbol;
            requestTerm = term;
        }

        @Override
        protected Map<String, String[]> doInBackground(String... params) {
            /*
            String requestUri =
                    requestUriBase + requestTerm + requestSelection + query + requestUriSuffix;

            try {
                URL url = new URL(requestUri);
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
                e.printStackTrace();
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
            */
            String request;
            switch (requestTerm)
            {
                case "keystats":
                    request = "ks";
                    jsoupKeyStats(request);
                    break;
                case "incomestatement":
                    request = "is";
                    jsoupParse(request);
                    break;
                case "balancesheet":
                    request = "bs";
                    jsoupParse(request);
                    break;
                case "cashflow":
                    request = "cf";
                    jsoupParse(request);
                    break;
                default:
                    request = "";
                    break;
            }

            return map;

        }

        @Override
        protected void onPostExecute(Map<String, String[]> stringMap) {
            if (stringMap == null) {
                return;
            }
            if (stringMap.size() != 0) {
                switch (requestTerm) {
                    case "keystats":
                        TextView heading = new TextView(mContext);
                        heading.setText("Financial Highlights");
                        mFinancialLayout.addView(heading);

                        for (int idx = 0; idx < FinancialData.KEY_STATS.length; ++idx) {
                            String key = FinancialData.KEY_STATS[idx];
                            if (stringMap.containsKey(key)) {
                                String val = stringMap.get(key)[0];
                                String term = stringMap.get(key)[1];
                                String key_disp = FinancialData.KEY_STATS_DISP[idx];
                                if (term != null)
                                    key_disp = key_disp + " (" + term + ")";
                                //String value = parseVal(val);

                                TextView termText = new TextView(mContext);
                                TableRow.LayoutParams layoutParams = new LayoutParams(
                                        0, LayoutParams.WRAP_CONTENT, 0.75f);
                                termText.setLayoutParams(layoutParams);
                                termText.setText(key_disp);
                                TextView valueText = new TextView(mContext);
                                layoutParams = new LayoutParams(
                                        0, LayoutParams.WRAP_CONTENT, 0.25f);
                                valueText.setLayoutParams(layoutParams);
                                valueText.setText(val);
                                TableRow tableRow = new TableRow(mContext);
                                tableRow.addView(termText);
                                tableRow.addView(valueText);
                                mFinancialLayout.addView(tableRow);
                            }
                        }
                        TextView textView = new TextView(mContext);
                        textView.setText("\nmrq = Most Recent Quarter \n" +
                                "ttm = Trailing Twelve Months \n" +
                                "yoy = Year Over Year \n" +
                                "lfy = Last Fiscal Year \n" +
                                "fye = Fiscal Year Ending");
                        mFinancialLayout.addView(textView);
                        break;
                    case "incomestatement":
                        incomeGraphPlot(stringMap);
                        break;
                    case "balancesheet":
                        balanceGraphPlot(stringMap);
                        break;
                    case "cashflow":
                        cashFlowGraphPlot(stringMap);
                        break;
                    default:
                }
            } else {
                switch (requestTerm) {
                    case "keystats":
                        TextView textView = new TextView(mContext);
                        textView.setText("No data");
                        mFinancialLayout.addView(textView);
                        break;
                    default:
                        mTextView.setText("No chart");
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
                        mBalanceLayout.setLayoutParams(layoutParams);
                        mCashFlowLayout.setLayoutParams(layoutParams);
                        mIncomeLayout.setLayoutParams(layoutParams);
                }
            }
        }

        private void jsoupParse(String request)
        {
            String url = urlBase + request + "?s=" + query;
            try {
                Document doc = Jsoup.connect(url).get();
                Elements table = doc.select("table.yfnc_tabledata1");

                Elements trs = table.select("tr");
                Element title = trs.get(1);
                String titleStr = title.child(0).text();
                String[] periodStrs = new String[4];
                int j = 0;
                for (int i = 1; i < title.childNodeSize(); ++i) {
                    periodStrs[4 - i] = title.child(i).text();
                }
                map.put(titleStr, periodStrs);
                for (int i = 2; i < trs.size(); ++i) {
                    j = 0;
                    Element tr = trs.get(i);
                    Elements tds = tr.select("td");
                    if (tds.size() > 4) {
                        String term = "";
                        String[] content = new String[4];
                        for (Element td : tds) {
                            if (td.hasText()) {
                                if (j == 0)
                                    term = td.text();
                                else if (j < 5) {
                                    String item = td.text();
                                    item = item.replace('(', '-');
                                    item = item.replaceAll(",", "");
                                    item = item.replaceAll("\\s+", "");
                                    item = item.replace(")", "");

                                    content[4 - j] = item;
                                }
                                ++j;
                            }
                        }
                        map.put(term, content);
                    }
                }

            }
            catch(Exception ex) {
            }

        }

        private void jsoupKeyStats(String request)
        {
            String url = urlBase + request + "?s=" + query;
            if (request.equals("ks"))
            {
                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements table = doc.select("table.yfnc_datamodoutline1");

                    for (Element row : table.select("tr")) {
                        int count = row.select("td.yfnc_tablehead1").size();
                        for (int i = 0; i < count; ++i ) {
                            Elements heads = row.select("td.yfnc_tablehead1");
                            heads.select("sup").remove();
                            String[] ret = headProc(heads.get(i).text());
                            String head = ret[0];
                            String[] data = new String[2];
                            data[0] = row.select("td.yfnc_tabledata1").get(i).text();
                            data[1] = ret[1];
                            map.put(head, data);
                        }

                    }
                }
                catch (Exception ex)
                {

                }
            }
            else
                return;
        }

        private String[] headProc(String str) {
            String[] ret = new String[2];
            if (str != null) {
                str = str.replaceAll("/", "");
                str = str.replace(")", "");
                str = str.replace(":", "");
                int start = str.indexOf("(");
                if (start != -1) {
                    String head = str.substring(0, start);
                    head = head.replaceAll("\\s+", "");
                    ret[0] = head;
                    ret[1] = str.substring(start+1);
                }
                else {
                    str = str.replaceAll("\\s+", "");
                    ret[0] = str;
                    ret[1] = null;
                }
            }
            return ret;
        }

        private String parseVal(String val) {
            double value;
            try {
                value = Double.parseDouble(val);
            } catch (Exception e) {
                return val;
            }
            if (value > 1000000 || value < -1000000) {
                if (value > 1000000000 || value < -1000000000) {
                    value = value / 1000000000;
                    return (Double.toString(value) + "B");
                } else {
                    value = value / 1000000;
                    return (Double.toString(value) + "M");
                }
            } else {
                if (value > 1000 || value < -1000) {
                    Number number = value;
                    return (number.toString());
                } else {
                    return val;
                }
            }
        }

        private Map<String, String[]> getStrFromJson(String jsonStr) throws JSONException{
            if (jsonStr == null) {
                Log.d("JSON parse error" ,"JSON string null");
                return null;
            }
            switch (requestTerm) {
                case "keystats":
                    result = "stats";
                    resultTerms = FinancialData.KEY_STATS;
                    break;
                case "incomestatement":
                    result = "incomestatement";
                    resultTerms = FinancialData.INCOME;
                    break;
                case "cashflow":
                    result = "cashflow";
                    resultTerms = FinancialData.CASH;
                    break;
                case "balancesheet":
                    result = "balancesheet";
                    resultTerms = FinancialData.BALANCE;
                    break;
                default:
                    return map;
            }

            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.getJSONObject("query").get("results").equals(null)) {
                return map;
            }
            JSONObject results = jsonObject.getJSONObject("query")
                    .getJSONObject("results").getJSONObject(result);
            switch (requestTerm) {
                case "keystats":
                    for (String resultTerm : resultTerms) {
                        if (!results.has(resultTerm)) {
                            return map;
                        } else if (results.getJSONObject(resultTerm).has("content") &&
                                results.getJSONObject(resultTerm).has("term")) {
                            String content = results.getJSONObject(resultTerm).getString("content");
                            String term = results.getJSONObject(resultTerm).getString("term");
                            map.put(resultTerm, new String[]{content, term});
                        }
                    }
                    break;
                case "balancesheet":
                    parseJSON(results);
                    break;
                case "cashflow":
                    parseJSON(results);
                    break;
                case "incomestatement":
                    parseJSON(results);
                    break;
                default:
                    break;
            }

            return map;
        }

        private Map<String, String[]> parseJSON(JSONObject results) throws JSONException {
            if (!results.has("statement")) {
                return map;
            }
            JSONArray array = results.getJSONArray("statement");
            String[] dateLabels = new String[array.length()];
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            for (int idx = 0; idx < array.length(); ++idx) {
                String dateInStr = array.getJSONObject(idx).getString("period");
                try {
                    Date date = dateFormat.parse(dateInStr);
                    dateLabels[array.length() - idx - 1] = df.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            map.put("period", dateLabels);
            for (String resultTerm : resultTerms) {
                String[] content = new String[array.length()];
                for (int idx = 0; idx < array.length(); ++idx) {
                    content[array.length() - idx - 1] = array.getJSONObject(idx)
                            .getJSONObject(resultTerm).getString("content");
                }
                map.put(resultTerm, content);
            }
            return map;
        }

        public Map<String, String[]> getData() {
            return map;
        }
    }


    public long addStock(StockInfo stockInfo) {
        String stockTicker = stockInfo.getTicker();
        String companyName = stockInfo.getCompany_name();
        String exchMarket = stockInfo.getExchange_market();
        String stockType = stockInfo.getStock_type();

        // First, check if the stock with this company name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                new String[]{FinancialContract.StockEntry._ID},
                StockEntry.COLUMN_STOCK_TICKER + " = ?",
                new String[]{stockTicker},
                null
        );

        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found it in the database!");
            int stockIdIndex = cursor.getColumnIndex(StockEntry._ID);
            return cursor.getLong(stockIdIndex);
        } else {
            Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
            ContentValues stockValues = new ContentValues();
            stockValues.put(StockEntry.COLUMN_STOCK_TICKER, stockTicker);
            stockValues.put(StockEntry.COLUMN_COMPANY_NAME, companyName);
            stockValues.put(StockEntry.COLUMN_EXCH_DISP, exchMarket);
            stockValues.put(StockEntry.COLUMN_TYPE_DISP, stockType);

            Uri stockInsertUri = mContext.getContentResolver()
                    .insert(StockEntry.CONTENT_URI, stockValues);

            return ContentUris.parseId(stockInsertUri);
        }
    }

    private long[] getValue(String[] list) {
        if (list == null) {
            long[] zero = {0, 0, 0, 0};
            return zero;
        }
        long[] data = new long[list.length];
        int idx = 0;
        for (String item : list) {
            if (!item.equals("-")) {
                data[idx] = Long.parseLong(item)/1000;
            } else {
                data[idx] = 0;
            }
            ++idx;
        }
        return  data;
    }

    private long[] findM(long[][] values) {
        long max = values[0][0];
        long min = values[0][0];
        for (long[] set : values) {
            for (long value : set) {
                max = Math.max(value, max);
                min = Math.min(value, min);
            }
        }
        return new long[]{min, max};
    }

    private void incomeGraphPlot(Map<String, String[]> stringListMap) {
        String[] dateLabels = stringListMap.get("Period Ending");
        String[] totalRevenueList = stringListMap.get(FinancialData.INCOME_STATEMENT[0]);
        long[] totalRevenue = getValue(totalRevenueList);
        // init series data
        GraphViewSeries totalRevenueSeries = new GraphViewSeries(
                "Total Revenue",
                new GraphViewSeriesStyle(Color.rgb(250, 0, 30), 3),
                new GraphViewData[] {
                        new GraphViewData(0, totalRevenue[0]),
                        new GraphViewData(1, totalRevenue[1]),
                        new GraphViewData(2, totalRevenue[2]),
                        new GraphViewData(3, totalRevenue[3])
                });
        String[] operatingIncomeList = stringListMap.get(FinancialData.INCOME_STATEMENT[8]);
        long[] operatingIncome = getValue(operatingIncomeList);
        // init series data
        GraphViewSeries operatingIncomeSeries = new GraphViewSeries(
                "Operating Income",
                new GraphViewSeriesStyle(Color.rgb(0, 0, 250), 3),
                new GraphViewData[] {
                        new GraphViewData(0, operatingIncome[0]),
                        new GraphViewData(1, operatingIncome[1]),
                        new GraphViewData(2, operatingIncome[2]),
                        new GraphViewData(3, operatingIncome[3])
                });
        String[] netIncomeList = stringListMap.get(FinancialData.INCOME_STATEMENT[20]);
        long[] netIncome = getValue(netIncomeList);
        // init series data
        GraphViewSeries netIncomeSeries = new GraphViewSeries(
                "Net Income",
                new GraphViewSeriesStyle(Color.rgb(50, 250, 90), 3),
                new GraphViewData[] {
                        new GraphViewData(0, netIncome[0]),
                        new GraphViewData(1, netIncome[1]),
                        new GraphViewData(2, netIncome[2]),
                        new GraphViewData(3, netIncome[3])
                });
        GraphView mIncomeGraphView = new LineGraphView(
                mContext, // context
                "Income Statement\t(in millions)" // heading
        );
        mIncomeGraphView.addSeries(totalRevenueSeries);
        mIncomeGraphView.addSeries(operatingIncomeSeries);
        mIncomeGraphView.addSeries(netIncomeSeries); // data
        mIncomeGraphView.setHorizontalLabels(dateLabels);
        long min = findM(new long[][] {netIncome, totalRevenue, operatingIncome})[0];
        long max = findM(new long[][] {netIncome, totalRevenue, operatingIncome})[1];
        mIncomeGraphView.setManualYAxisBounds(max*1.2, ((min > 0) ? 0 : min-max*0.2));
        mIncomeGraphView.setShowLegend(true);
        mIncomeGraphView.setLegendWidth(300);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                mIncomeLayout.getWidth(), mIncomeLayout.getWidth()-70);
        mIncomeLayout.setLayoutParams(layoutParams);
        mIncomeLayout.addView(mIncomeGraphView);
    }

    private void balanceGraphPlot(Map<String, String[]> stringListMap) {
        String[] dateLabels = stringListMap.get("Period Ending");
        String[] totalAssetsList = stringListMap.get(FinancialData.BALANCE_SHEET[13]);
        long[] totalAssets = getValue(totalAssetsList);
        // init series data
        GraphViewSeries totalAssetsSeries = new GraphViewSeries(
                "Total Assets",
                new GraphViewSeriesStyle(Color.rgb(250, 0, 30), 3),
                new GraphViewData[] {
                        new GraphViewData(0, totalAssets[0]),
                        new GraphViewData(1, totalAssets[1]),
                        new GraphViewData(2, totalAssets[2]),
                        new GraphViewData(3, totalAssets[3])
                });
        String[] totalLiabilitiesList = stringListMap.get(FinancialData.BALANCE_SHEET[23]);
        long[] totalLiabilities = getValue(totalLiabilitiesList);
        // init series data
        GraphViewSeries totalLiabilitiesSeries = new GraphViewSeries(
                "Total Liabilities",
                new GraphViewSeriesStyle(Color.rgb(0, 0, 250), 3),
                new GraphViewData[] {
                        new GraphViewData(0, totalLiabilities[0]),
                        new GraphViewData(1, totalLiabilities[1]),
                        new GraphViewData(2, totalLiabilities[2]),
                        new GraphViewData(3, totalLiabilities[3])
                });
        GraphView mBalanceGraphView = new LineGraphView(
                mContext, // context
                "Balance Sheet\t(in millions)" // heading
        );
        mBalanceGraphView.addSeries(totalAssetsSeries);
        mBalanceGraphView.addSeries(totalLiabilitiesSeries);
        mBalanceGraphView.setHorizontalLabels(dateLabels);
        long min = findM(new long[][] {totalAssets, totalLiabilities})[0];
        long max = findM(new long[][] {totalAssets, totalLiabilities})[1];
        mBalanceGraphView.setManualYAxisBounds(max*1.2, ((min > 0) ? 0 : min-max*0.2));
        mBalanceGraphView.setShowLegend(true);
        mBalanceGraphView.setLegendWidth(300);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                mBalanceLayout.getWidth(), mBalanceLayout.getWidth()-70);
        mBalanceLayout.setLayoutParams(layoutParams);
        mBalanceLayout.addView(mBalanceGraphView);
    }

    private void cashFlowGraphPlot(Map<String, String[]> stringListMap) {
        String[] dateLabels = stringListMap.get("Period Ending");
        String[] operatingList = stringListMap.get(FinancialData.CASH_FLOW[8]);
        long[] operating = getValue(operatingList);
        // init series data
        GraphViewSeries operatingSeries = new GraphViewSeries(
                "Operating",
                new GraphViewSeriesStyle(Color.rgb(250, 0, 30), 3),
                new GraphViewData[] {
                        new GraphViewData(0, operating[0]),
                        new GraphViewData(1, operating[1]),
                        new GraphViewData(2, operating[2]),
                        new GraphViewData(3, operating[3])
                });
        String[] investingList = stringListMap.get(FinancialData.CASH_FLOW[12]);
        long[] investing = getValue(investingList);
        // init series data
        GraphViewSeries investingSeries = new GraphViewSeries(
                "Investing",
                new GraphViewSeriesStyle(Color.rgb(0, 0, 250), 3),
                new GraphViewData[] {
                        new GraphViewData(0, investing[0]),
                        new GraphViewData(1, investing[1]),
                        new GraphViewData(2, investing[2]),
                        new GraphViewData(3, investing[3])
                });
        String[] financingList = stringListMap.get(FinancialData.CASH_FLOW[17]);
        long[] financing = getValue(financingList);
        // init series data
        GraphViewSeries financingSeries = new GraphViewSeries(
                "Financing",
                new GraphViewSeriesStyle(Color.rgb(50, 250, 90), 3),
                new GraphViewData[] {
                        new GraphViewData(0, financing[0]),
                        new GraphViewData(1, financing[1]),
                        new GraphViewData(2, financing[2]),
                        new GraphViewData(3, financing[3])
                });
        GraphView mCashFlowGraphView = new LineGraphView(
                mContext, // context
                "Cash Flow\t(in millions)" // heading
        );
        mCashFlowGraphView.addSeries(operatingSeries);
        mCashFlowGraphView.addSeries(investingSeries);
        mCashFlowGraphView.addSeries(financingSeries); // data
        mCashFlowGraphView.setHorizontalLabels(dateLabels);
        long min = findM(new long[][] {operating, investing, financing})[0];
        long max = findM(new long[][] {operating, investing, financing})[1];
        mCashFlowGraphView.setManualYAxisBounds(max*1.2, ((min > 0) ? 0 : min-max*0.2));
        mCashFlowGraphView.setShowLegend(true);
        mCashFlowGraphView.setLegendWidth(300);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                mCashFlowLayout.getWidth(), mCashFlowLayout.getWidth()-70);
        mCashFlowLayout.setLayoutParams(layoutParams);
        mCashFlowLayout.addView(mCashFlowGraphView);
    }
}
