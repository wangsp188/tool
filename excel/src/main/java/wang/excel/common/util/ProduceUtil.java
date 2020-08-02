package wang.excel.common.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import wang.excel.common.iwf.*;
import wang.excel.common.model.*;

@SuppressWarnings("rawtypes")
public class ProduceUtil {

	/**
	 * 根据构建参数获取需要插入的cellData 支持对集合类型数据的转换
	 * 
	 * @param produceParam 构建参数
	 * @param target       当前对象
	 * @param key          当前属性名 支持对集合类型数据的转换 如果是个集合类型的字段 需要以 实际名[下标]形式去传参
	 * @return
	 */
	public static CellData key2CellDataConvertArr(BaseProduceParam produceParam, Object target, String key) {
		return key2CellData(produceParam, target, key, true);
	}

	/**
	 * 根据构建参数获取需要插入的cellData 支持对集合类型数据的转换
	 * 
	 * @param produceParam 构建参数
	 * @param target       当前对象
	 * @param key          当前属性名
	 * @param supportArr   是否支持对集合类型数据的转换 如果是个集合类型的字段 需要以 实际名[下标]形式去传参
	 * @return
	 */
	public static CellData key2CellData(BaseProduceParam produceParam, Object target, String key, boolean supportArr) {
		CellData cellData = new CellData();
		if (target == null) {
			return cellData;
		}
		Assert.notNull(key, "属性名不可为空");
		Class type = target.getClass();
		Object fieldVal = getKeyValFromObject(target, key, supportArr);
		if (produceParam == null) {
			cellData.setValue(fieldVal);
		} else {
			// 自定义构建转换
			ProduceConvert produceConvert = produceParam.getProduceConvert();
			if (produceConvert != null) {
				return produceConvert.convert(fieldVal);
			}

			String format = produceParam.getMethodProduceConvert();
			if (!StringUtils.isEmpty(format)) {// 需要转换
				cellData = processCellDataUseMethodConvert(target, type, format);
			} else if (fieldVal != null && !StringUtils.isEmpty(fieldVal.toString())) {// 自动解析
				ImgStoreStrategy ie = produceParam.getImgStoreStrategy();
				Map<String, String> dic = produceParam.getDicMap();
				if (ie != null) {// 图片
					processCellDataUseImg(produceParam, cellData, fieldVal, ie);
				} else if (dic != null) {// 字典
					processCellDataUseDic(produceParam, cellData, fieldVal, dic);
				} else {// 直接取值
					cellData.setType(CellData.AUTO);
					cellData.setValue(fieldVal);
				}
			}
		}
		// 空值赋值
		if (CellData.isEmpty(cellData)) {
			cellData.setValue(produceParam == null ? null : produceParam.getNullStr());
		}
		return cellData;
	}

	/**
	 * 使用字典解析
	 * 
	 * @param produceParam
	 * @param cellData
	 * @param fieldVal
	 * @param dic
	 */
	private static void processCellDataUseDic(BaseProduceParam produceParam, CellData cellData, Object fieldVal, Map<String, String> dic) {
		cellData.setType(CellData.AUTO);
		cellData.setValue(ExcelUtil.convertDic(dic, fieldVal, produceParam.isMultiChoice(), produceParam.getDicErr()));
	}

	/**
	 * 使用图片逻辑解析
	 * 
	 * @param produceParam
	 * @param cellData
	 * @param fieldVal
	 * @param ie
	 */
	private static void processCellDataUseImg(BaseProduceParam produceParam, CellData cellData, Object fieldVal, ImgStoreStrategy ie) {
		cellData.setType(CellData.IMG);
		BaseImgData beanImgData = new BeanImgData();
		List<File> img = ie.recoverKey2Files(fieldVal.toString());
		beanImgData.setImgProduceStrategy(produceParam.getImgProduceStrategy());
		beanImgData.setImgFiles(img);
		cellData.setValue(beanImgData);
	}

	/**
	 * 使用自定义方法解析
	 * 
	 * @param target 当前对象
	 * @param type   当前对象的类
	 * @param format 自定义函数说明
	 * @return
	 */
	private static CellData processCellDataUseMethodConvert(Object target, Class type, String format) {
		CellData cellData;
		// 是否是静态函数
		boolean staticMethod = format.contains(".");
		try {
			if (staticMethod) {
				int last = format.lastIndexOf(".");
				String className = format.substring(0, last);
				String methodName = format.substring(last + 1);
				Class cz = Class.forName(className);
				Method method = cz.getDeclaredMethod(methodName, Object.class);
				method.setAccessible(true);
				cellData = (CellData) method.invoke(null, target);
			} else {
				Method convert = ReflectionUtils.findMethod(type, format, Object.class);
				Assert.notNull(convert, type + "中未找到构建方法:" + format);
				convert.setAccessible(true);
				cellData = (CellData) convert.invoke(target, target);
			}
		} catch (Exception e) {
			throw new RuntimeException("自定义构建方法执行失败" + format);
		}
		return cellData;
	}

