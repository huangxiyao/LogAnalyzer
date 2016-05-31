package fez;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import util.MutableInt;
import util.ValueComparator;

/**
 * Get usage per Eprid from Apache log file (able to handle plain txt file and gz file)
 * @author Terry
 *
 */
public class FezLogAnalyzer {
    
    public static DecimalFormat format = new DecimalFormat("#0.0000");
    
    public static void main(String[] args) {
        // Dir of apache log files such as access_log; access_log-20160530; ssl_access_log-20160530.gz
        File dir = new File("C:\\Users\\shijie\\Desktop\\fez_log\\");
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
    
    public static Map<String, MutableInt> analyzeOneFile(BufferedReader br, Map<String, MutableInt> totalReqs){
        
        String line;
        try {
            while ((line = br.readLine()) != null) {
                // sample line for access_log
                // 16.207.68.44 - APP-112166-FEZ-PRO [31/May/2016:04:02:05 +0000] "PROPFIND /fez/112166/sadb-collections/sadb_p1_heartbeat/ HTTP/1.1" 207 902 289 394 1065
                // sample line for ssl_access_log
                // 16.250.12.85 - APP-119812-FEZ-PRO [31/May/2016:04:02:43 +0000] "HEAD /fez/119812/IN/UPSI/IN/deviceConfiguration/P.CLQR.LZ_SRVC_OBJ_RELSHP.LZ_SRVC_OBJ_RELSHP.79947.2_4_2.20160531.035643.dat HTTP/1.1" 200 - 202 718 3149
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
    
    /**
     * Extract eprid from apache log file
     * @param line:  one line form apache access log or ssl access log
     * @return eprid
     */
    public static String getEprid(String line) {
        int idx = line.indexOf("/fez/");
        if (idx != -1) {
            String eprid = line.substring(idx+5, idx+5+6);
            return eprid;
        } else {
            return null;
        }
    }
}

