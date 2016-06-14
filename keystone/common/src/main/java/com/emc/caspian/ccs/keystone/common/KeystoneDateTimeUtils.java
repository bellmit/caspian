/**
 * 
 */
package com.emc.caspian.ccs.keystone.common;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author krishs9
 *
 * Utility functions to handle keystone specific date time string formats
 * Keystone generate date and time strings in ISO8601 compliant formats when generating the auth tokens
 */
public class KeystoneDateTimeUtils {
  private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();

  /**
   * @param keystoneDateTimeString
   *    The date time string in ISO8601 format. Eg: "2015-06-02T08:43:59.746123Z"
   * @return long
   *    The time in milliseconds since epoch
   * @throws IllegalArgumentException
   */
  public static long getTimeInMillis(String keystoneDateTimeString) throws IllegalArgumentException {
    return dateTimeFormatter.parseMillis(keystoneDateTimeString); 
  }
  
  /**
   * @param time1
   * @param time2
   * @return
   *    0 if time1 and time2 are identical with millisecond precision 
   *    >0 if time1 is after time2
   *    <= if time1 is before time2
   * @throws IllegalArgumentException
   */
  public static int compareTime(String time1, String time2) throws IllegalArgumentException {
    long lTime1 = dateTimeFormatter.parseMillis(time1); 
    long lTime2 = dateTimeFormatter.parseMillis(time2);
    if (lTime1 > lTime2) {
      return 1;
    } else if (lTime1 < lTime2) {
      return -1;
    } else {
      return 0;
    }
  }
  
}
