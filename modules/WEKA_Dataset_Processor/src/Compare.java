import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

public class Compare {

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) {
		System.out.println(timeInMinutes("2111-1-1",
				Long.parseLong("1370563385000")));
	}

	public static long timeInMinutes(String d1, Long d2) {
		String date = "1900-1-1";
		if ((!d1.equals("")) || (!d1.contains("?"))) {
			System.out.println();
			String[] tokens = d1.split("-");
			if (tokens.length == 1) {
				date = d1 + "-1-1";
			} else if (tokens.length == 2) {
				date = d1 + "-1";
			} else if (tokens.length == 3) {
				date = d1;
			}
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = null;
		try {
			date1 = format.parse(date);
		} catch (ParseException e) {
			System.out.println("ERROR: " + d1);
		}
		// Date date2 = format.parse("2014-07-2");

		DateTime dt1 = new DateTime(date1);
		DateTime dt2 = new DateTime(d2);
		int mins = 100000;
		try {
			mins = Minutes.minutesBetween(dt1, dt2).getMinutes();
		} catch (Exception e) {
			return 100000;
		}
		return mins;
	}
}
