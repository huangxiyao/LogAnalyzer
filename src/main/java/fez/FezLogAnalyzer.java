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

import casci.NexusLogAnalyzer;
import util.LogFileQuerier;
import util.MutableInt;
import util.ValueComparator;

/**
 * Get usage per Eprid from Apache log file (able to handle plain txt file and gz file)
 * @author Terry
 *
 */
public class FezLogAnalyzer extends LogFileQuerier {
    
    
    public static void main(String[] args) {
        // sample FEZ line for access_log
        // 16.207.68.44 - APP-112166-FEZ-PRO [31/May/2016:04:02:05 +0000] "PROPFIND /fez/112166/sadb-collections/sadb_p1_heartbeat/ HTTP/1.1" 207 902 289 394 1065
        // sample FEZ line for ssl_access_log
        // 16.250.12.85 - APP-119812-FEZ-PRO [31/May/2016:04:02:43 +0000] "HEAD /fez/119812/IN/UPSI/IN/deviceConfiguration/P.CLQR.LZ_SRVC_OBJ_RELSHP.LZ_SRVC_OBJ_RELSHP.79947.2_4_2.20160531.035643.dat HTTP/1.1" 200 - 202 718 3149
        String path = "C:\\Users\\shijie\\Desktop\\fez_log\\";
        FezLogAnalyzer logAnalyzer = new FezLogAnalyzer();

        System.out.println("FEZ metrics report:");
        System.out.println("Date: 20160528 --- 20160531");
        logAnalyzer.query(path, "20160528", "20160531");
        
        System.out.println("Date: 20160528");
        logAnalyzer.query(path, "20160528");

        System.out.println("Date: 20160529");
        logAnalyzer.query(path, "20160529");

        System.out.println("Date: 20160530");
        logAnalyzer.query(path, "20160530");

        System.out.println("Date: 20160531");
        logAnalyzer.query(path, "20160531");
    }
    
    
    /**
     * Extract eprid from apache log file
     * @param line:  one line form apache access log or ssl access log
     * @return eprid
     */
    public String getEprid(String line) {
        int idx = line.indexOf("/fez/");
        if (idx != -1) {
            String eprid = line.substring(idx+5, idx+5+6);
            return eprid;
        } else {
            return null;
        }
    }
}

