package com.larrymyers.headlessjstest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Custom Ant Task to execute javascript unit tests from within
 * html test pages.
 * 
 * @author larrymyers
 *
 */
public class RunTestPageTask extends Task {

    private List<FileSet> filesets;
    private String reportsDir;
    private BrowserVersion browserVersion;
    private boolean failOnError;
    
    @Override
    public void init() throws BuildException {
        super.init();
        
        this.filesets = new ArrayList<FileSet>();
        this.reportsDir = "";
        this.browserVersion = BrowserVersion.FIREFOX_3;
        this.failOnError = false;
    }

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
    
    public void setReportDir(String path) {
        this.reportsDir = path;
    }
    
    public void setFailonerror(boolean flag) {
        this.failOnError = flag;
    }
    
    public void setBrowserVersion(String version) {
        version = version.toLowerCase();
        
        if (version.equals("firefox3")) {
            this.browserVersion = BrowserVersion.FIREFOX_3;
        }
        
        if (version.equals("ie6")) {
            this.browserVersion = BrowserVersion.INTERNET_EXPLORER_6;
        }
        
        if (version.equals("ie7")) {
            this.browserVersion = BrowserVersion.INTERNET_EXPLORER_7;
        }
        
        if (version.equals("ie8")) {
            this.browserVersion = BrowserVersion.INTERNET_EXPLORER_8;
        }
    }
    
    @Override
    public void execute() throws BuildException {
        List<URL> testpages = new ArrayList<URL>();
        
        for (FileSet fs: this.filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner();
            
            for (String filename: ds.getIncludedFiles()) {
                File f = new File(ds.getBasedir(), filename);
                
                try {
                    testpages.add(f.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        TestRunner runner = new TestRunner();
        WebClient client = new WebClient(this.browserVersion);
        
        runner.setClient(runner.configureClientWithDefaultOptions(client));
        runner.setReportsDir(this.reportsDir);
        boolean allPassed = runner.runTestPages(testpages);
        
        if (! allPassed && failOnError) {
            throw new BuildException("Not all test pages passed.");
        }
    }
}
