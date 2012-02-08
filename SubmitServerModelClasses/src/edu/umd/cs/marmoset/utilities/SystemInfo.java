package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashSet;
import java.util.List;

import javax.annotation.CheckForNull;

import com.sun.management.UnixOperatingSystemMXBean;

public class SystemInfo {

	static final int MEGABYTE = 1024 * 1024;

	static OperatingSystemMXBean osBean = ManagementFactory
			.getOperatingSystemMXBean();
	static @CheckForNull
	com.sun.management.OperatingSystemMXBean sunOsBean = osBean instanceof com.sun.management.OperatingSystemMXBean ? ((com.sun.management.OperatingSystemMXBean) osBean)
			: null;
	static @CheckForNull com.sun.management.UnixOperatingSystemMXBean unixBean =
	       (sunOsBean instanceof UnixOperatingSystemMXBean ?   (UnixOperatingSystemMXBean) sunOsBean : null);

	public static double getLoadAverage() {
		return osBean.getSystemLoadAverage();
	}

	public static boolean hasExtended() {
		return sunOsBean != null;
	}

	public static long getSystemMemory() {
		if (sunOsBean != null)
			return sunOsBean.getTotalPhysicalMemorySize();
		return 0;
	}

	   public static String getSystemLoad() {
	       return getSystemLoad(false);
	   }
	   
	static int mb(long bytes) {
	    long result = bytes/MEGABYTE;
	    if (result > Integer.MAX_VALUE)
	        throw new IllegalArgumentException();
	    return (int) result;
	}
	
	public static void getFreeDiskSpace(PrintWriter out,  boolean verbose) {
	    try {
	    ProcessBuilder b = new ProcessBuilder("/bin/df", ".", "/tmp");
	    Process p = b.start();
	    p.getOutputStream().close();
	    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    in.readLine();
	    HashSet<String> seen = new HashSet<String>();
	    while(true) {
	        String s = in.readLine();
	        if (s == null) break;
	        String fields []= s.split("[\t ]+");
	        String fileSystem = fields[0];
	        if (!seen.add(fileSystem))
	            continue;
	        String free = fields[3];
            long freeSpace = Long.parseLong(free);
	        if (verbose || freeSpace < 1000000) 
	                out.printf("%s %d MBytes free, ", fields[0], freeSpace/1024);
	        }
	    in.close();
	    p.getErrorStream().close();
	    p.destroy();
	    } catch (Exception e) {
	        assert true;
	    }
	    
	}
	public static String getSystemLoad(boolean verbose) {
		Runtime runtime = Runtime.getRuntime();
		
		StringWriter w = new StringWriter();
		PrintWriter out = new PrintWriter(w);
		
		double loadAverage = getLoadAverage();
		if (loadAverage > 2.0)
		    out.printf("Load average %.1f, ", loadAverage);
		
        int freeMemory = mb(runtime.freeMemory());
        int totalMemory = mb(runtime.totalMemory());
        int maxMemory = mb(runtime.maxMemory() );
        if (verbose || totalMemory - freeMemory > maxMemory/2)
            out.printf("memory %d/%d/%d, ", freeMemory, totalMemory, maxMemory);
        
        if (unixBean != null) {
            long openFD = unixBean.getOpenFileDescriptorCount();
            long maxFD = unixBean.getMaxFileDescriptorCount() ;
            if (verbose || openFD > maxFD/2 || openFD > 300)
                out.printf("fd %d/%d, ", openFD, maxFD);
        }
        
        List<MemoryPoolMXBean> mlist = ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean mb : mlist) {
            MemoryUsage usage = mb.getUsage();
            int current = mb(usage.getUsed());
            int max = mb(usage.getMax());
            
            if (verbose || current > max/2+4)
                out.printf("%s %d/%d, ", mb.getName(), current, max);
        }   
        
        getFreeDiskSpace(out, verbose);
        out.close();
        String s =  w.toString();
        if (s.isEmpty())
            return " good ";
        return s.substring(0, s.length() -2);
	}
	
	public static void main(String args[]) {
	    System.out.println(getSystemLoad(false));
        
	    System.out.println(getSystemLoad(true));
		  List<MemoryPoolMXBean> mlist = ManagementFactory.getMemoryPoolMXBeans();

		for(MemoryPoolMXBean mb : mlist) {
			System.out.println(mb.getName());
			System.out.println(mb.getType());
			MemoryUsage usage = mb.getUsage();
            System.out.println(usage);			
		}
	}

}
