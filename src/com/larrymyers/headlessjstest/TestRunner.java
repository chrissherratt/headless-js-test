package com.larrymyers.headlessjstest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.NativeArray;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Loads html testpages and waits for all javascript to execute.
 * Writes test results to the specified directory, in JUnit XML Report format.
 * 
 * Expects the report results to be available in a global javascript variable
 * called "reporter" with the follow data structure:
 * 
 * var reporter = [
 *   {
 *     name: "Test Suite Name",
 *     result: {
 *         passed: 3,
 *         failed: 0,
 *         total:  3
 *     },
 *     filename: "TEST-TestSuiteName.xml",
 *     text: "<testcases></testcases>"
 *   }
 * ];
 * 
 * For more information on JUnit XML Report Format:
 * 
 * http://stackoverflow.com/questions/428553/unable-to-get-hudson-to-parse-junit-test-output-xml
 * 
 * @author lbmyers
 *
 */
public class TestRunner {
    private Log log = LogFactory.getLog(TestRunner.class);
    
    private WebClient client;
    private String reportsDir;
    
    public TestRunner() {
        this.client = null;
        this.reportsDir = "";
    }
    
    /**
     * Loads each URL in the provided list, and records the results
     * after the javascript finishes executing.
     * @param testpages
     */
    public void runTestPages(List<URL> testpages) {
        if (this.client == null) {
            this.client = createDefaultClient();
        }
        
        for (URL testpage: testpages) {
            log.info(testpage.toString());
            
            HtmlPage page = null;
            
            try {
                page = this.client.getPage(testpage);
            } catch (FailingHttpStatusCodeException e) {
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            
            if (page == null) {
                log.error("Requested page was null.");
                continue;
            }
            
            long totalWait = 0;
            
            while (! (Boolean) page.executeJavaScript("reporter.finished").getJavaScriptResult()) {
                if (totalWait >= 10000) {
                    break;
                }
                
                log.info("Waiting for testpage reporter to finish ...");
                
                synchronized(page) {
                    try {
                        page.wait(500);
                    } catch (InterruptedException e) {
                        log.error("Page load interrupted.");
                    }
                }
            }
            
            ScriptResult result = page.executeJavaScript("reporter.reports");
            NativeArray reports = (NativeArray) result.getJavaScriptResult();
            
            String reportsFound = Long.valueOf(reports.getLength()).toString();
            log.info(reportsFound + " reports generated");
            
            for (long i = 0; i < reports.getLength(); i++) {
                NativeObject report = (NativeObject) reports.get(i);
                
                logResults(report);
                
                String filename = report.get("filename").toString();
                String reportXml = report.get("text").toString();
                
                File reportFile = new File(this.reportsDir + "/" + filename);
                
                try {
                    FileWriter out = new FileWriter(reportFile);
                    out.write(reportXml);
                    out.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            
            this.client.closeAllWindows();
        }
    }
    
    private void logResults(NativeObject report) {
        String name = report.get("name").toString();
        NativeObject results = (NativeObject) report.get("results");
        Double passed = (Double) results.get("passed");
        Double total = (Double) results.get("total");
        
        log.info(name + " : " + passed.intValue() + " of " + total.intValue() + " passed.");
    }
    
    private WebClient createDefaultClient() {
        WebClient client = new WebClient(BrowserVersion.FIREFOX_3);
        
        configureClientWithDefaultOptions(client);
        
        return client;
    }
    
    /**
     * Takes the given WebClient and configures it with sane options to load
     * html test pages that contain css, javascript, and use ajax calls.
     * @param client
     * @return
     */
    public WebClient configureClientWithDefaultOptions(WebClient client) {
        client.setCssEnabled(true);
        client.setJavaScriptEnabled(true);
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        client.setThrowExceptionOnScriptError(false);
        client.setIncorrectnessListener(new IncorrectnessListener() {
            @Override
            public void notify(String arg0, Object arg1) {
            }
        });
        
        return client;
    }
    
    /**
     * Set the HtmlUnit WebClient instance to use for
     * loading and executing the test pages.
     * @param client
     */
    public void setClient(WebClient client) {
        this.client = client;
    }

    /**
     * Get the WebClient instance used by this runner.
     * Can be null if it has not been explicitly set, or
     * runTestPages has not been called yet.
     * @return
     */
    public WebClient getClient() {
        return client;
    }

    /**
     * Sets the directory path to write the test reports to.
     * @param reportsDir
     */
    public void setReportsDir(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    /**
     * Gets the directory path to write the test reports to.
     * @return
     */
    public String getReportsDir() {
        return reportsDir;
    }
}
