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

/**
 * Custom Ant Task to execute javascript unit tests from within
 * html test pages.
 * 
 * @author larrymyers
 *
 */
public class SeleniumTestTask extends Task {

    private List<FileSet> filesets;
    private String reportsDir;
    private boolean failOnError;
    
    @Override
    public void init() throws BuildException {
        super.init();
        
        this.filesets = new ArrayList<FileSet>();
        this.reportsDir = "";
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
        
        SeleniumRunner runner = new SeleniumRunner();
        runner.setReportsDir(reportsDir);
        
        boolean allPassed = runner.runTestPages(testpages);
        
        if (! allPassed && failOnError) {
            throw new BuildException("Not all test pages passed.");
        }
    }
}
