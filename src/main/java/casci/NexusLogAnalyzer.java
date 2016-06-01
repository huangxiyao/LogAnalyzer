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
public class NexusLogAnalyzer extends LogFileQuerier {
    public static final Pattern pattern = Pattern.compile("/\\d{6}/");// means "/" + 6 digit + "/"  such as /201377/

    public static void main(String[] args) {
        // sample nexus data
        //16.85.88.18 - - [31/May/2016:03:18:19 +0000] "GET /nexus/content/repositories/snapshots/com/hp/ts/201377/selenium-core-fw/2.0.0-SNAPSHOT/maven-metadata.xml HTTP/1.1" 200 1008 "-" "Apache-Maven/3.2.1 (Java 1.8.0_72-internal; Linux 3.10.0-229.el7.x86_64)"
        
        String path = "C:\\Users\\shijie\\Desktop\\nexus_log\\";
        NexusLogAnalyzer logAnalyzer = new NexusLogAnalyzer();
        
        System.out.println("Nexus metrics report:");
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
        if (line.indexOf(" /nexus/content/repositories") != -1) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String epridWithSlash = matcher.group();
                return epridWithSlash.substring(1, 7);
            }
        }
        return null;
    }
}
