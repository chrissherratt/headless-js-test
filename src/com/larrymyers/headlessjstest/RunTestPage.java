package com.larrymyers.headlessjstest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class RunTestPage extends Task {

    private List<URL> testpages;
    private List<FileSet> filesets = new ArrayList<FileSet>();
    
    public RunTestPage() {
        this.testpages = new ArrayList<URL>();
    }
    
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
    
    @Override
    public void execute() throws BuildException {
        for (FileSet fs: this.filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner();
            
            for (String filename: ds.getIncludedFiles()) {
                File f = new File(ds.getBasedir(), filename);
                
                try {
                    this.testpages.add(f.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        for (URL testpage: this.testpages) {
            log(testpage.toString(), Project.MSG_INFO);
            
            WebClient client = new WebClient();
            HtmlPage page = null;
            
            client.setCssEnabled(true);
            client.setJavaScriptEnabled(true);
            client.setAjaxController(new NicelyResynchronizingAjaxController());
            
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
        }
    }
}
