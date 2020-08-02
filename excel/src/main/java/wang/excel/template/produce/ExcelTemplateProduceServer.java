package wang.excel.template.produce;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import wang.excel.common.iwf.ProduceConvert;
import wang.excel.common.iwf.SwapCell;
import wang.excel.common.iwf.impl.SimpleSwapCell;
import wang.excel.common.model.BaseProduceParam;
import wang.excel.common.model.BeanProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.common.util.ProduceUtil;
import wang.excel.template.produce.iwf.ProduceModify;
import wang.excel.template.produce.iwf.ProduceSkip;
import wang.util.ReflectUtil;

/**
 * 普通模板解析构建实现 依赖于 @Excel
 * 使用,模板解析时,当需要指定子实体中集合类初始化的类型时(ArrayList.class)可以配合 @NestExcel 指定 注解的具体使用详见
 * 注解说明 解析构建均 支持表的过滤 skip实现 解析支持的操做有 数据解析完成后修正操作 importModify 实现 构建支持 数据转换后的修正
 * modify 实现 模板 的key格式类似于 spring的实体解析 以#{}包裹 形似 #{wo.ni[4].ta}
 * 主实体中字段wo中集合ni的第5个的属性ta
 *
 * @author wangshaopeng
 *
 * @param <E>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExcelTemplateProduceServer {

	/**
	 * 匹配正则,类似于 #{wo.ni[1].ta} 中间可以带些空格
	 */
	private static final Pattern allMatch = Pattern.compile("#\\{\\s*(\\s*\\w+(\\s\\[\\s*\\d+\\s*])?\\s*\\.?)+\\s*}");

	/**
	 * 赋值单元格实现
	 */
	private SwapCell swapCell = new SimpleSwapCell();

	/**
	 * 构建修正接口
	 */
	private ProduceModify modify;

	/**
	 * 构建的自定义接口
	 */
	private Map<String, ProduceConvert> convertMap;

	/**
	 * 解析或构建时过滤sheet
	 */
	private ProduceSkip skip;

	/**
	 * 解析构建自定义
	 *
	 * @param key
	 * @param produceConvert
	 */
	public void registConvert(String key, ProduceConvert produceConvert) {
		if (this.convertMap == null) {
			convertMap = new HashMap<>();
		}
		convertMap.put(key, produceConvert);
	}

	/**
	 * 根据模板构建数据 固定模板的模板构建
	 *
	 * @param entity 实体
	 * @return
	 */
	public Workbook create(InputStream templateIs, Object entity, Map<String, Object> adherentInfo) {
		Assert.notNull(templateIs, "构建模板源为空");
		Workbook resultWb;// 创建输出源
		// 定义字段和解析属性间缓存映射关系 key是 field.toString()
		Map<String, BaseProduceParam> beanProduceParamMap = new HashMap<>();
		try {
			resultWb = WorkbookFactory.create(templateIs);
			List<Integer> removeIndexChain = new ArrayList<>();
			Set<String> adherentKeys = adherentInfo == null ? SetUtils.EMPTY_SET : adherentInfo.keySet();
			for (int i = 0; i < resultWb.getNumberOfSheets(); i++) {
				Sheet oneSheet = resultWb.getSheetAt(i);
				if (skip != null && skip.skip(oneSheet, entity, adherentInfo)) {
					// 由于删除一个后,其实后面的下标都变了,所以这里要减去已删除的个数下标
					removeIndexChain.add(i - removeIndexChain.size());
					continue;
				}
				for (Row oneRow : oneSheet) {
					for (Cell oneCell : oneRow) {
						if (oneCell.getCellType() == Cell.CELL_TYPE_STRING) {
							String cellVal = oneCell.getStringCellValue();
							Set<String> matchSet = matchAndReturn(cellVal, false);
							for (String match : matchSet) {
								// 扩展值
								String allKey = removePrefixAndSuffixAndBlank(match);
								// 如果附着属性包含直接走附着属性
								if (adherentKeys.contains(allKey)) {
									swap4AdherentInfo(beanProduceParamMap, oneCell, match, allKey, adherentInfo);
								} else {
									// 以附着属性开头的
									boolean adherent = swap4StartAdherent(beanProduceParamMap, adherentKeys, oneCell, match, allKey, adherentInfo);
									if (!adherent) {
										// 最后再原始方式匹配
										replaceCellData(match, allKey, entity, oneCell, beanProduceParamMap);
									}

								}
							}

						}
					}
				}
			}
			// 删除过滤掉的
			for (Integer remove : removeIndexChain) {
				resultWb.removeSheetAt(remove);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				templateIs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultWb;
	}

	/**
	 * 附着属性开头的先解析
	 * 
	 * @param beanProduceParamMap
	 * @param adherentKeys
	 * @param oneCell
	 * @param match
	 * @param allKey
	 * @return 是否被匹配到
	 */
	private boolean swap4StartAdherent(Map<String, BaseProduceParam> beanProduceParamMap, Set<String> adherentKeys, Cell oneCell, String match, String allKey, Map<String, Object> adherentInfo) {
		// 扩展属性有可能中间匹配
		int inl = allKey.indexOf("[");
		String canKey = null;
		if (inl != -1) {
			int ind = allKey.indexOf(".");
			if (inl > ind) {
				canKey = allKey.substring(0, allKey.substring(0, inl).lastIndexOf("."));
			}
		} else {
			canKey = allKey;
		}
		if (canKey != null) {
			String[] split = canKey.split("\\.");
			String key = "";
			for (String s : split) {
				key += s;
				if (adherentKeys.contains(key)) {
					String ak = allKey.substring(key.length() + 1);
					Object data = adherentInfo.get(key);
					replaceCellData(match, ak, data, oneCell, beanProduceParamMap);
					return true;
				}
				key += ".";
			}
		}
		return false;
	}

	/**
	 * 使用附着信息赋值
	 * 
	 * @param beanProduceParamMap
	 * @param oneCell
	 * @param match
	 * @param allKey
	 */
	private void swap4AdherentInfo(Map<String, BaseProduceParam> beanProduceParamMap, Cell oneCell, String match, String allKey, Map<String, Object> adherentInfo) {
		BaseProduceParam beanProduceParam = beanProduceParamMap.get(allKey);
		if (beanProduceParam == null) {
			beanProduceParam = new BeanProduceParam();
			beanProduceParamMap.put(allKey, beanProduceParam);
		}

		Object ad = adherentInfo.get(allKey);
		CellData cellData = new CellData(ad == null ? beanProduceParam.getNullStr() : ad);

		// 赋值
		swapCell.swap(oneCell, cellData, match);
	}

	/**
	 * 为模板填充数据
	 *
	 * @param match  匹配到的key
	 * @param allKey 被匹配的所有的key
	 * @param cell   填充的单元格
	 * @return 最终匹配到的值是不是空
	 */
	private void replaceCellData(String match, String allKey, Object e, Cell cell, final Map<String, BaseProduceParam> field2ParamCache) {
		if (e == null) {
			// 第一次进入没有,则直接赋值null
			// 这里有bug就是如果早早的没了怎没有了这个体现
			swapCell.swap(cell, null, match);
			return;
		}
		List<String> caseFlow = Arrays.asList(allKey.split("\\."));
		// 匹配流
		Object current = e;
		Class currentType = e.getClass();
		boolean notNull = true;// 定义是否在中间就断了
		final int level = caseFlow.size();
		for (int i = 0; i < level; i++) {
			String caseOne = caseFlow.get(i);
			boolean isOne = true;
			String main = caseOne;
			int in = -1;
			int start = caseOne.indexOf("[");
			if (start != -1) {
				isOne = false;
				int end = caseOne.indexOf("]");
				in = Integer.parseInt(caseOne.substring(start + 1, end));
				main = caseOne.substring(0, start);
			}
			// 先判断是否是最后一级数据
			if (i == level - 1) {// 最后一级
				CellData cellData;
				// 去除所有数字为缓存的key
				String cacheKey = allKey.replaceAll("\\[\\d+]", "[]");
				BaseProduceParam p = field2ParamCache.get(cacheKey);
				if (p == null) {
					// 解析参数
					p = deduceProduceParam(cacheKey, currentType, main);
					field2ParamCache.put(cacheKey, p);
				}
				if (notNull) {
					cellData = ProduceUtil.key2CellDataConvertArr(p, current, caseOne);
				} else {// 空数据
					cellData = new CellData(p.getNullStr());
				}
				// 赋值
				swapCell.swap(cell, cellData, match);
				if (modify != null) {
					modify.modify(cell, cellData, allKey, currentType, main);
				}
			} else {// 中间级
				// 首先遍历类型
				currentType = processClass(current, currentType, isOne, main);
				// 如果上次不是空,便利值
				if (notNull) {
					if (isOne) {
						current = ProduceUtil.getValFromObjectOrMap(current, main);
					} else {
						current = getListVal(current, main, in);
					}
					// 判断notNull
					notNull = current != null;
				}
			}
		}
	}

	/**
	 * 推断构建参数
	 * 
	 * @param cacheKey
	 * @param currentType
	 * @param main
	 * @return
	 */
	private BaseProduceParam deduceProduceParam(String cacheKey, Class currentType, String main) {
		BeanProduceParam param = new BeanProduceParam();
		if (convertMap != null && convertMap.containsKey(cacheKey)) {
			param.setProduceConvert(convertMap.get(cacheKey));
			return param;
		}

		try {
			Field currentField = ReflectionUtils.findField(currentType, main);
			return ProduceUtil.field2BaseProduceParam(currentField);
		} catch (Exception e) {
		}
		return param;
	}

	/**
	 * 获取集合属性
	 *
	 * @param current 对象
	 * @param main    属性名
	 * @param in      下标
	 * @return
	 */
	private Object getListVal(Object current, String main, int in) {
		try {
			List o = (List) ProduceUtil.getValFromObjectOrMap(current, main);
			if (CollectionUtils.isEmpty(o) || o.size() < in + 1) {
				current = null;
			} else {
				current = o.get(in);
			}
		} catch (Exception e1) {
			throw new RuntimeException("获取集合属性失败,实体:" + current + "属性:" + main + ",信息" + e1.getMessage());
		}
		return current;
	}

	/**
	 * 迭代类型
	 *
	 * @param currentClass 当前类型
	 * @param isOne        是不是但
	 * @param main         属性名
	 * @return
	 */
	private Class processClass(Object current, Class currentClass, boolean isOne, String main) {
		if (currentClass == null) {
			return null;
		}
		try {
			Field currentField = ReflectionUtils.findField(currentClass, main);
			Assert.notNull(currentField, "获取属性失败,类:" + current.getClass() + "属性:" + main);
			if (isOne) {
				currentClass = currentField.getType();
			} else {
				Class[] cs = ReflectUtil.getFieldActualType(currentField);
				if (cs.length == 1) {
					currentClass = cs[0];
				} else {
					Object property = PropertyUtils.getProperty(current, main);
					if (property instanceof List && ((List) property).size() > 0) {
						currentClass = ((List) property).get(0).getClass();
					}
				}
			}
		} catch (Exception e) {
			if (current instanceof Map) {
				Object o = ((Map) current).get(main);
				if (o != null) {
					if (isOne) {
						currentClass = o.getClass();
					} else {
						if (o instanceof List && ((List) o).size() > 0) {
							currentClass = ((List) o).get(0).getClass();
						}
					}
				}
			}
		}
		return currentClass;
	}

	/**
	 * 解析字符串是否匹配指定规则并将结果取出返回 已经取出中间多余的空格 分段匹配,,
	 *
	 * @param templateVal                   模板值
	 * @param removePrefixAndSuffixAndBlank 是否去除左右包边和中间空格
	 * @return 匹配不到返回空set
	 */
	private Set<String> matchAndReturn(String templateVal, boolean removePrefixAndSuffixAndBlank) {
		Matcher mr = ExcelTemplateProduceServer.allMatch.matcher(templateVal);
		Set<String> set = new HashSet<String>();
		while (mr.find()) {
			String value = mr.group();
			if (removePrefixAndSuffixAndBlank) {
				set.add(removePrefixAndSuffixAndBlank(value));
			} else {
				set.add(value);
			}
		}
		return set;
	}

	/**
	 * 去除匹配的外框 并去除所有的空格
	 *
	 * @param match
	 * @return
	 */
	private String removePrefixAndSuffixAndBlank(String match) {
		return StringUtils.deleteWhitespace(match.substring(2, match.length() - 1));
	}

	public ProduceModify getModify() {
		return modify;
	}

	public void setModify(ProduceModify modify) {
		this.modify = modify;
	}

	public ProduceSkip getSkip() {
		return skip;
	}

	public void setSkip(ProduceSkip skip) {
		this.skip = skip;
	}

	public Map<String, ProduceConvert> getConvertMap() {
		return convertMap;
	}

	public void setConvertMap(Map<String, ProduceConvert> convertMap) {
		this.convertMap = convertMap;
	}

}
