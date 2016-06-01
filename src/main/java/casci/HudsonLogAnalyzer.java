package casci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.LogFileQuerier;

/**
 * Get usage per Eprid from Apache log file (able to handle plain txt file and
 * gz file)
 * 
 * @author Terry
 *
 */
public class HudsonLogAnalyzer extends LogFileQuerier {
    public static final Pattern pattern = Pattern.compile("/\\d{6}");// means "/" + 6 digit    such as /201377

    public static void main(String[] args) {
        // sample nexus data
        // [31/May/2016:03:18:50 +0000] 16.178.110.132 TLSv1.2 ECDHE-RSA-AES128-GCM-SHA256 "POST /hudson/view/200456-mdcp-mdm/view/200456-mdm-hpe/view/2
        
        String path = "C:\\Users\\shijie\\Desktop\\nexus_log\\";
        HudsonLogAnalyzer logAnalyzer = new HudsonLogAnalyzer();
        //logAnalyzer.query(path);
        
        System.out.println("Hudson metrics report:");
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
     * 
     * @param line:
     *            one line from apache access log or ssl access log
     * @return eprid
     */
    public String getEprid(String line) {
        if (line.indexOf(" /hudson/") != -1) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String epridWithSlash = matcher.group();
                return epridWithSlash.substring(1, 7);
            }
        }
        return null;
    }
}
