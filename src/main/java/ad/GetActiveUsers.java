package ad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import util.MutableInt;
import util.ValueComparator;

/**
 * Get usage by APP UID
 * @author Terry
 *
 */
public class GetActiveUsers {
    public static void main(String[] args) {
        Map<String, MutableInt> freq = new HashMap<String, MutableInt>();
        // Dir of ad-service.log or filename like ad-service-log.2016-04-24.log
        File dir = new File("data");
        String[] fileNames = dir.list();
        for (int i = 0; i < fileNames.length; i++) {
            //System.out.println(fileNames[i]);
            String fileName = dir.getAbsolutePath() + "/" + fileNames[i];
         
    
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // sample data
                    // 2016-04-28 00:00:00,221 [TP-Processor34] [ap-uid=w-mdcp:prd-http] DEBUG AbstractAddressFinderRequestLogger.process:100 ENTRY AddressQuery [country1=CN, locality1=Handan, province1=Hebei, postalCode1=057350, characterScriptDetectionIndicator=true]
                    int idxFrom = line.indexOf("ap-uid=");
                    int idxEnd = line.indexOf("]", idxFrom);
                    if (idxFrom != -1) {
                        String userId = line.substring(idxFrom + 7, idxEnd);
                        MutableInt count = freq.get(userId);
                        if (count == null) {
                            freq.put(userId, new MutableInt());
                        } else {
                            count.increment();
                        }
                    }
                }
                //System.out.println(freq);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // // ORDER BY request_count DESC
        List<Map.Entry<String,MutableInt>> list=new ArrayList<>(); 
        list.addAll(freq.entrySet());  
        ValueComparator vc=new ValueComparator();  
        Collections.sort(list, vc); 
        //System.out.println(list);
        
        // Print with nice format (copy to excel and create a table easily)
        for (Entry<String, MutableInt> entry : list) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }
}

