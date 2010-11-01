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
 * Expects a global javascript variable called 'reporter' to be available, and contain
 * the following data structure for the TestRunner to generate and report results correctly.
 * 
 * var reporter = {
 *   finished: true,
 *   reports: [
 *     {
 *       name: "Test Suite Name",
 *       result: {
 *         passed: 3,
 *         failed: 0,
 *         total:  3
 *       },
 *       filename: "TEST-TestSuiteName.xml",
 *       text: "<testcases></testcases>"
 *     }
 *   ]
 * };
 * 
 * For more information on JUnit XML Report Format:
 * 
 * http://stackoverflow.com/questions/428553/unable-to-get-hudson-to-parse-junit-test-output-xml
 * 
 * @author larrymyers
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
     * @return True if all test pages executed and passed successfully.
     */
    public boolean runTestPages(List<URL> testpages) {
        if (this.client == null) {
            this.client = createDefaultClient();
        }
        
        boolean allPassed = true;
        
        for (URL testpage: testpages) {
            log.debug(testpage.toString());
            
            HtmlPage page = null;
            
            try {
                page = this.client.getPage(testpage);
            } catch (FailingHttpStatusCodeException e) {
                log.error(e.getMessage());
                allPassed = false;
            } catch (IOException e) {
                log.error(e.getMessage());
                allPassed = false;
            }
            
            if (page == null) {
                log.error("Requested page was null: " + testpage.toString());
                allPassed = false;
                continue;
            }
            
            try {
                long totalWait = 0;
                
                while (! (Boolean) page.executeJavaScript("reporter.finished").getJavaScriptResult()) {
                    if (totalWait >= 10000) {
                        break;
                    }
                    
                    log.debug("Waiting for testpage reporter to finish ...");
                    
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
                log.debug(reportsFound + " reports generated");
                
                for (long i = 0; i < reports.getLength(); i++) {
                    NativeObject report = (NativeObject) reports.get(i);
                    
                    boolean passed = logResults(report);
                    
                    if (!passed) {
                        allPassed = false;
                    }
                    
                    String filename = report.get("filename").toString();
                    String reportXml = report.get("text").toString();
                    
                    File reportFile = new File(this.reportsDir + "/" + filename);
                    FileWriter out = new FileWriter(reportFile);
                    out.write(reportXml);
                    out.close();
                }
            } catch (Exception e) {
                log.error("Error generating report for: " + testpage.toString());
                allPassed = false;
            } finally {
                this.client.closeAllWindows();
            }
        }
        
        return allPassed;
    }
    
    private boolean logResults(NativeObject report) {
        String name = report.get("name").toString();
        NativeObject results = (NativeObject) report.get("results");
        int passed = ((Double) results.get("passed")).intValue();
        int total = ((Double) results.get("total")).intValue();
        
        log.info(name + " : " + passed + " of " + total + " passed.");
        
        if (passed != total) {
            return false;
        }
        
        return true;
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
