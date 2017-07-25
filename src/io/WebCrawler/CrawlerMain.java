package io.WebCrawler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.CrawlerAtribute.item;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class CrawlerMain {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Please input file: <KeyFile> <ResultFile> <ProxyListFile> <LogFile>");
            System.exit(0);
        }

        ObjectMapper mapper = new ObjectMapper();
        String rawQuery = args[0];
        String result = args[1];
        String proxyList = args[2];
        String logfile = args[3];

        HashMap<String,Integer> map = new HashMap<>();

        CrawlerHelper crawler = new CrawlerHelper(proxyList, logfile);
        File file = new File(result);
        //if file does not exist,create a new file
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        try(BufferedReader br = new BufferedReader(new FileReader(rawQuery))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                //System.out.println(line);
                String keyword = line.trim();
                if (map.containsKey(keyword)) {
                    continue;
                }
                map.put(keyword, 1);
                List<item> resultList = crawler.getResult(keyword);
                for (item attri : resultList) {
                    String jsonInString = mapper.writeValueAsString(attri);
                    bw.write(jsonInString);
                    bw.newLine();
                }
                Thread.sleep(5000);
            }
            bw.close();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        crawler.cleanup():
    }
}
