package ad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import util.MutableInt;
import util.ValueComparator;

/**
 * Get Address Service usage group by APP UID
 * @author Terry
 * @see https://hpedia.osp.hpe.com/wiki/Address_Service_Support_Guide#Log_Path
 * ad-service.log and ad-legacy-service.log come from /opt/casfw/data-match1/data-match-2016.04.r3/var/log/data-match-web
 * access_log come from /opt/webhost/logs/WHA-General-Inst/apache/logs/
 * only ad-service.log contains APP UID,  ad-legacy-service.log and apache log don't have APP UID
 *
 * example of ad-service.log
 * 2016-06-07 00:00:00,024 [TP-Processor33] [ap-uid=w-mdcp:prd-http] DEBUG AbstractAddressFinderRequestLogger.process:100 ENTRY AddressQuery [country1=AU, locality1=CAULFIELD NORTH, province1=VICTORIA, postalCode1=3161, deliveryAddressLine1=UNIT 8, deliveryAddressLine2=12 WYUNA ROAD, characterScriptDetectionIndicator=true]
 * example of ad-legacy-service.log
 * 2016-06-07 05:49:30,030 [TP-Processor45] [] DEBUG LegacyAddressDoctorAddressAnalyzerRequestLogger.analyzeHybridAddress:171 Input address: [222 N LA SALLE ST, , , CHICAGO, , IL, 60601-1003, US, null, null, null, null, null]
 * example of access_log
 * 16.193.104.226 - - [07/Jun/2016:04:00:20 +0000] "GET /match/validatedAddress?locality1=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0&country1=RU&preferredScript=ASCII_EXTENDED&deliveryAddressLine1=%D0%9C%D0%BE%D1%81%D1%84%D0%B8%D0%BB%D1%8C%D0%BC%D0%BE%D0%B2%D1%81%D0%BA%D0%B0%D1%8F,+%D0%B4.70&characterScriptDetectionIndicator=true&postalCode1=119590 HTTP/1.1" 200 3082 "-" "Java/1.7.0_09"
 * 16.248.19.121 - - [07/Jun/2016:04:33:42 +0000] "GET /legacy-match/address/v1?wsdl HTTP/1.1" 200 6178 "-" "curl/7.29.0"
 * 
 * 
 */
public class GetActiveUsers {
    
	public static final DecimalFormat format = new DecimalFormat("#0.0000");
    /**
     * 
     * @param args: args[0] dir of log file; args[1] file name (optional)   
     */
    public static void main(String[] args) {
        // for local running        
//        args = new String[2];
//        args[0] = "C:\\Users\\shijie\\git\\LogAnalyzer\\data\\g4t8318-data-match3\\";
        
        Map<String, String> epridUidMap = new HashMap<String, String>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();           
        try (
            InputStream stream = loader.getResourceAsStream("eprid_uid_mapping.properties");
            Reader decoder = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(decoder);
        ){
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] s = line.split("=");
                    epridUidMap.put(s[0], s[1]);
                }
            } catch (IOException e) {
            }
        } catch (IOException e) {
        }
        System.out.println(epridUidMap.keySet());
        
        Map<String, MutableInt> freq = new HashMap<String, MutableInt>();
        // FIXME: Dir of ad-service.log or archive file like ad-service-log.2016-04-24.log
        File dir = new File(args[0]);
        // FIXME: which file you want to search
        String date = args.length==2 ? args[1]:null; // "ad-service-log.2016-06-06.log";
        String[] fileNames = dir.list();
        for (int i = 0; i < fileNames.length; i++) {
            //System.out.println(fileNames[i]);
            if (date!=null && !fileNames[i].equals(date)) {
                continue;
            }
            String fileName = dir.getAbsolutePath() + "/" + fileNames[i];
            File file = new File(fileName);
            if (! file.isFile()) {
                continue;
            }
            System.out.println(fileName);
            
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // sample data
                    // 2016-04-28 00:00:00,221 [TP-Processor34] [ap-uid=w-mdcp:prd-http] DEBUG AbstractAddressFinderRequestLogger.process:100 ENTRY AddressQuery [country1=CN, locality1=Handan, province1=Hebei, postalCode1=057350, characterScriptDetectionIndicator=true]
                    int idxFrom = line.indexOf("ap-uid=");
                    int idxEnd = line.indexOf("]", idxFrom);
                    if (idxFrom != -1 && line.indexOf("RETURN") != -1) {
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
        
        int countOfAllReq = 0;
        for (String key : freq.keySet()) {
            countOfAllReq += freq.get(key).get();
        }
        
        // Print with nice format (copy to excel and create a table easily)
        System.out.println("###### Copy below output to excel file ######");
        System.out.println("App-Instance" + "\t" + "EPR ID" + "\t" + "L2 Business ORG" +"\t" + "Usage %" + "\t" + "# of Address Validations");
        for (Entry<String, MutableInt> entry : list) {
        	String eprid_org = epridUidMap.get(entry.getKey());
        	String[] epridOrgArray = eprid_org.split(",");
            System.out.println(entry.getKey() + "\t" + epridOrgArray[0] + "\t" + epridOrgArray[1] + "\t" + format.format(entry.getValue().get() * 100.0 / countOfAllReq) + "\t" + entry.getValue());
        }
        
    }
}

