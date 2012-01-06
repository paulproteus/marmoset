package edu.umd.cs.buildServer.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class LoadAverage {

	static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();


	static public double getLoadAverage() {
		return operatingSystemMXBean.getSystemLoadAverage();
	}

	static public  double getWeightedLoadAverage() {
		return operatingSystemMXBean.getSystemLoadAverage()
		  / operatingSystemMXBean.getAvailableProcessors();
	}

	static public boolean isOverloaded() {
		return getWeightedLoadAverage() > 2.0;
	}
}

