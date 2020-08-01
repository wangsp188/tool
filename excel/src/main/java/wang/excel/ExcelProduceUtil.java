package wang.excel;

import wang.excel.advanced.compose.ExcelComposeServer;
import wang.excel.advanced.compose.model.WorkbookPart;
import wang.excel.common.iwf.ProduceConvert;
import wang.excel.common.iwf.WorkbookType;
import wang.excel.common.model.BaseProduceParam;
import wang.excel.normal.produce.ExcelNormalProduceServer;
import wang.excel.normal.produce.iwf.SheetModule;
import wang.excel.normal.produce.iwf.impl.*;
import wang.excel.template.produce.ExcelTemplateProduceServer;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 超级无敌
 * 
 */
public class ExcelProduceUtil {


	/**
	 * 模板构建 实体-模版实现
	 * 
	 * @param describe
	 * @param t
	 * @return
	 */
	public static <T> Workbook templateProduce(InputStream template, T t) {
		return new ExcelTemplateProduceServer().create(template,t,null);
	}

	/**
	 * 构建对象列表
	 *  @param response 响应
	 * @param downName 下载名(不要后缀)
	 * @param <T>
	 * @param datas    数据
	 * @param type     类型
	 * @param title    标题
	 */
	public static <T> Workbook listProduce(List<T> datas, Class<T> type, String title) {
		SheetModule module = new SimpleBeanSheetModule<T>(type, datas, title);
		ExcelNormalProduceServer server = new ExcelNormalProduceServer(module);
		return  server.produce();
	}

	/**
	 * 默认构建,带有构建修饰的功能
	 * 
	 * @param datas            数据
	 * @param type             类型
	 * @param title            标题
	 * @param produceConvertMap 构建修饰map
	 * @param <T>
	 */
	public static <T> Workbook listProduce(List<T> datas, Class<T> type, String title, final Map<String, ProduceConvert> produceConvertMap) {
		if (produceConvertMap == null) {
			SheetModule module = new SimpleBeanSheetModule<T>(type, datas, title);
			ExcelNormalProduceServer server = new ExcelNormalProduceServer(module);
			return server.produce();
		} else {
			// 构建修饰定义
			WrapO2CellMiddleware<T> wrapO2Cell = new WrapO2CellMiddleware<>();
			wrapO2Cell.setParam((wraped, key) -> {
				BaseProduceParam param = wraped.param(key);
				ProduceConvert produceConvert = produceConvertMap.get(key);
				if (produceConvert != null) {
					param.setProduceConvert(produceConvert);
				}
				return param;
			});

			SheetModule module = new SimpleBeanSheetModule<>(type, datas, title, null, null, wrapO2Cell);

			return new ExcelNormalProduceServer(module).produce();
		}

	}

	/**
	 * 固定头部模版的列表构建
	 * 
	 * @param titleTemplate    模版所在流 取第一个sheet
	 * @param type             类型
	 * @param fields           字段数组
	 * @param produceConvertMap 需要覆盖注解的自定义操作
	 * @param datas            数据集合
	 * @param <T>              数据集合
	 */
	public static <T> Workbook listProduce(Sheet titleTemplate, Class<T> type, String[] fields, final Map<String, ProduceConvert> produceConvertMap, List<T> datas) {

		Assert.notNull(titleTemplate, "模版流不可为空");
		Assert.notNull(type, "类型不可为空");
		// 拼接
		ExcelNormalProduceServer server = new ExcelNormalProduceServer();
		// 添加标题块
		TemplateTitleModule titleModule = new TemplateTitleModule();
		titleModule.setTemplateSheet(titleTemplate);
		server.addModule(titleModule);
		// 根据模版定义初始化类型
		if (titleTemplate instanceof XSSFSheet) {
			server.setWorkbookType(WorkbookType.XSSF.getType());
		} else if (titleTemplate instanceof HSSFSheet) {
			server.setWorkbookType(WorkbookType.HSSF.getType());
		} else {
			throw new RuntimeException("不支持的模版类型");
		}

		// 内容部分
		O2CellMiddlewareBodyModule<T> bodyModule = new O2CellMiddlewareBodyModule<>(type, fields, null);
		bodyModule.setDatas(datas);
		bodyModule.setNeedHead(false);
		// 自定义设置
		if (produceConvertMap != null) {
			// 构建修饰定义
			WrapO2CellMiddleware<T> wrapO2Cell = new WrapO2CellMiddleware<>();
			wrapO2Cell.setParam((wraped, key) -> {
				BaseProduceParam param = wraped.param(key);
				ProduceConvert produceConvert = produceConvertMap.get(key);
				if (produceConvert != null) {
					param.setProduceConvert(produceConvert);
				}
				return param;
			});
			// 存有原始被修饰对象
			wrapO2Cell.setWraped(bodyModule.getMiddleware());
			// 替代原有被修饰对象
			bodyModule.setMiddleware(wrapO2Cell);
		}
		server.addModule(bodyModule);

		// 生成
		return server.produce();
	}

