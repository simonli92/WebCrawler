package io.WebCrawler;
import io.CrawlerAtribute.item;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrawlerHelper {
    private static final String AMAZON_QUERY_URL = "url";
    //user_agent can change version due to each browser's update
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/537.13 (KHTML, like Gecko) Chrome/24.0.1290.1 Safari/537.13";
    private final String authUser = "username";
    private final String authPassword = "password";
    private List<String[]> proxyList;
    private List<String> titleList;
    private List<String> categoryList;
    BufferedWriter logBFWriter;

    private int index = 0;

    public CrawlerHelper(String proxyListFile, String logFile) {
        initProxyList(proxyListFile);

        initHtmlSelector();

        initLog(logFile);
    }

    public void cleanup() {
        if (logBFWriter != null) {
            try {
                logBFWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initProxyList(String proxyListFile) {
        proxyList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(proxyListFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(":");
                proxyList.add(fields);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
    }

    private void initHtmlSelector() {
        //add initial selector here
        titleList = new ArrayList<String>();
        titleList.add("html selector");

        categoryList = new ArrayList<String>();
        categoryList.add("html selector");
    }

    private void initLog(String logFile) {
        try {
            File log = new File(logFile);
            if (!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            logBFWriter = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setProxy() {
        //rotate to avoid banned
        if (index == proxyList.size()) {
            index = 0;
        }
        String[] proxy = proxyList.get(index);
        System.setProperty("socksProxyHost", proxy[0].trim()); // set proxy server
        System.setProperty("socksProxyPort", proxy[1].trim()); // set proxy port
        index++;
    }

    private void testProxy() {
        System.setProperty("socksProxyHost", "ip"); // set only one proxy server selected from proxy list
        System.setProperty("socksProxyPort", "port"); // set proxy port based on proxy list
        String test_url = "http://www.toolsvoid.com/what-is-my-ip-address";
        try {
            Document doc = Jsoup.connect(test_url).userAgent(USER_AGENT).timeout(10000).get();
            String iP = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").first().text(); //get used IP.
            System.out.println("IP-Address: " + iP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<item> getResult(String keyword) {
        List<item> results = new ArrayList<>();

        try {
            //testProxy();
            setProxy();

            String url = AMAZON_QUERY_URL + keyword;
            HashMap<String,String> headers = new HashMap<String,String>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).timeout(100000).get();
            //System.out.println(doc.text());

            Elements itemList = doc.select("#s-results-list-atf").select("li");// amazon search result list element
            System.out.println("num of results = " + results.size());

            for (int i = 0; i < itemList.size(); i++) {
                item a = new item();
                a.keyWord = keyword;

                //title
                for (String title : titleList) {
                    String title_ele_path = "#result_"+Integer.toString(i)+ title;
                    Element title_ele = doc.select(title_ele_path).first();
                    if(title_ele != null) {
                        //System.out.println("title = " + title_ele.text());
                        a.title = title_ele.text();
                        break;
                    }
                }

                if (a.title == "") {
                    logBFWriter.write("cannot parse title for keyword: " + keyword);
                    logBFWriter.newLine();
                    continue;
                }

                //picture
                String thumbnail_path = "#result_"+Integer.toString(i)+"imgSelector";
                Element thumbnail_ele = doc.select(thumbnail_path).first();
                if(thumbnail_ele != null) {
                    //System.out.println("thumbnail = " + thumbnail_ele.attr("src"));
                    a.thumbnail = thumbnail_ele.attr("src");
                }


                //detail url
                String detail_path = "#result_"+Integer.toString(i)+"detailLink";
                Element detail_url_ele = doc.select(detail_path).first();
                if(detail_url_ele != null) {
                    String detail_url = detail_url_ele.attr("href");
                    //System.out.println("detail = " + detail_url);
                    a.detail_url = detail_url;
                } else {
                    logBFWriter.write("cannot parse detail for keyword:" + keyword + ", title: " + a.title);
                    logBFWriter.newLine();
                    continue;
                }

                //brand
                String brand_path = "#result_"+Integer.toString(i)+"brandSelector";
                Element brand = doc.select(brand_path).first();
                if(brand != null) {
                    //System.out.println("brand = " + brand.text());
                    a.brand = brand.text();
                }

                //price
                a.price = 0.0;
                String price_whole_path = "#result_"+Integer.toString(i)+"wholepriceSelector";
                String price_fraction_path = "#result_"+Integer.toString(i)+"fractionpriceSelector";
                Element price_whole_ele = doc.select(price_whole_path).first();
                if(price_whole_ele != null) {
                    String price_whole = price_whole_ele.text();
                    //System.out.println("price whole = " + price_whole);
                    //remove ","
                    //1,000
                    if (price_whole.contains(",")) {
                        price_whole = price_whole.replaceAll(",","");
                    }
                    try {
                        a.price = Double.parseDouble(price_whole);
                    } catch (NumberFormatException ne) {
                        // TODO Auto-generated catch block
                        ne.printStackTrace();
                        //log
                    }
                }

                Element price_fraction_ele = doc.select(price_fraction_path).first();
                if(price_fraction_ele != null) {
                    //System.out.println("price fraction = " + price_fraction_ele.text());
                    try {
                        a.price = a.price + Double.parseDouble(price_fraction_ele.text()) / 100.0;
                    } catch (NumberFormatException ne) {
                        ne.printStackTrace();
                    }
                }

                //category
                for (String category : categoryList) {
                    Element category_ele = doc.select(category).first();
                    if(category_ele != null) {
                        //System.out.println("category = " + category_ele.text());
                        a.category = category_ele.text();
                        break;
                    }
                }

                results.add(a);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
