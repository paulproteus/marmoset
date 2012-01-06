package edu.umd.cs.marmoset.utilities;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LenientDateParser {

	private static String[] dayFormats = { "yyyy-MM-dd", "MM/dd", "MMM dd", "dd MMM" };
	private static String[] timeFormats = { "hh:mm a", "hh:mm:ss a", "hh a", "hh:mma", "hha" , "HH:mm:ss", "HH:mm"};
	private static List<SimpleDateFormat> formats = new ArrayList<SimpleDateFormat>();
	static {
		for (String d : dayFormats) {

			for (String t : timeFormats) {
				formats.add(new SimpleDateFormat(d + " " + t));
				formats.add(new SimpleDateFormat(d + ", " + t));
				formats.add(new SimpleDateFormat(t + ", " + d));
			}
		}
	}

	private static Date parse0(String anyDate) {
		try {
			return new Date(Date.parse(anyDate));


		} catch (IllegalArgumentException e) {
			for(DateFormat f : formats) {
				try {
				return f.parse(anyDate);
				} catch (ParseException e2) {
				}
			}
			throw new IllegalArgumentException("Unable to parse " + anyDate);
		}

	}
	public static Date parse(String anyDate) {
		Date d = parse0(anyDate);
		if (d.getYear() == 70)
			d.setYear(new Date().getYear());
		return d;
	}

	public static void main(String args[]) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("MMM dd, hh:mm a");
		System.out.println(f.parse("Dec 26, 1:00 pm"));

		Date d = parse("12/26, 6:00pm");
		System.out.println(d);
		System.out.println(parse("12/26, 6pm"));


	}

}
