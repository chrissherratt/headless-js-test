package com.larrymyers.headlessjstest;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SeleniumRunner {
    private Log log = LogFactory.getLog(SeleniumRunner.class);
    
    private String reportsDir;
    
    public SeleniumRunner() {
        this.reportsDir = null;
    }
    
    /**
     * Loads each URL in the provided list, and records the results
     * after the javascript finishes executing.
     * @param testpages
     * @return True if all test pages executed and passed successfully.
     */
    public boolean runTestPages(List<URL> testpages) {
        FirefoxDriver driver = new FirefoxDriver();
        
        boolean allPassed = true;
        
        for (URL testpage: testpages) {
            driver.get(testpage.toString());
            
            try {
                long totalWait = 0;
                
                if (driver.executeScript("return window.reporter") == null) {
                    log.error("Couldn't find reporter global var in DOM: " + testpage.toString());
                    continue;
                }
                
                while (! (Boolean) driver.executeScript("return window.reporter.finished")) {
                    if (totalWait >= 10000) {
                        break;
                    }
                    
                    Thread.sleep(500);
                    totalWait += 500;
                }
                
                Long reportCount = (Long) driver.executeScript("return window.reporter.reports.length");
                
                for (long i = 0; i < reportCount; i++) {
                    String name = driver.executeScript("return window.reporter.reports["+i+"].name").toString();
                    Long failed = (Long) driver.executeScript("return window.reporter.reports["+i+"].results.failed");
                    Long passed = (Long) driver.executeScript("return window.reporter.reports["+i+"].results.passed");
                    Long total = (Long) driver.executeScript("return window.reporter.reports["+i+"].results.total");
                    
                    if (failed > 0) {
                        log.info(name + ": " + failed + " tests of " + total + " failed!");
                        allPassed = false;
                    }
                    
                    String filename = driver.executeScript("return window.reporter.reports["+i+"].filename").toString();
                    String reportXml = driver.executeScript("return window.reporter.reports["+i+"].text").toString();
                    
                    File reportFile = new File(this.reportsDir + "/" + filename);
                    FileWriter out = new FileWriter(reportFile);
                    out.write(reportXml);
                    out.close();
                }
            } catch (Exception e) {
                log.error("Error generating report for: " + testpage.toString());
                e.printStackTrace();
                allPassed = false;
            }
        }
        
        driver.quit();
        
        return allPassed;
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
