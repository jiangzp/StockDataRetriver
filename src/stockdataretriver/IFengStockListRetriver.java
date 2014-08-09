/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockdataretriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.*;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.*;

/**
 *
 * @author ZhipingJiang
 */
public class IFengStockListRetriver {

    public static final String ifengStockListURL = "http://app.finance.ifeng.com/hq/list.php";
    public static final String ifengStockLineRegexp = "<li><a\\shref=.+\\starget=.+>.+</a></li>";
    private static final String ifengStockURLRegexp = "http.+tml";

    public static enum ifengUrlType {

        stock_a, stock_b, hkstock, usstock, fund, bond
    }

    public static enum ifengUrlClass {

        zs, ha, sa, qz, gem, hb, sb, gp, wl, fb, kf, hgz, hqz, hzz, sgz, sqz, szz
    }

    public static final Map<ifengUrlType, List<ifengUrlClass>> pageMap = new HashMap<>();

    static {
        pageMap.put(stock_a, Arrays.asList(new ifengUrlClass[]{zs, ha, sa, qz, gem}));
        pageMap.put(stock_b, Arrays.asList(new ifengUrlClass[]{hb, sb}));
        pageMap.put(hkstock, Arrays.asList(new ifengUrlClass[]{gp, wl}));
        pageMap.put(fund, Arrays.asList(new ifengUrlClass[]{fb, kf}));
        pageMap.put(bond, Arrays.asList(new ifengUrlClass[]{hgz, hqz, hzz, sgz, sqz, szz}));
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO code application logic here
        List<StockInformation> allStockList = new ArrayList<>();
        for (ifengUrlType type : pageMap.keySet()) {
            for (ifengUrlClass cls : pageMap.get(type)) {
                allStockList.addAll(ifengPagePoller(type, cls));
            }
        }
        System.out.println("totally " + allStockList.size() + " stocks");

        for (int i = 0; i < 100; i++) {
            NetEaseStockRecordsDownloader.allStockDownloader(allStockList, "/Users/ZhipingJiang/stock/netease");
            Thread.sleep(1000 * 120);
        }

    }

    public static List<StockInformation> ifengPagePoller(ifengUrlType type, ifengUrlClass cls) throws IOException {
        String url = ifengStockListURL + "?type=" + type.toString() + "&class=" + cls.toString();
        System.out.println(url);
        Response pageResponse = Request.Get(url).execute();
        String pageContent = IOUtils.toString(pageResponse.returnContent().asBytes(), "utf8");
        Pattern pattern = Pattern.compile(ifengStockLineRegexp);
        Pattern urlPattern = Pattern.compile(ifengStockURLRegexp);
        Matcher matcher = pattern.matcher(pageContent);

        String stockUrl = null;
        List<StockInformation> stockList = new ArrayList<>();
        while (matcher.find()) {

            String line = matcher.group();
            Matcher urlMatcher = urlPattern.matcher(line);
            if (urlMatcher.find()) {
                stockUrl = urlMatcher.group();
            }
            line = line.substring(line.lastIndexOf("\">") + 2);
            line = line.substring(0, line.indexOf("</"));
            String code = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));
            String name = line.substring(0, line.lastIndexOf("("));
            stockList.add(new StockInformation(code, name, stockUrl, type, cls));
        }
        System.out.println("found stocks : " + stockList.size());

        return stockList;
    }

}
