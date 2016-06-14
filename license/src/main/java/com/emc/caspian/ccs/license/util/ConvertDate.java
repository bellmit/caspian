package com.emc.caspian.ccs.license.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.emc.caspian.ccs.license.DateParseException;

public class ConvertDate {

	public static String convertMillisToUTC(long timestamp) throws DateParseException {
		try{
			final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
			final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
			final TimeZone utc = TimeZone.getTimeZone("UTC");
			sdf.setTimeZone(utc);
			Date date = new Date(timestamp);
			return sdf.format(date).toString();
		}catch (Exception e) {
			AppLogger.error("Error in converting millis date to UTC", e);
			throw new DateParseException(e);
		}
	}

	public static long convertrawToMillis(String date) throws DateParseException{
		try{
			String pattern = "dd-MMM-yyyy";
			SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date convertDate = dateFormat.parse(date);	
			return convertDate.getTime();
		}catch (Exception e) {
			AppLogger.error("Error in converting license date to millis", e);
			throw new DateParseException(e);
		}
	}

}
