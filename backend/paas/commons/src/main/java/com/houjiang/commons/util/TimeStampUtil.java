package com.houjiang.commons.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 随机数与时间戳
 * 
 * @author Amos
 *
 */
public class TimeStampUtil {
	public static final String LONG_DATE = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 生成单号
	 * 
	 * @return
	 */
	public static String getTimeStamp() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateString = formatter.format(currentTime);
		int random = (int) (Math.random() * 9000 + 1000);
		String timestamp = dateString + random;
		return timestamp;

	}

	/**
	 * 时间字符串转化为日期类型，格式为yyyy-MM-dd HH:mm:ss
	 *
	 * @param dateTime
	 *            时间字符串，格式为yyyy-MM-dd HH:mm:ss
	 * @return Date
	 */
	public static Date parseDateTime(String dateTime){
		try {
			return new SimpleDateFormat(LONG_DATE).parse(dateTime);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 设置微信二维码失效时间，并返回具体失效的时间点
	 * 
	 * @param expire
	 *            二维码的有效时间，单位是毫秒
	 * @return
	 */
	public static String getOrderExpireTime(Long expire) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date now = new Date();
		Date afterDate = new Date(now.getTime() + expire);
		return sdf.format(afterDate);
	}

	/**
	 * 生成支付宝单号
	 * 
	 * @return
	 */
	public static String getALiTimeStamp() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateString = formatter.format(currentTime);
		int random = (int) (Math.random() * 900 + 100);
		String timestamp = dateString + random;
		return timestamp;

	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	public static String getNowDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取年份
	 * 
	 * @return
	 */
	public static String getYear() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		String year = formatter.format(currentTime);
		return year;

	}

	/**
	 * 获取月份
	 * 
	 * @return
	 */
	public static String getMonth() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM");
		String month = formatter.format(currentTime);
		return month;

	}

	/**
	 * 获取日期
	 * 
	 * @return
	 */
	public static String getDay() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd");
		String day = formatter.format(currentTime);
		return day;

	}

	/**
	 * 获取当前时间时间戳
	 * 
	 * @return
	 */
	public static Integer timeToStamp() {
		int seconds = (int) (System.currentTimeMillis() / 1000);
		return seconds;
	}

	/**
	 * 将时间转换为时间戳
	 */
	public static String dateToStamp(String s) throws ParseException {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = simpleDateFormat.parse(s);
		long ts = date.getTime();
		res = String.valueOf(ts);
		return res;

	}

	public static String dateToStr(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(date);
		return dateString;

	}

	/**
	 * 将时间戳转换为时间
	 */
	public static String stampToDate(String s) {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long lt = Long.valueOf(s);
		Date date = new Date(lt);
		res = simpleDateFormat.format(date);
		return res;
	}

	/**
	 * 获取当前时间时间戳（后九位）
	 * 
	 * @return
	 */
	public static String nowtime() {
		int seconds = (int) (System.currentTimeMillis() / 1000);
		String str = String.valueOf(seconds);
		String time = str.substring(1, 10);
		return time;
	}

	/**
	 * 随机获取四位数
	 * 
	 * @return
	 */
	public static int randomLengthByTwo() {
		int random = (int) (Math.random() * 9000 + 1000);
		return random;
	}

	/**
	 * 随机取时分秒
	 * 
	 * @return
	 */
	public static String randomDate() {
		Date date = randomDate("00:00:00", "23:59:59");
		return new SimpleDateFormat("HH:mm:ss").format(date);
	}

	/**
	 * 获取随机日期
	 * 
	 * @param beginDate
	 *            起始日期，格式为：yyyy-MM-dd或HH:mm:ss
	 * @param endDate
	 *            结束日期，格式为：yyyy-MM-dd或HH:mm:ss
	 * @return
	 */
	private static Date randomDate(String beginDate, String endDate) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			Date start = format.parse(beginDate);
			Date end = format.parse(endDate);

			if (start.getTime() >= end.getTime()) {
				return null;
			}

			long date = random(start.getTime(), end.getTime());

			return new Date(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static long random(long begin, long end) {
		long rtn = begin + (long) (Math.random() * (end - begin));
		if (rtn == begin || rtn == end) {
			return random(begin, end);
		}
		return rtn;
	}

	/**
	 * 通过时间秒毫秒数判断两个时间的间隔
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int differentDaysByMillisecond(Date date1,Date date2)
	{
		int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
		return days;
	}

	/**
	 * 获取当前时间前后的时间
	 * @param day
	 * @return
	 */
	public static String getAfterDate(int day) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, +day);
		date = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat(LONG_DATE);
		String dateString = formatter.format(date);
		return dateString;
	}
	/**
	 * 比较时间
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static boolean compareDate(String time1,String time2) {
		if(StringUtils.isBlank(time1) || StringUtils.isBlank(time2)){
			return true;
		}else {
			Date date1 = parseDateTime(time1);
			Date date2 = parseDateTime(time2);
			if (date1.before(date2)) {
				return true;
			}
		}
		return false;
	}

}
