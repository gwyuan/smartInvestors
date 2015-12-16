package harristech.smartinvestors;

/**
 * Created by henry on 11/29/14.
 */
public class StockInfo {
    private String ticker;
    private String company_name;
    private String exchange_market;
    private String stock_type;

    public StockInfo(String symbol, String name, String exchDisp, String typeDisp) {
        ticker = symbol;
        company_name = name;
        exchange_market = exchDisp;
        stock_type = typeDisp;
    }
    public StockInfo(String[] info) {
        ticker = info[0];
        company_name = info[1];
        exchange_market = info[2];
        stock_type = info[3];
    }

    public String getStockInfo() {
        String res = getTicker() + " - " + getCompany_name() + "\n"
                + getExchange_market() + "-" + getStock_type();
        return res;
    }

    public String getTicker() {
        return ticker;
    }

    public String getCompany_name() {
        return company_name;
    }

    public String getExchange_market() {
        return exchange_market;
    }

    public String getStock_type() {
        return stock_type;
    }
}
