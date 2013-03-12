package edu.umd.cs.buildServer.builder;

import java.io.File;
import java.lang.reflect.Method;

public class Clover {

    static Class<?> cloverInstr;
    static Method cloverInstrMainImpl, xmlReporterRunReport, cloverUtilsScrubCoverageData;

    static {
        boolean a = false;

        try {
            cloverInstr = Class.forName("com.cenqua.clover.CloverInstr");
            cloverInstrMainImpl = cloverInstr.getDeclaredMethod("mainImpl", String[].class);
            Class<?> cloverUtils = Class.forName("com.cenqua.clover.util.CloverUtils");
            cloverUtilsScrubCoverageData = cloverUtils.getDeclaredMethod("scrubCoverageData", String.class, Boolean.TYPE);
            Class<?> xmlReporter = Class.forName("com.cenqua.clover.reporters.xml.XMLReporter");
            xmlReporterRunReport = xmlReporter.getDeclaredMethod("runReport", String[].class);
            a = true;
        } catch (Exception e) {
            
        }
        available = a;

    }
    static final boolean available;

    public static boolean isAvailable() {
        return available;

    }

    public static File getCloverJar() {
        return JavaBuilder.getCodeBase(cloverInstr);
    }

    public static int cloverInstrMainImpl(String[] cliArgs) {
        try {
            return (Integer) cloverInstrMainImpl.invoke(null, (Object) cliArgs);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int xmlReporterRunReport(String[] cliArgs) {
        try {
            return (Integer) xmlReporterRunReport.invoke(null, (Object) cliArgs);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void cloverUtilsScrubCoverageData(String requiredProperty, boolean b) {
        try {
            cloverUtilsScrubCoverageData.invoke(null, requiredProperty, b);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
