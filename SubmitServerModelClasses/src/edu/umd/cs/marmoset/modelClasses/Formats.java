/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package edu.umd.cs.marmoset.modelClasses;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author pugh
 *
 */
public class Formats {
	public static final long MILLIS_PER_HOUR = 60L*60L*1000L;
    private static final DateFormat date= new SimpleDateFormat("EEE, MMM d, h:mm a");
    private static final DateFormat shortDate= new SimpleDateFormat("EEE, h a");


    public static synchronized String dateFormat(Date d) {
    	return date.format(d);
    }
    public static synchronized String shortDateFormat(Date d) {
    	return shortDate.format(d);
    }


   public static final NumberFormat twoDigitInt = new DecimalFormat("00");
    public static final long  MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
}
