package wang.excel.common.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.PictureData;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import wang.excel.common.iwf.*;
import wang.excel.common.model.BeanParseParam;
import wang.util.CommonUtil;
import wang.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ParseUtil {

	private static final String[] nullStrArr = new String[] { "空", "无", "null", "/", "-" };


	/**
	 * 将可能不符合规范的字符串尽量解析成 形如 yyyy/MM/dd形似的字符串 并返回(只支持到天)
	 * 
	 * @param s        值
	 * @param splitArr 字符串分隔符 支持 , . - / 有默认值
	 * @return
	 */
	public static String parseStrToDateStr(String s, String[] splitArr) {
		String reVal = "";
		String[] fs = parseDateFormat(s, splitArr);
		reVal = fs[0];
		switch (Integer.parseInt(fs[1])) {
		case 1:
			reVal = reVal + "/01/01";
			break;
		case 2:
			reVal = reVal + "/01";
			break;
		case 3:
			break;
		default:
			break;
		}
		return reVal;
	}

	/**
	 * 根据传入时间字符串,解析成2011/10/08 类似格式字符串 及分割的份数 注意 由于有可能传入的是 如 2017.1 的情况 ,这么
	 * 他会解析成数字....所以判断是,要加入数字的情况
	 * 
	 * @param s        传入不可为空
	 * @param splitArr 时间分隔符数组,如果不传或长度为0 默认 { , . - /}
	 * @return
	 */
	private static String[] parseDateFormat(String s, String[] splitArr) {
		Assert.notNull(s, "时间解析失败,数据不能为空");
		int yearIndex = s.indexOf("年");
		String reString = "";
		String[] ds = null;
		// 年
		if (yearIndex != -1) {
			String len = "1";
			// 判断是19开头还是20开头
			if (yearIndex == 4) {
				reString = s.substring(0, yearIndex);
			} else if (yearIndex == 2) {
				int year = Integer.parseInt(s.substring(0, 2));
				if (year > 59) {
					reString = "19" + year;
				} else {
					reString = "20" + year;
				}
			}
			// 最后一个
			if (yearIndex == s.length() - 1) {
			} else {
				reString += "/";
				String noY = s.substring(yearIndex + 1);
				int monthIndex = noY.indexOf("月");
				if (monthIndex != -1) {
					len = "2";
					if (monthIndex == 2) {
						reString += noY.substring(0, monthIndex);
					} else if (monthIndex == 1) {
						reString += "0" + noY.substring(0, monthIndex);
					}
					// 最后
					if (monthIndex == noY.length() - 1) {
					} else {
						reString += "/";
						String noYM = noY.substring(monthIndex + 1);
						int dayIndex = noYM.indexOf("日");
						if (dayIndex == -1) {
							dayIndex = noYM.indexOf("号");
						}

						if (dayIndex != -1) {
							len = "3";
							if (dayIndex == 2) {
								reString += noYM.substring(0, dayIndex);
							} else if (dayIndex == 1) {
								reString += "0" + noYM.substring(0, dayIndex);
							}
						}
					}
				}
			}
			return new String[] { reString, len };
		} else {// 不是年月日
			String[] ss = null;
			if (splitArr == null || splitArr.length == 0) {
				ss = new String[] { "丶", "\\.", "/", "-" };
			} else {
				ss = splitArr;
			}

			ds = new String[1];
//			String sp = "";
			for (int i = 0; ds.length == 1 && i < ss.length; i++) {
				ds = s.split(ss[i]);
//				sp = ss[i];
			}
			for (int i = 0; i < ds.length; i++) {
				if (ds[0].length() == 2) {
					if (Integer.parseInt(ds[0]) > 59) {
						ds[0] = "19" + ds[0];
					} else {
						ds[0] = "20" + ds[0];
					}
				}
				if (ds[i].equals("0")) {
					ds[i] = "01";
				} else if (ds[i].length() == 1) {
					ds[i] = "0" + ds[i];
				}
			}
		}
        StringJoiner joiner = new StringJoiner("/", "", "");
        for (String d : ds) {
            joiner.add(d);
        }
        return new String[] { joiner.toString(), ds.length + "" };
	}


	/**
	 * 解析单元格
	 * 
	 * @param t             当前对象
	 * @param one           解析参数
	 * @param field         当前属性
	 * @param cell          目标单元格
	 * @param imgs          单元格中的图片
	 * @param typeHandleMap 类型解析器
	 * @param <T>
	 * @return
	 */
	public static <T> Object parseCell(T t, BeanParseParam one, Field field, Cell cell, List<PictureData> imgs, final Map<Class, ExcelTypeHandler> typeHandleMap) throws Exception {
		ParseConvert parseConvert = one.getParseConvert();
		// 转换接口
		if (parseConvert != null) {
			return parseConvert.parse(cell, imgs);
		}

		// 获取实际类型
		Class fieldType = ParseUtil.getRealType(field);
		// 方法转换
		String importFormat = one.getBeanInnerParseConvert();
		if (importFormat != null) {// 有格式化,
			return innerImportFormat(t, cell, imgs, importFormat);
		}

		// 图片
		ImgStore imgStrategy = one.getImgStoreStrategy();
		if (imgStrategy != null ) {// 说明是策略图片保存
			return imgFormat(t, imgs, imgStrategy);
		}

		// 普通解析
		String[] str2NullArr = one.getStr2NullArr();
		Object cellval = ExcelUtil.getCellValue(cell, str2NullArr, true, -1);

		// 字典
		Map<String, String> dic = one.getDicMap();
		if (dic != null) {// 字典值转换
			return parseDic(cellval, fieldType, dic, one.isMultiChoice(), one.getDicErr());
		}
		// 类型解析器解析
		return simpleParse(cellval, imgs, typeHandleMap, fieldType);
	}

	/**
	 * 普通解析
	 * 
	 * @param cellVal       单元格值
	 * @param img          图片
	 * @param typeHandleMap 类型解析器
	 * @param fieldType     属性类型
	 * @return
	 */
	private static Object simpleParse(Object cellVal, List<PictureData> img, Map<Class, ExcelTypeHandler> typeHandleMap, Class fieldType) {
		ExcelTypeHandler handle = null;
		Assert.notNull(fieldType, "类型解析失败！");
		if (typeHandleMap != null) {
			handle = typeHandleMap.get(fieldType);
		}
		if (handle != null) {
			return handle.parse(cellVal, img);
		}

		return cell2Data(cellVal, fieldType);
	}

	/**
	 * 图片解析
	 * 
	 * @param t           载体
	 * @param img        图片
	 * @param imgStrategy 图片策略
	 * @param <T>
	 * @return
	 */
	private static <T> Object imgFormat(T t, List<PictureData> img, ImgStore imgStrategy) {
		Assert.notNull(t, "载体类不可为空");
		if (CollectionUtils.isEmpty(img)) {
			return null;
		}
		try {
			StringJoiner joiner = new StringJoiner(",","","");
			for (PictureData p : img) {
				joiner.add(imgStrategy.uploadReturnKey(p, null));
			}
			return joiner.toString();
		} catch (Exception e) {
			throw new RuntimeException("图片保存失败" + e.getMessage());
		}
	}

	/**
	 * 类内部方法转换
	 * 
	 * @param t            载体对象
	 * @param cell         单元格
	 * @param imgs         图片
	 * @param importFormat 转换
	 * @param <T>
	 * @return
	 */
	private static <T> Object innerImportFormat(T t, Cell cell, List<PictureData> imgs, String importFormat) {
		// 是否是静态函数
		boolean staticsMethod = importFormat.contains(".");
		try {
			if (staticsMethod) {
				int last = importFormat.lastIndexOf(".");
				String className = importFormat.substring(0, last);
				String methodName = importFormat.substring(last + 1);
				Class cz = Class.forName(className);
				Method method = cz.getDeclaredMethod(methodName, Cell.class, List.class);
				method.setAccessible(true);
				return method.invoke(null, cell, imgs);
			} else {
				Class type = t.getClass();
				Method formatMethod = ReflectionUtils.findMethod(type, importFormat, Cell.class, List.class);
				formatMethod.setAccessible(true);
				return formatMethod.invoke(t, cell, imgs);
			}
		} catch (Exception e) {
			throw new RuntimeException("自定义解析方法执行失败" + importFormat);
		}

	}

	/**
	 * 字典转换
	 * 
	 * @param cellValue   但愿搁置
	 * @param dic         字典
	 * @param multiChoice 多选查询
	 * @param dicErr      字典转换失败
	 * @return
	 */
	private static Object parseDic(Object cellValue, Class type, Map<String, String> dic, boolean multiChoice, DicErr dicErr) {
		if (cellValue==null || StringUtils.isEmpty(cellValue.toString())) {
			return null;
		}
		if (cellValue instanceof Double) {
			cellValue = ((Double) cellValue).intValue();
		}
		try {
			String val = ExcelUtil.convertDic(dic, cellValue, multiChoice, dicErr);
			if (val == null || val.length() == 0) {
				return null;
			}
			if (type.equals(String.class)) {
				return val;
			} else if (int.class.equals(type) || Integer.class.equals(type)) {
				return Integer.valueOf(val);
			} else if (type.equals(char.class) || type.equals(Character.class)) {
				return val.charAt(0);
			}

		} catch (Exception e) {
			List<String> keys = new ArrayList<>(dic.keySet());
			String tip ;
			if(keys.size()>10){
				tip = keys.subList(0, 10).toString()+"......";
			}else {
				tip = keys.toString();
			}
			throw new RuntimeException("该数据项有固定匹配规则,可选值有" + tip + (multiChoice ? "(支持多个,逗号隔开)," : ",") + "请规范填写!");
		}
		throw new IllegalArgumentException("字典类型不适配,支持字符/整数");
	}

	/**
	 * 解析单元格的值 如果传入的属性名是个集合,则取集合的泛型去解析
	 * 
	 * @param cellValue 单元格值
	 * @return 解析的结果
	 * @throws RuntimeException 解析失败抛出异常 异常中message进行错误说明
	 */
	private static Object cell2Data(Object cellValue, Class fieldType) {
		Object result = cellValue;
		// 如果是空,直接返回
		if (result==null || StringUtils.isEmpty(result.toString())) {
			return null;
		}

		// 错误信息
		String errDesc = null;
		// 默认解析
		try {
			// 将读取到的值转换为字段支持的值
			if (fieldType.equals(String.class)) {
				errDesc = "[" + cellValue + "]," + "请填写文本";
				if (result instanceof Double) {// 数值
					result = CommonUtil.formatNum(result.toString(), 0, 3);
				} else if (result instanceof Date) {// 时间
					Date d = (Date) result;
					Calendar calendar = Calendar.getInstance();
					if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0) {
						result = wang.util.DateUtil.formatDate(d, "yyyy-MM-dd");
					} else {
						result = wang.util.DateUtil.formatDate(d, "yyyy-MM-dd HH:ss:mm");
					}
					try {
						if (StringUtils.isNotEmpty(cellValue.toString())) {
							result = DateUtil.getJavaDate(Double.parseDouble(cellValue.toString()));
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			} else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
				errDesc = "[" + cellValue + "]," + "请填写整数";
				if (result instanceof Double) {
					result = ((Double) result).intValue();
				} else if (result instanceof String) {// 有可能是字典
					result = Double.valueOf((String) result).intValue();
				}
			} else if (fieldType.equals(Date.class)) {
				errDesc = "[" + cellValue + "]," + "请填写时间或规范文本eg:2017/08/09";
				if (result instanceof Long) {
					result = new Date((Long) result);
				} else if (result instanceof String || result instanceof Number) {

					result = ParseUtil.parseStrToDateStr(result.toString(), new String[] { "/", "\\.", "丶", "-" });
					// 正则判断时间格式是否正确
					if (((String) result).matches(wang.util.DateUtil.dateRegex())) {
						result = wang.util.DateUtil.parseDate(((String) result), "yyyy/MM/dd");
					} else {// 格式不正确,抛出异常
						result = DateUtil.getJavaDate(Double.parseDouble(cellValue.toString()));

					}
				}

			} else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
				errDesc = "[" + cellValue + "]," + "请填写小数或整数";
				result = Double.parseDouble(CommonUtil.formatNum(result.toString(), 0, 3));
			} else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
				errDesc = "[" + cellValue + "]," + "该项属于是否关系,请填写(true,false,是,否,1,0)";
				if (result instanceof String) {
					String cb = String.valueOf(result);
					result = cb.equals("true") || result.equals("1");
				} else if (result instanceof Double || result instanceof Integer) {
					if (result instanceof Double) {
						result = ((Double) result).intValue();
					}
					result = (Integer) result == 1;
				}
			} else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
				errDesc = "[" + cellValue + "]," + "请填写单字符";
				if (!(result instanceof String)) {
					result = String.valueOf(result);
				}
				result = ((String) result).charAt(0);
			} else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
				errDesc = "[" + cellValue + "]," + "请填写小于128的整数";
				if (result instanceof Double) {
					result = ((Double) result).byteValue();
				} else if (result instanceof String) {// 有可能是字典
					result = Double.valueOf((String) result).byteValue();
				}
			} else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
				errDesc = "[" + cellValue + "]," + "请填写小于32767的整数";
				if (result instanceof Double) {
					result = ((Double) result).shortValue();
				} else if (result instanceof String) {// 有可能是字典
					result = Double.valueOf((String) result).shortValue();
				}
			} else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
				errDesc = "[" + cellValue + "]," + "请填写小数或整数";
				if (result instanceof Double) {
					result = ((Double) result).floatValue();
				} else if (result instanceof String) {
					result = Float.parseFloat((String) result);
				} else {
					throw new RuntimeException();
				}
			} else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
				errDesc = "[" + cellValue + "]," + "请填写整数";
				if (result instanceof Double) {
					result = ((Double) result).longValue();
				} else if (result instanceof String) {
					result = Double.valueOf((String) result).longValue();
				}
			} else {
				throw new RuntimeException("不支持的类型,请配置类型解析器！" + fieldType);
			}
		} catch (Exception e) {
			throw new RuntimeException(errDesc);
		}
		return result;
	}

	// 获取字段的实际类型
	private static Class getRealType(Field field) {
		Class fieldType = field.getType();
		boolean isCollection = Collection.class.isAssignableFrom(fieldType);
		if (isCollection) {// 集合类型,取其泛型
			Class[] types = ReflectUtil.getFieldActualType(field);
			if (types.length != 1) {
				throw new IllegalArgumentException("请指定集合中具体泛型,或泛型不规范");
			}
			fieldType = types[0];
		}
		return fieldType;
	}

	/**
	 * 解析字段上的注解 Excel 或 excel 获取解析时的信息
	 * 
	 * @param field 解析的字段
	 * @return 结果
	 */
	public static BeanParseParam field2ImportParam(Field field) {
		BeanParseParam cp = new BeanParseParam();
		Excel ew = field.getAnnotation(Excel.class);
		if (ew != null) {
			cp.setNullable(ew.nullable());
			// 字典
			Map<String, String> dicMap = DicFactory.get(ew.dicGroup(), false);
			if (dicMap==null && ew.replace().length > 0) {
				dicMap = ExcelUtil.initDicMap(ew.replace(), false);
			}
			cp.setDicMap(dicMap);
			// 转换
			String importFormat = StringUtils.isEmpty(ew.innerParseConvert().trim()) ? null : ew.innerParseConvert().trim();
			cp.setBeanInnerParseConvert(importFormat);
			// 图片生成策略
			cp.setImgStoreStrategy(ImgStoreFactory.get(ew.imgStoreStrategy()));
			cp.setDicErr(ew.dicErr());
			cp.setMultiChoice(ew.multiChoice());
			cp.setStr2NullArr(ew.str2Nulls());
			cp.setName(StringUtils.isEmpty(ew.name()) ? field.getName() : ew.name());
		} else {
			// 没有注解
			cp.setName(field.getName());
			cp.setImgStoreStrategy(null);
			cp.setNullable(true);
			cp.setStr2NullArr(commonNullStrArr());
		}
		return cp;
	}

	/**
	 * 普通做空字符串数组
	 * @return
	 */
	private static String[] commonNullStrArr() {
		return nullStrArr;
	}

}
