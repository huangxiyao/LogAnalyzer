package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public abstract class LogFileQuerier {
    
    public static final DecimalFormat format = new DecimalFormat("#0.0000");
    public static final Pattern pattern = Pattern.compile("\\d{8}");// means 8 digit; such as 20160530

    /**
     * Query all the log file under given path
     * @param path: Dir of Apache log files
     */
    public void query(String path){
        queryWithDateRange(path);
    }
            
    
    /**
     * Query log file with given date
     * @param path: Dir of apache log files
     * @param date YYYYMMDD
     */
    public void query(String path, String date) {
        queryWithDateRange(path,  date);
    }
    
    /**
     * Query log file with given fromDate and toDate
     * @param path: Dir of apache log files
     * @param fromDate
     * @param toDate
     */
    public void query(String path, String fromDate, String toDate) {
        queryWithDateRange(path, fromDate, toDate);
    }
    
    /**
     * Query all the log file given date
     * @param path: Dir of Apache log files
     * @param date: case null: query all file; case length==1: file==Date[0]; case length==2:  Date[0]<= file >=Date[1]
     * @return
     */
    private Map<String, String> queryWithDateRange(String path, String... date) {
        // Dir of apache log files such as access_log; access_log-20160530; ssl_access_log-20160530.gz
        File dir = new File(path);
        
        Map<String, MutableInt> totalReqs = new HashMap<String, MutableInt>();        
        String[] fileNames = null;
        if (dir.isDirectory()) {
            fileNames = dir.list();
        } else {
            System.out.println("Please input a DIR !");
            return null;
        }
        for (int i = 0; i < fileNames.length; i++) {
            
            File logFile = new File(dir.getAbsolutePath() + "/" + fileNames[i]);
            if (logFile.isDirectory()) continue;
            
            // check if file is in date range
            if (date != null && date.length != 0) {
                if (date.length == 1) {
                    Matcher matcher = pattern.matcher(fileNames[i]);
                    if (matcher.find()) {
                        if ( ! matcher.group().equals(date[0])) { 
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else if (date.length == 2) {
                    Matcher matcher = pattern.matcher(fileNames[i]);
                    if (matcher.find()) {
                        String fileDate = matcher.group();
                        if ( fileDate.compareTo(date[0]) < 0 || fileDate.compareTo(date[1]) > 0 ) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
            //System.out.println(dir.getAbsolutePath() + "/" + fileNames[i]);
            if (logFile.getName().endsWith(".gz")) {
                try (
                    GZIPInputStream in = new GZIPInputStream(new FileInputStream(logFile));
                    Reader decoder = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(decoder);
                ){
                     analyzeOneFile(br, totalReqs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try (
                    FileInputStream in = new FileInputStream(logFile);
                    Reader decoder = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(decoder);
                ){
                    analyzeOneFile(br, totalReqs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println(totalReqs);
        int countOfAllReq = 0;
        for (String key : totalReqs.keySet()) {
            countOfAllReq += totalReqs.get(key).get();
        }
        
        // ORDER BY request_count DESC
        List<Map.Entry<String,MutableInt>> list = new ArrayList<>(); 
        list.addAll(totalReqs.entrySet());  
        ValueComparator vc= new ValueComparator();  
        Collections.sort(list, vc); 
        
        Map<String, String> frequence = new LinkedHashMap<String, String>();
        for (Map.Entry<String,MutableInt> entry : list) {
            int countOfOneEprid = entry.getValue().get();
            frequence.put(entry.getKey(), format.format(countOfOneEprid * 100.0 / countOfAllReq));
        }
        //System.out.println(frequence);
        
        // Print with nice format (copy to excel and create a table easily)
        for (Entry<String, String> entry : frequence.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        return frequence;
    }
    
    public Map<String, MutableInt> analyzeOneFile(BufferedReader br, Map<String, MutableInt> totalReqs){
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String eprid = getEprid(line);
                //System.out.println(eprid);
                if (eprid != null) {
                    MutableInt count = totalReqs.get(eprid);
                    if (count == null) {
                        totalReqs.put(eprid, new MutableInt());
                    } else {
                        count.increment();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalReqs;
    }
    
    public abstract String getEprid(String line);
}
