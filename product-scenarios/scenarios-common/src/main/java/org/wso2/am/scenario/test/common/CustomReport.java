package org.wso2.am.scenario.test.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;
import org.testng.reporters.HtmlHelper;
//new
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.reporters.util.StackTraceTools;
import org.testng.xml.XmlSuite;

public class CustomReport  extends TestListenerAdapter implements IReporter {
    private static final Logger L = Logger.getLogger(CustomReport.class);
    private PrintWriter m_out;
    private int m_row;
    private int m_methodIndex;
    private int m_rowTotal;

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                               String outputDirectory) {
        try {
            this.m_out = this.createWriter(outputDirectory);
        } catch (IOException var5) {
            L.error("output file", var5);
            return;
        }

        this.startHtml(this.m_out);
        this.generateSuiteSummaryReport(suites);
        this.generateMethodSummaryReport(suites);
        this.generateMethodDetailReport(suites);
        this.endHtml(this.m_out);
        this.m_out.flush();
        this.m_out.close();

    }

    protected PrintWriter createWriter(String outdir) throws IOException {
        (new File(outdir)).mkdirs();
        return new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, "emailable-report.html"))));
    }

    protected void generateMethodSummaryReport(List<ISuite> suites) {
        this.m_methodIndex = 0;
        this.m_out.println("<a id=\"summary\"></a>");
        this.startResultSummaryTable("passed");
        Iterator i$ = suites.iterator();

        while(i$.hasNext()) {
            ISuite suite = (ISuite)i$.next();
            if (suites.size() > 1) {
                this.titleRow(suite.getName(), 4);
            }

            Map<String, ISuiteResult> r = suite.getResults();
            i$ = r.values().iterator();

            while(i$.hasNext()) {
                ISuiteResult r2 = (ISuiteResult)i$.next();
                ITestContext testContext = r2.getTestContext();
                String testName = testContext.getName();
                this.resultSummary(testContext.getFailedConfigurations(), testName, "failed", " (configuration methods)");
                this.resultSummary(testContext.getFailedTests(), testName, "failed", "");
                this.resultSummary(testContext.getSkippedConfigurations(), testName, "skipped", " (configuration methods)");
                this.resultSummary(testContext.getSkippedTests(), testName, "skipped", "");
                this.resultSummary(testContext.getPassedTests(), testName, "passed", "");
            }
        }

        this.m_out.println("</table>");
    }

    protected void generateMethodDetailReport(List<ISuite> suites) {
        this.m_methodIndex = 0;
        Iterator i$ = suites.iterator();

        while(i$.hasNext()) {
            ISuite suite = (ISuite)i$.next();
            Map<String, ISuiteResult> r = suite.getResults();
            i$ = r.values().iterator();

            while(i$.hasNext()) {
                ISuiteResult r2 = (ISuiteResult)i$.next();
                ITestContext testContext = r2.getTestContext();
                if (r.values().size() > 0) {
                    this.m_out.println("<h1>" + testContext.getName() + "</h1>");
                }

                this.resultDetail(testContext.getFailedConfigurations(), "failed");
                this.resultDetail(testContext.getFailedTests(), "failed");
                this.resultDetail(testContext.getSkippedConfigurations(), "skipped");
                this.resultDetail(testContext.getSkippedTests(), "skipped");
                this.resultDetail(testContext.getPassedTests(), "passed");
            }
        }

    }

    private void resultSummary(IResultMap tests, String testname, String style, String details) {
        if (tests.getAllResults().size() > 0) {
            StringBuffer buff = new StringBuffer();
            String lastClassName = "";
            int mq = 0;
            int cq = 0;
            Iterator i$ = this.getMethodSet(tests).iterator();

            while(i$.hasNext()) {
                ITestNGMethod method = (ITestNGMethod)i$.next();
                ++this.m_row;
                ++this.m_methodIndex;
                ITestClass testClass = method.getTestClass();
                String className = testClass.getName();
                if (mq == 0) {
                    this.titleRow(testname + " &#8212; " + style + details, 4);
                }

                if (!className.equalsIgnoreCase(lastClassName)) {
                    if (mq > 0) {
                        ++cq;
                        this.m_out.println("<tr class=\"" + style + (cq % 2 == 0 ? "even" : "odd") + "\">" + "<td rowspan=\"" + mq + "\">" + lastClassName + buff);
                    }

                    mq = 0;
                    buff.setLength(0);
                    lastClassName = className;
                }

                Set<ITestResult> resultSet = tests.getResults(method);
                long end = -9223372036854775808L;
                long start = 9223372036854775807L;
                Iterator i$2 = tests.getResults(method).iterator();

                while(i$2.hasNext()) {
                    ITestResult testResult = (ITestResult)i$2.next();
                    if (testResult.getEndMillis() > end) {
                        end = testResult.getEndMillis();
                    }

                    if (testResult.getStartMillis() < start) {
                        start = testResult.getStartMillis();
                    }
                }

                ++mq;
                if (mq > 1) {
                    buff.append("<tr class=\"" + style + (cq % 2 == 0 ? "odd" : "even") + "\">");
                }

                String description = method.getDescription();
                String testInstanceName = ((ITestResult[])resultSet.toArray(new ITestResult[0]))[0].getTestName();
                buff.append("<td><a href=\"#m" + this.m_methodIndex + "\">" + this.qualifiedName(method) + " " + (description != null && description.length() > 0 ? "(\"" + description + "\")" : "") + "</a>" + "</td>" + "<td class=\"numi\">" + resultSet.size() + "</td><td class=\"numi\">" + (end - start) + "</td></tr>");
            }

            if (mq > 0) {
                ++cq;
                this.m_out.println("<tr class=\"" + style + (cq % 2 == 0 ? "even" : "odd") + "\">" + "<td rowspan=\"" + mq + "\">" + lastClassName + buff);
            }
        }

    }

    private void startResultSummaryTable(String style) {
        this.tableStart(style);
        this.m_out.println("<tr><th>Class</th><th>Method</th><th># of<br/>Scenarios</th><th>Time<br/>(Msecs)</th></tr>");
        this.m_row = 0;
    }

    private String qualifiedName(ITestNGMethod method) {
        StringBuilder addon = new StringBuilder();
        String[] groups = method.getGroups();
        int length = groups.length;
        if (length > 0 && !"basic".equalsIgnoreCase(groups[0])) {
            addon.append("(");

            for(int i = 0; i < length; ++i) {
                if (i > 0) {
                    addon.append(", ");
                }

                addon.append(groups[i]);
            }

            addon.append(")");
        }

        return "<b>" + method.getMethodName() + "</b> " + addon;
    }

    private void resultDetail(IResultMap tests, String style) {
        Iterator i$ = tests.getAllResults().iterator();

        while(i$.hasNext()) {
            ITestResult result = (ITestResult)i$.next();
            int row = 0;
            ITestNGMethod method = result.getMethod();
            int var9 = row + 1;
            ++this.m_methodIndex;
            String cname = method.getTestClass().getName();
            this.m_out.println("<a id=\"m" + this.m_methodIndex + "\"></a><h2>" + cname + ":" + method.getMethodName() + "</h2>");
            Set<ITestResult> resultSet = tests.getResults(method);
            this.generateForResult(result, method, resultSet.size());
            this.m_out.println("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
        }

    }

    private void generateForResult(ITestResult ans, ITestNGMethod method, int resultSetSize) {
        int rq = 0;
        rq = rq + 1;
        Object[] parameters = ans.getParameters();
        boolean hasParameters = parameters != null && parameters.length > 0;
        if (hasParameters) {
            if (rq == 1) {
                this.tableStart("param");
                this.m_out.print("<tr>");

                for(int x = 1; x <= parameters.length; ++x) {
                    this.m_out.print("<th style=\"padding-left:1em;padding-right:1em\">Parameter #" + x + "</th>");
                }

                this.m_out.println("</tr>");
            }

            this.m_out.print("<tr" + (rq % 2 == 0 ? " class=\"stripe\"" : "") + ">");
            Object[] arr$ = parameters;
            int len$ = parameters.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                Object p = arr$[i$];
                this.m_out.println("<td style=\"padding-left:.5em;padding-right:2em\">" + (p != null ? Utils.escapeHtml(p.toString()) : "null") + "</td>");
            }

            this.m_out.println("</tr>");
        }

        List<String> msgs = Reporter.getOutput(ans);
        boolean hasReporterOutput = msgs.size() > 0;
        Throwable exception = ans.getThrowable();
        boolean hasThrowable = exception != null;
        if (hasReporterOutput || hasThrowable) {
            String indent = " style=\"padding-left:3em\"";
            if (hasParameters) {
                this.m_out.println("<tr" + (rq % 2 == 0 ? " class=\"stripe\"" : "") + "><td" + indent + " colspan=\"" + parameters.length + "\">");
            } else {
                this.m_out.println("<div" + indent + ">");
            }

            if (hasReporterOutput) {
                if (hasThrowable) {
                    this.m_out.println("<h3>Test Messages</h3>");
                }

                Iterator i$ = msgs.iterator();

                while(i$.hasNext()) {
                    String line = (String)i$.next();
                    this.m_out.println(line + "<br/>");
                }
            }

            if (hasThrowable) {
                boolean wantsMinimalOutput = ans.getStatus() == 1;
                if (hasReporterOutput) {
                    this.m_out.println("<h3>" + (wantsMinimalOutput ? "Expected Exception" : "Failure") + "</h3>");
                }

                this.generateExceptionReport(exception, method);
            }

            if (hasParameters) {
                this.m_out.println("</td></tr>");
            } else {
                this.m_out.println("</div>");
            }
        }

        if (hasParameters && rq == resultSetSize) {
            this.m_out.println("</table>");
        }

    }

    protected void generateExceptionReport(Throwable exception, ITestNGMethod method) {
        this.generateExceptionReport(exception, method, exception.getLocalizedMessage());
    }

    private void generateExceptionReport(Throwable exception, ITestNGMethod method, String title) {
        this.m_out.println("<p>" + Utils.escapeHtml(title) + "</p>");
        StackTraceElement[] s1 = exception.getStackTrace();
        Throwable t2 = exception.getCause();
        if (t2 == exception) {
            t2 = null;
        }

        int maxlines = Math.min(100, StackTraceTools.getTestRoot(s1, method));

        for(int x = 0; x <= maxlines; ++x) {
            this.m_out.println((x > 0 ? "<br/>at " : "") + Utils.escapeHtml(s1[x].toString()));
        }

        if (maxlines < s1.length) {
            this.m_out.println("<br/>" + (s1.length - maxlines) + " lines not shown");
        }

        if (t2 != null) {
            this.generateExceptionReport(t2, method, "Caused by " + t2.getLocalizedMessage());
        }

    }

    private Collection<ITestNGMethod> getMethodSet(IResultMap tests) {
        List<ITestNGMethod> r = new ArrayList(tests.getAllMethods());
        Arrays.sort(r.toArray(new ITestNGMethod[r.size()]), new CustomReport.TestSorter());
        return r;
    }

    public void generateSuiteSummaryReport(List<ISuite> suites) {
        this.tableStart("param");
        this.m_out.print("<tr><th>Test</th>");
        this.tableColumnStart("Methods<br/>Passed");
        this.tableColumnStart("Scenarios<br/>Passed");
        this.tableColumnStart("# skipped");
        this.tableColumnStart("# failed");
        this.tableColumnStart("Total<br/>Time");
        this.tableColumnStart("Included<br/>Groups");
        this.tableColumnStart("Excluded<br/>Groups");
        this.m_out.println("</tr>");
        NumberFormat formatter = new DecimalFormat("#,##0.0");
        int qty_tests = 0;
        int qty_pass_m = 0;
        int qty_pass_s = 0;
        int qty_skip = 0;
        int qty_fail = 0;
        long time_start = 9223372036854775807L;
        long time_end = -9223372036854775808L;
        Iterator i$ = suites.iterator();

        while(i$.hasNext()) {
            ISuite suite = (ISuite)i$.next();
            if (suites.size() > 1) {
                this.titleRow(suite.getName(), 7);
            }

            Map<String, ISuiteResult> tests = suite.getResults();
            i$ = tests.values().iterator();

            while(i$.hasNext()) {
                ISuiteResult r = (ISuiteResult)i$.next();
                ++qty_tests;
                ITestContext overview = r.getTestContext();
                this.startSummaryRow(overview.getName());
                int q = this.getMethodSet(overview.getPassedTests()).size();
                qty_pass_m += q;
                this.summaryCell(q, 2147483647);
                q = overview.getPassedTests().size();
                qty_pass_s += q;
                this.summaryCell(q, 2147483647);
                q = this.getMethodSet(overview.getSkippedTests()).size();
                qty_skip += q;
                this.summaryCell(q, 0);
                q = this.getMethodSet(overview.getFailedTests()).size();
                qty_fail += q;
                this.summaryCell(q, 0);
                time_start = Math.min(overview.getStartDate().getTime(), time_start);
                time_end = Math.max(overview.getEndDate().getTime(), time_end);
                this.summaryCell(formatter.format((double)(overview.getEndDate().getTime() - overview.getStartDate().getTime()) / 1000.0D) + " seconds", true);
                this.summaryCell(overview.getIncludedGroups());
                this.summaryCell(overview.getExcludedGroups());
                this.m_out.println("</tr>");
            }
        }

        if (qty_tests > 1) {
            this.m_out.println("<tr class=\"total\"><td>Total</td>");
            this.summaryCell(qty_pass_m, 2147483647);
            this.summaryCell(qty_pass_s, 2147483647);
            this.summaryCell(qty_skip, 0);
            this.summaryCell(qty_fail, 0);
            this.summaryCell(formatter.format((double)(time_end - time_start) / 1000.0D) + " seconds", true);
            this.m_out.println("<td colspan=\"2\">&nbsp;</td></tr>");
        }

        this.m_out.println("</table>");
    }

    private void summaryCell(String[] val) {
        StringBuffer b = new StringBuffer();
        String[] arr$ = val;
        int len$ = val.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String v = arr$[i$];
            b.append(v + " ");
        }

        this.summaryCell(b.toString(), true);
    }

    private void summaryCell(String v, boolean isgood) {
        this.m_out.print("<td class=\"numi" + (isgood ? "" : "_attn") + "\">" + v + "</td>");
    }

    private void startSummaryRow(String label) {
        ++this.m_row;
        this.m_out.print("<tr" + (this.m_row % 2 == 0 ? " class=\"stripe\"" : "") + "><td style=\"text-align:left;padding-right:2em\">" + label + "</td>");
    }

    private void summaryCell(int v, int maxexpected) {
        this.summaryCell(String.valueOf(v), v <= maxexpected);
        this.m_rowTotal += v;
    }

    private void tableStart(String cssclass) {
        this.m_out.println("<table cellspacing=0 cellpadding=0" + (cssclass != null ? " class=\"" + cssclass + "\"" : " style=\"padding-bottom:2em\"") + ">");
        this.m_row = 0;
    }

    private void tableColumnStart(String label) {
        this.m_out.print("<th class=\"numi\">" + label + "</th>");
    }

    private void titleRow(String label, int cq) {
        this.m_out.println("<tr><th colspan=\"" + cq + "\">" + label + "</th></tr>");
        this.m_row = 0;
    }

    protected void writeStyle(String[] formats, String[] targets) {
    }

    protected void startHtml(PrintWriter out) {
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        out.println("<head>");
        out.println("<title>TestNG:  Unit Test</title>");
        out.println("<style type=\"text/css\">");
        out.println("table caption,table.info_table,table.param,table.passed,table.failed {margin-bottom:10px;border:1px solid #000099;border-collapse:collapse;empty-cells:show;}");
        out.println("table.info_table td,table.info_table th,table.param td,table.param th,table.passed td,table.passed th,table.failed td,table.failed th {");
        out.println("border:1px solid #000099;padding:.25em .5em .25em .5em");
        out.println("}");
        out.println("table.param th {vertical-align:bottom}");
        out.println("td.numi,th.numi,td.numi_attn {");
        out.println("text-align:right");
        out.println("}");
        out.println("tr.total td {font-weight:bold}");
        out.println("table caption {");
        out.println("text-align:center;font-weight:bold;");
        out.println("}");
        out.println("table.passed tr.stripe td,table tr.passedodd td {background-color: #00AA00;}");
        out.println("table.passed td,table tr.passedeven td {background-color: #33FF33;}");
        out.println("table.passed tr.stripe td,table tr.skippedodd td {background-color: #cccccc;}");
        out.println("table.passed td,table tr.skippedodd td {background-color: #dddddd;}");
        out.println("table.failed tr.stripe td,table tr.failedodd td,table.param td.numi_attn {background-color: #FF3333;}");
        out.println("table.failed td,table tr.failedeven td,table.param tr.stripe td.numi_attn {background-color: #DD0000;}");
        out.println("tr.stripe td,tr.stripe th {background-color: #E6EBF9;}");
        out.println("p.totop {font-size:85%;text-align:center;border-bottom:2px black solid}");
        out.println("div.shootout {padding:2em;border:3px #4854A8 solid}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
    }

    protected void endHtml(PrintWriter out) {
        out.println("</body></html>");
    }

    private class TestSorter implements Comparator<ITestNGMethod> {
        private TestSorter() {
        }

        public int compare(ITestNGMethod o1, ITestNGMethod o2) {
            return (int)(o1.getDate() - o2.getDate());
        }
    }

    private static final Comparator<ITestResult> NAME_COMPARATOR = new CustomReport.NameComparator();
    private static final Comparator<ITestResult> CONFIGURATION_COMPARATOR = new CustomReport.ConfigurationComparator();
    private ITestContext m_testContext = null;
    private static String HEAD = "\n<style type=\"text/css\">\n.log { display: none;} \n.stack-trace { display: none;} \n</style>\n<script type=\"text/javascript\">\n<!--\nfunction flip(e) {\n  current = e.style.display;\n  if (current == 'block') {\n    e.style.display = 'none';\n    return 0;\n  }\n  else {\n    e.style.display = 'block';\n    return 1;\n  }\n}\n\nfunction toggleBox(szDivId, elem, msg1, msg2)\n{\n  var res = -1;  if (document.getElementById) {\n    res = flip(document.getElementById(szDivId));\n  }\n  else if (document.all) {\n    // this is the way old msie versions work\n    res = flip(document.all[szDivId]);\n  }\n  if(elem) {\n    if(res == 0) elem.innerHTML = msg1; else elem.innerHTML = msg2;\n  }\n\n}\n\nfunction toggleAllBoxes() {\n  if (document.getElementsByTagName) {\n    d = document.getElementsByTagName('div');\n    for (i = 0; i < d.length; i++) {\n      if (d[i].className == 'log') {\n        flip(d[i]);\n      }\n    }\n  }\n}\n\n// -->\n</script>\n\n";

    public CustomReport() {
    }

    public void onStart(ITestContext context) {
        this.m_testContext = context;
    }

    public void onFinish(ITestContext context) {
        generateLog(this.m_testContext, (String)null, this.m_testContext.getOutputDirectory(), this.getConfigurationFailures(), this.getConfigurationSkips(), this.getPassedTests(), this.getFailedTests(), this.getSkippedTests(), this.getFailedButWithinSuccessPercentageTests());
    }

    private static String getOutputFile(ITestContext context) {
        return context.getName() + ".html";
    }

    public static void generateTable(StringBuffer sb, String title, Collection<ITestResult> tests, String cssClass, Comparator<ITestResult> comparator) {
        sb.append("<table width='100%' border='1' class='invocation-").append(cssClass).append("'>\n").append("<tr><td colspan='4' align='center'><b>").append(title).append("</b></td></tr>\n").append("<tr>").append("<td><b>Test method</b></td>\n").append("<td width=\"30%\"><b>Exception</b></td>\n").append("<td width=\"10%\"><b>Time (seconds)</b></td>\n").append("<td><b>Instance</b></td>\n").append("</tr>\n");
        if (tests instanceof List) {
            Collections.sort((List)tests, comparator);
        }

        String id = "";
        Throwable tw = null;
        Iterator i$ = tests.iterator();

        while(i$.hasNext()) {
            ITestResult tr = (ITestResult)i$.next();
            sb.append("<tr>\n");
            ITestNGMethod method = tr.getMethod();
            sb.append("<td title='").append(tr.getTestClass().getName()).append(".").append(tr.getName()).append("()'>").append("<b>").append(tr.getName()).append("</b>");
            String testClass = tr.getTestClass().getName();
            if (testClass != null) {
                sb.append("<br>").append("Test class: " + testClass);
                String testName = tr.getTestName();
                if (testName != null) {
//                    sb.append(" (").append(testName).append(")");
                }
            }

            if (!Utils.isStringEmpty(method.getDescription())) {
                sb.append("<br>").append("Test method: ").append(method.getDescription());
            }

            Object[] parameters = tr.getParameters();
            if (parameters != null && parameters.length > 0) {
                sb.append("<br>Parameters: ");

                for(int j = 0; j < parameters.length; ++j) {
                    if (j > 0) {
                        sb.append(", ");
                    }

                    sb.append(parameters[j] == null ? "null" : parameters[j].toString());
                }
            }

            List<String> output = Reporter.getOutput(tr);
            String fullStackTrace;
            if (null != output && output.size() > 0) {
                sb.append("<br/>");
                fullStackTrace = "Output-" + tr.hashCode();
                sb.append("\n<a href=\"#").append(fullStackTrace).append("\"").append(" onClick='toggleBox(\"").append(fullStackTrace).append("\", this, \"Show output\", \"Hide output\");'>").append("Show output</a>\n").append("\n<a href=\"#").append(fullStackTrace).append("\"").append(" onClick=\"toggleAllBoxes();\">Show all outputs</a>\n");
                sb.append("<div class='log' id=\"").append(fullStackTrace).append("\">\n");
                i$ = output.iterator();

                while(i$.hasNext()) {
                    String s = (String)i$.next();
                    sb.append(s).append("<br/>\n");
                }

                sb.append("</div>\n");
            }

            sb.append("</td>\n");
            tw = tr.getThrowable();
            String stackTrace = "";
            fullStackTrace = "";
            id = "stack-trace" + tr.hashCode();
            sb.append("<td>");
            if (null != tw) {
                String[] stackTraces = Utils.stackTrace(tw, true);
                fullStackTrace = stackTraces[1];
                stackTrace = "<div><pre>" + stackTraces[0] + "</pre></div>";
                sb.append(stackTrace);
                sb.append("<a href='#' onClick='toggleBox(\"").append(id).append("\", this, \"Click to show all stack frames\", \"Click to hide stack frames\")'>").append("Click to show all stack frames").append("</a>\n").append("<div class='stack-trace' id='" + id + "'>").append("<pre>" + fullStackTrace + "</pre>").append("</div>");
            }

            sb.append("</td>\n");
            long time = (tr.getEndMillis() - tr.getStartMillis()) / 1000L;
            String strTime = Long.toString(time);
            sb.append("<td>").append(strTime).append("</td>\n");
            Object instance = tr.getInstance();
            sb.append("<td>").append(instance).append("</td>");
            sb.append("</tr>\n");
        }

        sb.append("</table><p>\n");
    }

    private static String arrayToString(String[] array) {
        StringBuffer result = new StringBuffer("");
        String[] arr$ = array;
        int len$ = array.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String element = arr$[i$];
            result.append(element).append(" ");
        }

        return result.toString();
    }

    public static void generateLog(ITestContext testContext, String host, String outputDirectory, Collection<ITestResult> failedConfs, Collection<ITestResult> skippedConfs, Collection<ITestResult> passedTests, Collection<ITestResult> failedTests, Collection<ITestResult> skippedTests, Collection<ITestResult> percentageTests) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>\n<head>\n").append("<title>TestNG:  ").append(testContext.getName()).append("</title>\n").append(HtmlHelper.getCssString()).append(HEAD).append("</head>\n").append("<body>\n");
        Date startDate = testContext.getStartDate();
        Date endDate = testContext.getEndDate();
        long duration = (endDate.getTime() - startDate.getTime()) / 1000L;
        int passed = testContext.getPassedTests().size() + testContext.getFailedButWithinSuccessPercentageTests().size();
        int failed = testContext.getFailedTests().size();
        int skipped = testContext.getSkippedTests().size();
        String hostLine = Utils.isStringEmpty(host) ? "" : "<tr><td>Remote host:</td><td>" + host + "</td>\n</tr>";
        sb.append("<h2 align='center'>").append(testContext.getName()).append("</h2>").append("<table border='1' align=\"center\">\n").append("<tr>\n").append("<td>Tests passed/Failed/Skipped:</td><td>").append(passed).append("/").append(failed).append("/").append(skipped).append("</td>\n").append("</tr><tr>\n").append("<td>Started on:</td><td>").append(testContext.getStartDate().toString()).append("</td>\n").append("</tr>\n").append(hostLine).append("<tr><td>Total time:</td><td>").append(duration).append(" seconds (").append(endDate.getTime() - startDate.getTime()).append(" ms)</td>\n").append("</tr><tr>\n").append("<td>Included groups:</td><td>").append(arrayToString(testContext.getIncludedGroups())).append("</td>\n").append("</tr><tr>\n").append("<td>Excluded groups:</td><td>").append(arrayToString(testContext.getExcludedGroups())).append("</td>\n").append("</tr>\n").append("</table><p/>\n");
        sb.append("<small><i>(Hover the method name to see the test class name)</i></small><p/>\n");
        if (failedConfs.size() > 0) {
            generateTable(sb, "FAILED CONFIGURATIONS", failedConfs, "failed", CONFIGURATION_COMPARATOR);
        }

        if (skippedConfs.size() > 0) {
            generateTable(sb, "SKIPPED CONFIGURATIONS", skippedConfs, "skipped", CONFIGURATION_COMPARATOR);
        }

        if (failedTests.size() > 0) {
            generateTable(sb, "FAILED TESTS", failedTests, "failed", NAME_COMPARATOR);
        }

        if (percentageTests.size() > 0) {
            generateTable(sb, "FAILED TESTS BUT WITHIN SUCCESS PERCENTAGE", percentageTests, "percent", NAME_COMPARATOR);
        }

        if (passedTests.size() > 0) {
            generateTable(sb, "PASSED TESTS", passedTests, "passed", NAME_COMPARATOR);
        }

        if (skippedTests.size() > 0) {
            generateTable(sb, "SKIPPED TESTS", skippedTests, "skipped", NAME_COMPARATOR);
        }

        sb.append("</body>\n</html>");
        Utils.writeFile(outputDirectory, getOutputFile(testContext), sb.toString());
    }

    private static void ppp(String s) {
        System.out.println("[CustomReport] " + s);
    }

    private static class ConfigurationComparator implements Comparator<ITestResult>, Serializable {
        private static final long serialVersionUID = 5558550850685483455L;

        private ConfigurationComparator() {
        }

        public int compare(ITestResult o1, ITestResult o2) {
            ITestNGMethod tm1 = o1.getMethod();
            ITestNGMethod tm2 = o2.getMethod();
            return annotationValue(tm2) - annotationValue(tm1);
        }

        private static int annotationValue(ITestNGMethod method) {
            if (method.isBeforeSuiteConfiguration()) {
                return 10;
            } else if (method.isBeforeTestConfiguration()) {
                return 9;
            } else if (method.isBeforeClassConfiguration()) {
                return 8;
            } else if (method.isBeforeGroupsConfiguration()) {
                return 7;
            } else if (method.isBeforeMethodConfiguration()) {
                return 6;
            } else if (method.isAfterMethodConfiguration()) {
                return 5;
            } else if (method.isAfterGroupsConfiguration()) {
                return 4;
            } else if (method.isAfterClassConfiguration()) {
                return 3;
            } else if (method.isAfterTestConfiguration()) {
                return 2;
            } else {
                return method.isAfterSuiteConfiguration() ? 1 : 0;
            }
        }
    }

    private static class NameComparator implements Comparator<ITestResult>, Serializable {
        private static final long serialVersionUID = 381775815838366907L;

        private NameComparator() {
        }

        public int compare(ITestResult o1, ITestResult o2) {
            String c1 = o1.getMethod().getMethodName();
            String c2 = o2.getMethod().getMethodName();
            return c1.compareTo(c2);
        }
    }
}