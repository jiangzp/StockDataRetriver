/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockdataretriver;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.gem;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.gp;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.ha;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.hb;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.qz;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.sa;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.sb;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.zs;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.bond;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.fund;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.hkstock;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.stock_a;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.stock_b;

/**
 * fetch the historical per-day data from Netease.
 * The protocol may be obsolete someday.
 * @author ZhipingJiang
 */
public class NetEaseStockRecordsDownloader {

    public static final String neteaseURL = "http://quotes.money.163.com/service/chddata.html?code=";

    private static ExecutorService threadPool = null;
    private static final Set<StockInformation> remainStockSet = new HashSet<>();

    private static class StockDownloaderRunnable implements Runnable {

        public StockDownloaderRunnable(StockInformation stock, String fileoutputName) {
            this.stock = stock;
            this.outputName = fileoutputName;
        }

        private StockInformation stock;
        private String outputName;

        @Override
        public void run() {
            try {

                Thread.sleep(RandomUtils.nextInt(0, 1000));
                stockDownloader(stock, outputName);
            } catch (Exception ex) {
                remainStockSet.add(stock);
                System.out.println("error" + stock);
                System.out.println(remainStockSet.size());

            }
        }

        private boolean checkFile(File csvfile) {
            return csvfile.exists() && FileUtils.sizeOf(csvfile) > 500;
        }

        public void stockDownloader(StockInformation stock, String outputDir) throws IOException {
            String code = null, type = null, outputFile = null;
            if (stock.getType() == stock_a && stock.getCls() == sa) {
                code = stock.getCode();
                type = "1";
                outputFile = outputDir + "/" + stock.getCode() + ".sza.csv";
            }

            if (stock.getType() == stock_a && stock.getCls() == ha) {
                code = stock.getCode();
                type = "0";
                outputFile = outputDir + "/" + stock.getCode() + ".sha.csv";
            }

            if (stock.getType() == stock_a && stock.getCls() == zs) {
                code = stock.getCode();
                type = stock.getCode().startsWith("00") ? "0" : "1";
                outputFile = outputDir + "/" + stock.getCode() + ".zs.csv";
            }

            if (stock.getType() == stock_a && stock.getCls() == gem) {
                code = stock.getCode();
                type = "1";
                outputFile = outputDir + "/" + stock.getCode() + ".gem.csv";
            }

            if (stock.getType() == stock_b && stock.getCls() == hb) {
                code = stock.getCode();
                type = "0";
                outputFile = outputDir + "/" + stock.getCode() + ".hb.csv";
            }

            if (stock.getType() == stock_b && stock.getCls() == sb) {
                code = stock.getCode();
                type = "1";
                outputFile = outputDir + "/" + stock.getCode() + ".sb.csv";
            }

            if (stock.getType() == hkstock || stock.getType() == bond || stock.getType() == fund || stock.getCls() == qz) {
                return;
            }

            if (outputFile == null) {
                System.out.println(stock);
            }

            File csvfile = new File(outputFile);
            if (checkFile(csvfile)) {
                return;
            }

            String downloadURL = neteaseURL + type + code + "&start=19800101";
            System.out.println("try fetch data" + downloadURL);
//            System.out.println(downloadURL);
            Request csvRequest = Request.Get(downloadURL).addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8").
                    userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36").
                    addHeader("Accept-Encoding", "gzip,deflate,sdch").addHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh-TW;q=0.4").
                    connectTimeout(1000).socketTimeout(1000);
            Response csvResponse = csvRequest.execute();
//            System.out.println("got data" + downloadURL);
            csvResponse.saveContent(csvfile);
        }

    }

    public static void allStockDownloader(List<StockInformation> stockList, String outputDir) {
        if (threadPool != null) {
            threadPool.shutdownNow();

        }
        threadPool = Executors.newFixedThreadPool(30);
        Collections.shuffle(stockList);
        stockList.stream().forEach((StockInformation stockInformation) -> {
            threadPool.execute(new StockDownloaderRunnable(stockInformation, outputDir));
        });
        System.out.println("all tasks submitted");

    }
}