	/**
	 * 根据key获取实体值
	 * 
	 * @param target     实体
	 * @param key        表达式(字段名/wo[1]这种)
	 * @param supportArr 是否支持wo[1] 形式的解析
	 * @return
	 */
	private static Object getKeyValFromObject(Object target, String key, boolean supportArr) {
		Object fieldVal = null;
		try {
			if (supportArr) {
				Pattern manyP = Pattern.compile("\\w+\\[\\d+]");
				Matcher matcher = manyP.matcher(key);
				if (matcher.matches()) {
					String hit = matcher.group();
					int s = hit.indexOf("[");
					int e = hit.indexOf("]");
					String listName = hit.substring(0, s);
					Object listVal = getValFromObjectOrMap(target, listName);
					if (listVal != null) {
						if (!(listVal instanceof List)) {
							throw new RuntimeException("多的匹配,所存对象必须是有序集合List");
						}
						List fv = (List) listVal;
						int index = Integer.parseInt(hit.substring(s + 1, e));
						if (index < fv.size()) {
							fieldVal = fv.get(index);
						}
					}
				} else {
					fieldVal = getValFromObjectOrMap(target, key);
				}
			} else {
				fieldVal = getValFromObjectOrMap(target, key);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException("获取属性值失败" + target.getClass() + "属性" + key);
		}
		return fieldVal;
	}

	/**
	 * 从对象中获取属性 支持 Map 和 Object
	 * 
	 * @param obj 值
	 * @param key key
	 * @return
	 */
	public static Object getValFromObjectOrMap(Object obj, String key) {
		try {
			return PropertyUtils.getProperty(obj, key);
		} catch (Exception e) {
			if (obj instanceof Map) {
				return ((Map) obj).get(key);
			}
			throw new IllegalArgumentException(String.format("获取属性 %s 失败", key));
		}
	}

	/**
	 * 根据构建参数和值 转换为 通用单元格对象
	 * 
	 * @param produceParam 构建参数
	 * @param val          实际值
	 * @return
	 */
	public static CellData o2CellData(BaseProduceParam produceParam, Object val) {
		CellData cellData = new CellData();
		if (produceParam == null) {
			cellData.setValue(val);
		} else {
			if (val != null && !StringUtils.isEmpty(val.toString())) {// 自动解析
				ImgStoreStrategy ie = produceParam.getImgStoreStrategy();
				Map<String, String> dic = produceParam.getDicMap();
				if (ie != null) {// 图片
					cellData.setType(CellData.IMG);
					BaseImgData imgData = new MapImgData();
					List<File> img = ie.recoverKey2Files(val.toString());
					imgData.setImgProduceStrategy(produceParam.getImgProduceStrategy());
					imgData.setImgFiles(img);
					cellData.setValue(imgData);
				} else if (dic != null) {// 字典
					cellData.setType(CellData.AUTO);
					cellData.setValue(ExcelUtil.convertDic(dic, val, produceParam.isMultiChoice(), produceParam.getDicErr()));
				} else {// 直接取值
					cellData.setType(CellData.AUTO);
					cellData.setValue(val);
				}
			}

		}
		// 空值赋值
		if (CellData.isEmpty(cellData)) {
			cellData.setValue(produceParam == null ? null : produceParam.getNullStr());
		}
		return cellData;
	}

	/**
	 * 解析字段上的注解 Excel
	 * 
	 * @param field 字段
	 * @return 行式构建参数
	 */
	public static BeanProduceParam field2BeanProduceParam(Field field) {
		Assert.notNull(field, "要获取构建参数,field不可为空");
		BeanProduceParam cp = new BeanProduceParam();
		Excel ew = field.getAnnotation(Excel.class);
		if (ew != null) {
			// 字典
			Map<String, String> dicMap = DicFactory.get(ew.dicGroup(), true);
			if (dicMap == null && ew.replace().length > 0) {
				dicMap = ExcelUtil.initDicMap(ew.replace(), true);
			}
			cp.setDicMap(dicMap);
			cp.setWidth(ew.width());
			cp.setHeight(ew.height());
			// 转换
			String produceFormat = StringUtils.isEmpty(ew.methodProduceConvert().trim()) ? null : ew.methodProduceConvert().trim();
			cp.setMethodProduceConvert(produceFormat);
			// 图片生成策略
			cp.setImgStoreStrategy(ImgStoreStrategyFactory.get(ew.imgStoreStrategy()));
			cp.setDicErr(ew.dicErr());
			cp.setMultiChoice(ew.multiChoice());
			cp.setNullStr(ew.nullStr());
			cp.setImgProduceStrategy(ew.imgProduceStrategy());
			cp.setOrder(ew.order());
			cp.setName(ew.name());
		} else {// 兼容处理
			cp.setName(field.getName());
		}
		return cp;
	}

	/**
	 * 解析字段上的注解 Excel
	 *
	 * @param field 字段
	 * @return 基础构建参数
	 */
	public static BaseProduceParam field2BaseProduceParam(Field field) {
		Assert.notNull(field, "要获取构建参数,field不可为空");
		BaseProduceParam cp = new BaseProduceParam();
		Excel ew = field.getAnnotation(Excel.class);
		if (ew != null) {
			// 字典
			Map<String, String> dicMap = DicFactory.get(ew.dicGroup(), true);
			if (dicMap == null && ew.replace().length > 0) {
				dicMap = ExcelUtil.initDicMap(ew.replace(), true);
			}
			cp.setDicMap(dicMap);
			// 转换
			String produceFormat = StringUtils.isEmpty(ew.methodProduceConvert().trim()) ? null : ew.methodProduceConvert().trim();
			cp.setMethodProduceConvert(produceFormat);
			// 图片生成策略
			cp.setImgStoreStrategy(ImgStoreStrategyFactory.get(ew.imgStoreStrategy()));
			cp.setDicErr(ew.dicErr());
			cp.setMultiChoice(ew.multiChoice());
			cp.setNullStr(ew.nullStr());
			cp.setImgProduceStrategy(ew.imgProduceStrategy());
		}
		return cp;
	}

}
