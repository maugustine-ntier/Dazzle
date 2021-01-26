package za.co.ntier.common;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.util.DB;

/**
 * This class provides date manipulation methods.
 *
 * @version $Id: DateUtils.java,v 1.1 2012-09-04 08:23:03 maugustine Exp $
 * 
 *          Original author: maugustine
 * 
 * @author neil gordon 2015/01/12 Renamed from DateUtils to NTierDateUtils
 *         2015/09/21: Renamed from NTierDateUtils to ICEDateUtil
 *         2016/08/29: Renamed from ICEDateUtil to NTierDateUtil
 */
public class NTierDateUtil {

	public static final Timestamp NULL_DATE = getNullDate();
	
	// Date to Calendar
	static Calendar dateToCal(Date date) {
		Format formatter = new SimpleDateFormat("yyyy");
		String year = formatter.format(date);
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = new GregorianCalendar();
		cal2.setTime(date);
		cal2.get(Calendar.MONTH);
		cal.set(Integer.parseInt(year.toString()), cal2.get(Calendar.MONTH),
				cal2.get(Calendar.DATE));
		return cal;
	}

	// Calendar to Date
	public static Date calToDate(Calendar cal) {
		Date date = cal.getTime();
		return date;
	}

	// Calendar to Timestamp
	public static Timestamp calToTstamp(Calendar cal) {
		Timestamp tstamp = new Timestamp(cal.getTimeInMillis());
		return tstamp;
	}

	// Calendar addition
	public static Calendar add(Date date, int field, int amount) {
		Calendar cal = dateToCal(date);
		cal.add(field, amount);
		return cal;
	}

	public static Timestamp addDays(Date date, int daysToAdd) {
		return calToTstamp(add(date,
				Calendar.DATE, daysToAdd));
	}
	
	public static Timestamp addMonth(Date date, int monthsToAdd) {
		return calToTstamp(add(date,
				Calendar.MONTH, monthsToAdd));
	}
	
	public static Timestamp addYear(Date date, int yearsToAdd) {
		return calToTstamp(add(date,
				Calendar.YEAR, yearsToAdd));
	}
	
	// Calendar subtraction
	public static Calendar subtract(Date date, int field, int amount) {
		Calendar cal = dateToCal(date);
		amount *= -1;
		cal.add(field, amount);
		return cal;
	}

