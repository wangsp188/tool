package wang.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	/**
	 * 日期转换成yyyyMMdd形式
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date, String partten) {
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat(partten);
		return yyyyMMdd.format(date);
	}

	/**
	 * 字符传依据partten转换成日期
	 * 
	 * @param result
	 * @param partten
	 * @return
	 */
	public static Date parseDate(String result, String partten) throws IllegalArgumentException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(partten);
			return dateFormat.parse(result);
		} catch (ParseException e) {
			throw new IllegalArgumentException("日期转换失败");
		}
	}

	/**
	 * 时间格式的正则表达式
	 * 
	 * @return
	 */
	public static String dateRegex() {
		return "^(((\\d{2}(([02468][048])|([13579][26]))[\\-]((((0?[13578])|(1[02]))[\\-]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-]((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-]((((0?[13578])|(1[02]))[\\-]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-]((0?[1-9])|(1[0-9])|(2[0-8]))))))|"
				+ "((\\d{2}(([02468][048])|([13579][26]))[\\/]((((0?[13578])|(1[02]))[\\/]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\/]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\/]((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\/]((((0?[13578])|(1[02]))[\\/]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\/]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\/]((0?[1-9])|(1[0-9])|(2[0-8])))))))";
	}
}
