package wang.excel.normal.parse.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

import wang.excel.common.iwf.ParseConvert;
import wang.excel.normal.parse.impl.AnnotationCol2Field;
import wang.excel.normal.parse.impl.SimpleParse2Bean;
import wang.excel.normal.parse.impl.SimpleTitleCellFinder;
import wang.excel.normal.parse.iwf.Col2Field;
import wang.excel.normal.parse.iwf.Parse2Bean;
import wang.excel.normal.parse.iwf.Sheet2ParseParam;
import wang.excel.normal.parse.iwf.TitleCellFinder;

/**
 * 行式 解析时的参数封装
 * 
 * @author wangshaopeng
 */
public class ParseParam<T> implements Sheet2ParseParam {

	/**
	 * 需要解析的class[]必填[]
	 */
	private Class<T> typeClass;

	/**
	 * 标题单元格选择接口
	 */
	private TitleCellFinder titleCellFinder;

	/**
	 * 开始读取的行下标//[]必填[]
	 */
	private int startRow;

	/**
	 * 最大解析数量
	 */
	private int maxParse;

	/**
	 * 映射列和字段的功能接口,不可为空 必填
	 */
	private Col2Field col2Field;

	/**
	 * 解析一个实体 功能实现 不可为空 必填
	 */
	private Parse2Bean parse2Bean;

	/**
	 * 不可为空的字段名数组 该属性会覆盖掉Excel注解上的配置 若是指定子实体 则是 子实体在主实体属性名.子实体的属性名
	 *
	 */
	private String[] notNullArr;

	/**
	 * 是否是嵌套模型 此属性是true 需要 col2Field 和 parse2Bean 俩接口的实现支持
	 */
	private boolean nestModel;

	/**
	 * 自定义的解析接口
	 */
	private Map<String, ParseConvert> importConvertMap;

	private ParseParam() {
		super();
	}

	/**
	 * 返回一个默认参数的parse 建议初始化函数
	 *
	 * @return
	 */
	public static <T> ParseParam<T> common(Class<T> cz) {
		ParseParam<T> p = new ParseParam<>();
		// 类啊
		p.setTypeClass(cz);
		// 第三行开始读数据
		p.setStartRow(2);
		// 标题选择器(标头行列数选择)
		p.setTitleCellFinder(new SimpleTitleCellFinder(1, 0, (cell, cellVal) -> cellVal.trim().equals("序号")));
		// 普通实体
		p.setNestModel(false);
		// 注解解析字段
		p.setCol2Field(new AnnotationCol2Field<>(cz));
		// 默认解析实现
		p.setParse2Bean(SimpleParse2Bean.common());
		// 最大10000个实体
		p.setMaxParse(10000);
		return p;
	}

	/**
	 * 返回一个默认嵌套参数的parse 建议初始化函数
	 *
	 * @return
	 */
	public static <T> ParseParam<T> commonNest(Class<T> cz) {
		ParseParam<T> p = new ParseParam<>();
		// 类啊
		p.setTypeClass(cz);
		// 第三行开始读数据
		p.setStartRow(3);
		// 标题选择器(标头行列数选择)
		p.setTitleCellFinder(new SimpleTitleCellFinder(2, 0, (cell, cellVal) -> cellVal.trim().equals("序号")));
		// 嵌套表
		p.setNestModel(true);
		// 注解解析字段
		p.setCol2Field(new AnnotationCol2Field<>(cz));
		// 默认解析实现
		p.setParse2Bean(SimpleParse2Bean.common());
		// 最大10000个实体
		p.setMaxParse(10000);
		return p;
	}

	/**
	 * 注册解析自定义解析
	 *
	 * @param key          属性名,,如果是子实体的属性名 则前面加上 自己在父实体中的属性名 形如 childName.age
	 * @param parseConvert
	 */
	public void registConvert(String key, ParseConvert parseConvert) {
		if (importConvertMap == null) {
			importConvertMap = new HashMap<>();
		}
		importConvertMap.put(key, parseConvert);
	}

	/**
	 * 自实现接口,仅解析第一张表
	 *
	 * @param sheet
	 * @return
	 */
	@Override
	public ParseParam parseParam(Sheet sheet) {
		if (sheet.getWorkbook().getSheetIndex(sheet) == 0) {
			return this;
		}
		return null;
	}

	public Map<String, ParseConvert> getImportConvertMap() {
		return importConvertMap;
	}

	public void setImportConvertMap(Map<String, ParseConvert> importConvertMap) {
		this.importConvertMap = importConvertMap;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public String[] getNotNullArr() {
		return notNullArr;
	}

	public void setNotNullArr(String[] notNullArr) {
		this.notNullArr = notNullArr;
	}

	public Class<T> getTypeClass() {
		return typeClass;
	}

	public void setTypeClass(Class<T> typeClass) {
		this.typeClass = typeClass;
	}

	public Col2Field getCol2Field() {
		return col2Field;
	}

	public void setCol2Field(Col2Field col2Field) {
		this.col2Field = col2Field;
	}

	public int getMaxParse() {
		return maxParse;
	}

	public void setMaxParse(int maxParse) {
		this.maxParse = maxParse;
	}

	public Parse2Bean getParse2Bean() {
		return parse2Bean;
	}

	public void setParse2Bean(Parse2Bean parse2Bean) {
		this.parse2Bean = parse2Bean;
	}

	public boolean isNestModel() {
		return nestModel;
	}

	public void setNestModel(boolean nestModel) {
		this.nestModel = nestModel;
	}

	public TitleCellFinder getTitleCellFinder() {
		return titleCellFinder;
	}

	public void setTitleCellFinder(TitleCellFinder titleCellFinder) {
		this.titleCellFinder = titleCellFinder;
	}

	@Override
	public String toString() {
		return "ParseParam{" + "typeClass=" + typeClass + ", titleCellFinder=" + titleCellFinder + ", startRow=" + startRow + ", maxParse=" + maxParse + ", col2Field=" + col2Field + ", parse2Bean=" + parse2Bean + ", notNullArr=" + Arrays.toString(notNullArr) + ", nestModel=" + nestModel + ", importConvertMap=" + importConvertMap + '}';
	}

}