	/**
	 * 构建对象列表(制定属性名列表或过滤列表)
	 * @param datas      数据
	 * @param type       类型
	 * @param title      标题
	 * @param includes   指定属性名
	 * @param excludes   过滤属性名
	 * @param wrapO2Cell 修饰
	 * @param <T>
	 */
	public static <T> Workbook listProduce(List<T> datas, Class<T> type, String title, String[] includes, String[] excludes, WrapO2CellMiddleware<T> wrapO2Cell) {
		SheetModule module = new SimpleBeanSheetModule<T>(type, datas, title, includes, excludes, wrapO2Cell);
		return new ExcelNormalProduceServer(module).produce();
	}

	/**
	 * 构建Map集合
	 * 
	 * @param datas  数据
	 * @param title  表格标题
	 * @param keys   map中的哪些key
	 * @param titles map中key对应的标题
	 * @param params map中key对应的构建参数
	 */
	public static Workbook mapProduce(List<Map<String, Object>> datas, String title, String[] keys, Map<String, String> titles, Map<String, BaseProduceParam> params) {
		SheetModule module = new SimpleMapSheetModule(datas, title, keys, titles, params, null);
		return new ExcelNormalProduceServer(module).produce();
	}


	/**
	 * 构建对象列表
	 * 
	 * @param datas         数据
	 * @param type          类型
	 * @param title         标题
	 * @param wrapO2CellMap 修饰map 如果修饰主实体,key是空字符串
	 * @param <T>
	 */
	public static <T> Workbook nestlistProduce(List<T> datas, Class<T> type, String title, Map<String, WrapO2CellMiddleware<T>> wrapO2CellMap) {
		SheetModule module = new SimpleBeanSheetModule<T>(type, datas, title, null, new ArrayList<>(), wrapO2CellMap);
		return new ExcelNormalProduceServer(module).produce();
	}

	/**
	 * 组合拼接工作簿
	 * 
	 * @param workbooks 工作簿源数组
	 * @return
	 */
	public static Workbook composeWorkbook(Workbook... workbooks) {
		try {
			ExcelComposeServer server = new ExcelComposeServer();
			if (workbooks != null) {
				Class cz = null;
				WorkbookType type = null;
				for (final Workbook workbook : workbooks) {
					Class<? extends Workbook> currCls = workbook.getClass();
					if (cz == null) {
						cz = currCls;
						if (cz.equals(HSSFWorkbook.class)) {
							type = WorkbookType.HSSF;
						} else if (cz.equals(XSSFWorkbook.class)) {
							type = WorkbookType.XSSF;
						} else {
							throw new IllegalArgumentException("不支持的表格类型");
						}
					} else if (cz != currCls) {
						throw new IllegalArgumentException("工作簿类型不统一");
					}
					// 添加
					server.addPart(new WorkbookPart(workbook));
				}
				// 类型赋值
				if (type != null) {
					server.setWorkbookType(type);
				}
			}
			// 构建
			return server.compose();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
