package com.larrymyers.headlessjstest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.NativeArray;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class RunTestPage extends Task {

    private List<FileSet> filesets = new ArrayList<FileSet>();
    private String reportsDir;
    
    public RunTestPage() {
    }
    
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
    
    public void setReportDir(String path) {
        this.reportsDir = path;
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
        
        for (URL testpage: testpages) {
            log(testpage.toString(), Project.MSG_INFO);
            
            WebClient client = new WebClient();
            HtmlPage page = null;
            
            client.setCssEnabled(true);
            client.setJavaScriptEnabled(true);
            client.setAjaxController(new NicelyResynchronizingAjaxController());
            client.setIncorrectnessListener(new IncorrectnessListener() {
                @Override
                public void notify(String arg0, Object arg1) {
                }
            });
            
            try {
                page = client.getPage(testpage);
            } catch (FailingHttpStatusCodeException e) {
                log(e.getMessage(), Project.MSG_ERR);
            } catch (IOException e) {
                log(e.getMessage(), Project.MSG_ERR);
            }
            
            if (page == null) {
                log("Requested page was null.", Project.MSG_ERR);
                continue;
            }
            
            ScriptResult result = page.executeJavaScript("reporter.reports");
            NativeArray reports = (NativeArray) result.getJavaScriptResult();
            
            String reportsFound = Long.valueOf(reports.getLength()).toString();
            log(reportsFound + " reports generated", Project.MSG_INFO);
            
            for (long i = 0; i < reports.getLength(); i++) {
                NativeObject report = (NativeObject) reports.get(i);
                log(report.get("name").toString(), Project.MSG_INFO);
                
                String filename = report.get("filename").toString();
                String reportXml = report.get("text").toString();
                
                File reportFile = new File(this.reportsDir + "/" + filename);
                
                try {
                    FileWriter out = new FileWriter(reportFile);
                    out.write(reportXml);
                    out.close();
                } catch (IOException e) {
                    log(e.getMessage(), Project.MSG_ERR);
                }
            }
            
            client.closeAllWindows();
        }
    }
}
