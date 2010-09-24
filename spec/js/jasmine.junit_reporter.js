(function() {
    
    if (! jasmine) {
        throw new Exception("jasmine library does not exist in global namespace!");
    }
    
    function elapsed(startTime, endTime) {
        return (endTime - startTime)/1000;
    }
    
    function ISODateString(d) {
        function pad(n) { return n < 10 ? '0'+n : n; }
        
        return d.getFullYear() + '-'
            + pad(d.getMonth()+1) +'-'
            + pad(d.getDate()) + 'T'
            + pad(d.getHours()) + ':'
            + pad(d.getMinutes()) + ':'
            + pad(d.getSeconds());
    }
    
    /**
     * Generates JUnit XML for the given spec run.
     * Allows the test results to be used in java based CI
     * systems like CruiseControl and Hudson.
     */
    var JUnitXmlReporter = function() {
        this.started = false;
        this.finished = false;
        this.reports = [];
    };
    
    JUnitXmlReporter.prototype = {
        reportRunnerResults: function(runner) {
            this.finished = true;
        },
        
        reportRunnerStarting: function(runner) {
            this.started = true;
        },
        
        reportSpecResults: function(spec) {
            var resultText = "Failed.";
            
            if (spec.results().passed()) {
                resultText = "Passed.";
            }
            
            spec.endTime = new Date();
        },
        
        reportSpecStarting: function(spec) {
            spec.startTime = new Date();
            
            if (! spec.suite.startTime) {
                spec.suite.startTime = new Date();
            }
        },
        
        reportSuiteResults: function(suite) {
            var output = [],
                fileName = 'TEST-' + suite.description.replace(/\s/g, '') + '.xml',
                results = suite.results(),
                items = results.getItems(),
                item,
                spec,
                expectedResults,
                trace,
                i,
                j;
                
            suite.endTime = new Date();
            
            output.push('<?xml version="1.0" encoding="UTF-8" ?>');
            output.push('<testsuite name="' + suite.description + '" errors="0" failures="' 
                + results.failedCount + '" tests="' + results.totalCount + '" time="' 
                + elapsed(suite.startTime, suite.endTime) + '" timestamp="' + ISODateString(suite.startTime) + '">');
            
            for (i = 0; i < items.length; i++) {
                item = items[i];
                spec = suite.specs()[i];
                
                output.push(' <testcase classname="' + suite.description + '" name="' 
                    + item.description + '" time="' + elapsed(spec.startTime, spec.endTime) + '">');
                
                if (!item.passed()) {
                    expectedResults = item.getItems();
                    
                    for (j = 0; j < expectedResults.length; j++) {
                        trace = expectedResults[j].trace;
                        
                        if (trace instanceof Error) {
                            output.push('<failure>' + trace.message + '</failure>');
                            break;
                        }
                    }
                }
                
                output.push('</testcase>');
            }
            
            output.push('</testsuite>');
            
            this.reports.push({
                "name": suite.description,
                "results": {
                    "passed": results.passedCount,
                    "failed": results.failedCount,
                    "total": results.totalCount
                },
                "filename": 'TEST-' + suite.description.replace(/\s/g, '') + '.xml',
                "text": output.join('')
            });
        },
        
        log: function(str) {
            var console = jasmine.getGlobal().console;
            
            if (console && console.log) {
                console.log(str);
            }
        }
    };
    
    // export public
    jasmine.JUnitXmlReporter = JUnitXmlReporter;
})();