	// get number of working days between two dates
	public static int getNumDays(Date dstart, Date dend, String tabname,
			String keycolumn, String recordid) {
		Format formatter = new SimpleDateFormat("yyyy/MM/dd");
		String d1 = formatter.format(dstart);
		String d2 = formatter.format(dend);
		System.out.println("Formatted d1: " + d1 + "	and d2: " + d2);

		String sql = "SELECT '" + d2 + "'::Date - '" + d1 + "'::Date - "
				+ " count_weekend_days('" + d1 + "'::Date,'" + d2
				+ "'::Date ) + 1" + " FROM " + tabname + " WHERE " + tabname
				+ "." + keycolumn + " = " + recordid;

		System.out.println("SQL QUERY: " + sql);

		int retvalue = 0;
		try {
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retvalue = rs.getInt(1);
			rs.close();
			pstmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retvalue;

	}

	// Get number of days between two dates inclusive of the first and last
	// dates
	public static int getDays(Date dstart, Date dend) {
		long diff = dend.getTime() - dstart.getTime();
		long multiplicand = 1000 * 60 * 60 * 24;
		BigDecimal days = new BigDecimal(diff);
		days = days.divide(new BigDecimal(multiplicand), 0,
				BigDecimal.ROUND_CEILING);
		// days = days.setScale(0, BigDecimal.ROUND_CEILING);
		return days.intValue() + 1;
	}

	// NCG Provide a simple string representation
	public static String toStringFormatYMD(Timestamp time) {
		if (time == null)
			return " (null) ";
		return toStringFormat(time, "yyyy/MM/dd");
	}

	// NCG Format using specified format string
	public static String toStringFormat(Timestamp time, String format) {
		if (time == null)
			return " (null) ";
		return new SimpleDateFormat(format).format(time).toString();
	}

	// NCG Current time
	public static Timestamp getCurrentTime() {
		java.util.Date date = new java.util.Date();
		return new Timestamp(date.getTime());
	}

	// NCG Current time
	public static Timestamp getCurrentDate() {
		java.util.Date date = new java.util.Date();
		return truncateTime(new Timestamp(date.getTime()));
	}

	// NCG
	private static Timestamp getNullDate() {
		return getTimestamp(1900, 1, 1);
	}

	// simba getting Year from given timestamp
	public static int getYear(Timestamp time) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time.getTime());
		return cal.get(Calendar.YEAR);

	}

	/**
	 * Remove the time portion of a Timestamp NCG
	 */
	public static Timestamp truncateTime(Timestamp ts) {
		Calendar cal = Calendar.getInstance(); // locale-specific
		cal.setTime(ts);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long time = cal.getTimeInMillis();
		return new Timestamp(time);
	}

	/**
	 * @author ngordon Get timestamp
	 */
	public static Timestamp getTimestamp(int year, int month, int day) {
		return getTimestamp(year, month, day, 0, 0, 0, 0);
	}

	/**
	 * @author ngordon Get timestamp
	 */
	public static Timestamp getTimestamp(int year, int month, int day,
			int hour, int minute, int second, int millisecond) {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millisecond);

		// now convert GregorianCalendar object to Timestamp object
		return new Timestamp(cal.getTimeInMillis());
	}

	/**
	 * @author ngordon Get timestamp
	 */
	public static Timestamp setYear(int year, int month, int day, int hour,
			int minute, int second, int millisecond) {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millisecond);

		// now convert GregorianCalendar object to Timestamp object
		return new Timestamp(cal.getTimeInMillis());
	}

	/**
	 * @author ngordon Coalesce function
	 */
	public static Timestamp getLatest(Timestamp... items) {
		Timestamp latest = null;
		for (Timestamp i : items) {
			if (i == null)
				continue;
			if (latest == null) {
				latest = i;
				continue;
			}
			if (i.after(latest)) {
				latest = i;
			}
		}
		return latest;
	}

	public static Timestamp parse(String s1, String fmtStr) {
		DateFormat format = new SimpleDateFormat(fmtStr);
		// DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		// DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			return new java.sql.Timestamp(format.parse(s1).getTime());
		} catch (Exception e) {
			throw new AdempiereException(e);
		}
	}

	public static void main(String[] args) {
		System.out.println("Days between 01/01/2010 and 01/02/2010: "
				+ getDays(new Date(),
						calToDate(add(new Date(), Calendar.DAY_OF_YEAR, 5))));
		System.out.println("Days between 01/01/2010 add one month: "
				+ add(new Date(), Calendar.DAY_OF_MONTH, 1).getTime());
//		System.out.println(String.format("Add 6 months to today's date: %s",
//				ICEDateUtil.calToTstamp(ICEDateUtil.add(new Date(),
//						Calendar.MONTH, 6))));
//		System.out.println(String.format("Current date simple format: %s",
//				toStringFormatYMD(ICEDateUtil.getCurrentTime())));
//		System.out.println(String.format("Current time: %s", ICEDateUtil
//				.getCurrentTime().toString()));

		System.out.println("Days between 01/01/2010 and 03/01/2010: "
				+ getDays(new Date(2010, 1, 1), new Date(2010, 1, 3)));

		System.out.println("Days between 01/01/2010 and 03/01/2010: "
				+ getDays(new Date(2010, 1, 1), new Date(2010, 1, 3)));

		java.util.Date date = new java.util.Date();
		Timestamp now = new Timestamp(date.getTime());
		String format = new SimpleDateFormat("yyyyMMdd_hhmmss").format(now);
		System.out.println(String.format("%s", format));

		System.out.println(String.format("%s", truncateTime(getCurrentTime())));

		System.out.println(String.format("getTimestamp: %s",
				getTimestamp(2013, 5, 1)));

		System.out.println(String.format(
				"getLatest: %s",
				getLatest(null, getTimestamp(2013, 5, 1),
						getTimestamp(2015, 3, 11), getTimestamp(2011, 5, 1),
						getTimestamp(2017, 12, 1), getTimestamp(2009, 12, 1))));

//		System.out.println(String.format("%s", addDays(getTimestamp(2016, 2, 29), 30)));
//		
//		System.out.println(String.format("%s", addDays(getTimestamp(2016, 3, 15), 30)));
//		
//		System.out.println(String.format("%s", addDays(getTimestamp(2016, 5, 1), 30)));
	}

}
