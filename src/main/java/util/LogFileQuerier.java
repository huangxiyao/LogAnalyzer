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
import java.util.zip.GZIPInputStream;

public abstract class LogFileQuerier {
    
    public static DecimalFormat format = new DecimalFormat("#0.0000");
    
    public void query(String path) {
        // Dir of apache log files such as access_log; access_log-20160530; ssl_access_log-20160530.gz
        File dir = new File(path);
        Map<String, MutableInt> totalReqs = new HashMap<String, MutableInt>();        
        
        String[] fileNames = dir.list();
        for (int i = 0; i < fileNames.length; i++) {
            //System.out.println(dir.getAbsolutePath() + "/" + fileNames[i]);
            File logFile = new File(dir.getAbsolutePath() + "/" + fileNames[i]);
        
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
        List<Map.Entry<String,MutableInt>> list=new ArrayList<>(); 
        list.addAll(totalReqs.entrySet());  
        ValueComparator vc=new ValueComparator();  
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
