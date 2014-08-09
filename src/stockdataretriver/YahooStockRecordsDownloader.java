/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockdataretriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicHeader;
import static stockdataretriver.IFengStockListRetriver.ifengUrlClass.*;
import static stockdataretriver.IFengStockListRetriver.ifengUrlType.*;

/**
 *
 * @author ZhipingJiang
 */
public class YahooStockRecordsDownloader {

    public static final String yahooChnURL = "http://table.finance.yahoo.com/table.csv";

    private static ExecutorService threadPool = null;
    private static final Set<StockInformation> remainStockSet = new HashSet<StockInformation>();

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

                Thread.sleep(RandomUtils.nextInt(100, 1000));
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
                type = "sz";
                outputFile = outputDir + "/" + stock.getCode() + ".sza.csv";
            }

            if (stock.getType() == stock_a && stock.getCls() == ha) {
                code = stock.getCode();
                type = "ss";
                outputFile = outputDir + "/" + stock.getCode() + ".sha.csv";
            }

            if (stock.getType() == stock_a && stock.getCls() == zs) {
                code = stock.getCode();
                type = "sz";
                outputFile = outputDir + "/" + stock.getCode() + ".zs.csv";
            }

            if (stock.getType() == stock_a && stock.getCls() == gem) {
                code = stock.getCode();
                type = "sz";
                outputFile = outputDir + "/" + stock.getCode() + ".gem.csv";
            }

            if (stock.getType() == stock_b && stock.getCls() == hb) {
                code = stock.getCode();
                type = "ss";
                outputFile = outputDir + "/" + stock.getCode() + ".hb.csv";
            }

            if (stock.getType() == stock_b && stock.getCls() == sb) {
                code = stock.getCode();
                type = "sz";
                outputFile = outputDir + "/" + stock.getCode() + ".sb.csv";
            }

            if (stock.getType() == hkstock && stock.getCls() == gp) {
                code = stock.getCode().substring(1);
                type = "hk";
                outputFile = outputDir + "/" + stock.getCode() + ".hk.csv";
            }

            if (stock.getType() == bond || stock.getType() == fund || stock.getCls() == qz) {
                return;
            }

            if (outputFile == null) {
                System.out.println(stock);
            }

            File csvfile = new File(outputFile);
            if (checkFile(csvfile)) {
                return;
            }

            String downloadURL = yahooChnURL + "?s=" + code + "." + type;
            System.out.println("try fetch data" + downloadURL);
//            System.out.println(downloadURL);
            Request csvRequest = Request.Get(downloadURL).addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8").
                    userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36").
                    addHeader("Accept-Encoding", "gzip,deflate,sdch").addHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh-TW;q=0.4").
                    addHeader("DNT", "1").
                    connectTimeout(1000).socketTimeout(1000);
            System.out.println(csvRequest.toString());
            Response csvResponse = csvRequest.execute();
            System.out.println("got data" + downloadURL);
            System.out.println(csvResponse.returnResponse().getStatusLine());
            csvResponse.saveContent(csvfile);
        }

    }

    public static void allStockDownloader(List<StockInformation> stockList, String outputDir) {
        if (threadPool != null) {
            threadPool.shutdownNow();

        }
        threadPool = Executors.newFixedThreadPool(10);
        Collections.shuffle(stockList);
        stockList.stream().forEach((StockInformation stockInformation) -> {
            threadPool.execute(new StockDownloaderRunnable(stockInformation, outputDir));
        });
        System.out.println("all tasks submitted");

    }
}
