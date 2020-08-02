package wang.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 不好专门分类的工具函数和常量
 * 
 * @author Administrator
 *
 */
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class CommonUtil {

	/**
	 * 格式化数字
	 * 
	 * @param num
	 * @param format
	 * @return
	 */
	public static double getDecimal(double num, String format) {
		DecimalFormat dFormat = new DecimalFormat(format);
		String string = dFormat.format(num);
		Double temp = Double.valueOf(string);
		return temp;
	}

	/**
	 * 数字格式化 如果传入的字符串不是标准数字,则会抛出异常
	 * 
	 * @param value
	 * @param min   最少小数位数
	 * @param max   最多小数位数
	 * @return 字符串格式,如果是什么什么.0那么就去掉后面的0
	 */
	public static String formatNum(String value, int min, int max) {
		String x = String.valueOf(formatNum(Double.parseDouble(value), min, max));
		Pattern zeroMatch = Pattern.compile("\\.0+");
		Matcher matcher = zeroMatch.matcher(x);
		if (matcher.find()) {
			return x.substring(0, matcher.start());
		}

		return x;
	}

	/**
	 * 数字格式化 如果传入的字符串不是标准数字,则会抛出异常
	 * 
	 * @param value
	 * @param min   最少小数位数
	 * @param max   最多小数位数
	 * @return
	 */
	public static Double formatNum(double d, int min, int max) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(max);
		nf.setMinimumFractionDigits(min);
		// 四舍6如5随机
		nf.setRoundingMode(RoundingMode.HALF_EVEN);
		/*
		 * 如果想输出的格式用逗号隔开，可以设置成true
		 */
		nf.setGroupingUsed(false);
		return Double.valueOf(nf.format(d));
	}

	/**
	 * 将整型数字转换为二进制字符串，一共32位，不舍弃前面的0
	 *
	 * @param number 整型数字
	 * @return 二进制字符串
	 */
	public static String int2BitStr(int number) {
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			sBuilder.append(number & 1);
			number = number >>> 1;
		}
		return sBuilder.reverse().toString();
	}

	/**
	 * 数组,集合,整数,字符串的转换 结果都是对象
	 *
	 * @param resource    原数据 可以是 字符串,数组,集合()
	 * @param arrayOrList 1array 2 list
	 * @param intOrString 1 int 2:String
	 * @param split       用什么来拆分字符串
	 * @return
	 */
	public static Object listArray(Object resource, int arrayOrList, int intOrString, String split) {
		Object result = null;
		if (resource == null || StringUtils.isEmpty(resource.toString())) {// 空数据
			return empty(arrayOrList, intOrString);
		} else if (resource instanceof String) {// 字符串
			if (StringUtils.isEmpty(split)) {
				split = ",";
			}
			if (split.equals(resource)) {
				return empty(arrayOrList, intOrString);
			}
			String x = (String) resource;
			if (x.startsWith(split)) {// 去除开头的分隔符
				x = x.substring(split.length());
			}
			String[] ps = x.split(split);

			switch (arrayOrList) {
			case 1:
				switch (intOrString) {
				case 1:
					Integer[] is = new Integer[ps.length];
					for (int i = 0; i < ps.length; i++) {
						is[i] = Integer.parseInt(ps[i]);
					}
					result = is;
					break;
				case 2:
					result = ps;
					break;
				default:
					throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
				}
				break;
			case 2:// 集合
				switch (intOrString) {
				case 1:
					List<Integer> ls = new ArrayList<Integer>();
					for (String s : ps) {
						ls.add(Integer.parseInt(s));
					}
					result = ls;
					break;
				case 2:
					List<String> lss = new ArrayList<String>();
					for (String s : ps) {
						lss.add(s);
					}
					result = lss;
					break;
				default:
					throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
				}
				break;
			default:
				throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
			}
		} else if (resource instanceof Collection) {// 集合
			Collection x = (Collection) resource;
			if (CollectionUtils.isEmpty(x)) {
				return empty(arrayOrList, intOrString);
			}
			Iterator i = x.iterator();

			switch (arrayOrList) {
			case 1:
				int index = 0;
				switch (intOrString) {
				case 1:
					Integer[] is = new Integer[x.size()];
					while (i.hasNext()) {
						Object object = i.next();
						if (object instanceof Integer) {
							is[index++] = (Integer) object;
						} else if (object instanceof String) {
							is[index++] = Integer.parseInt((String) object);
						}
					}
					result = is;
					break;

				case 2:
					String[] iss = new String[x.size()];
					while (i.hasNext()) {
						Object object = i.next();
						iss[index++] = object + "";
					}
					result = iss;
					break;
				default:
					throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
				}
				break;

			case 2:// jihe
				List ls = new ArrayList();
				switch (intOrString) {
				case 1:
					while (i.hasNext()) {
						Object object = i.next();
						if (object instanceof Integer) {
							ls.add(object);
						} else if (object instanceof String) {
							ls.add(Integer.parseInt(object + ""));
						}
					}
					break;
				case 2:
					while (i.hasNext()) {
						Object object = i.next();
						ls.add(object + "");
					}
					break;
				default:
					throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
				}
				result = ls;
				break;
			default:
				throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
			}
		} else if (resource instanceof Object[] || resource instanceof int[]) {// 数组
			String[] ps = null;
			if (resource instanceof Object[]) {
				Object[] x = (Object[]) resource;
				if (x.length == 0) {
					return empty(arrayOrList, intOrString);
				} else {
					ps = new String[x.length];
					for (int i = 0; i < x.length; i++) {
						ps[i] = x[i] + "";
					}
				}
			} else {
				int[] x = (int[]) resource;
				if (x.length == 0) {
					return empty(arrayOrList, intOrString);
				} else {
					ps = new String[x.length];
					for (int i = 0; i < x.length; i++) {
						ps[i] = x[i] + "";
					}
				}
			}

			switch (arrayOrList) {
			case 1:
				switch (intOrString) {
				case 1:
					Integer[] is = new Integer[ps.length];
					for (int i = 0; i < ps.length; i++) {
						is[i] = Integer.parseInt(ps[i]);
					}
					result = is;
					break;
				case 2:
					result = ps;
					break;
				default:
					throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
				}
				break;
			case 2:// 集合
				switch (intOrString) {
				case 1:
					List<Integer> ls = new ArrayList<Integer>();
					for (String s : ps) {
						ls.add(Integer.parseInt(s));
					}
					result = ls;
					break;
				case 2:
					List<String> lss = new ArrayList<String>();
					for (String s : ps) {
						lss.add(s);
					}
					result = lss;
					break;
				default:
					throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
				}
				break;
			default:
				throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
			}
		}

		return result;
	}

	private static Object empty(int arrayOrList, int intOrString) {
		Object obj = null;
		switch (arrayOrList) {
		case 1:// 数组
			switch (intOrString) {
			case 1:// 数字
				obj = new Integer[] {};
				break;
			case 2:// 字符串
				obj = new String[] {};
				break;
			default:
				throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
			}
			break;

		case 2:// 集合
			obj = new ArrayList();
			break;
		default:
			throw new RuntimeException(" arrayOrList, intOrString  is only for    1/2");
		}
		return obj;
	}

}
