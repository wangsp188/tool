package wang.excel.normal.produce.iwf.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import wang.excel.common.iwf.SwapCell;
import wang.excel.common.iwf.impl.SimpleSwapCell;
import wang.excel.common.model.BaseListProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.normal.produce.iwf.CellPostProcessor;
import wang.excel.normal.produce.iwf.CellStyleDefine;
import wang.excel.normal.produce.iwf.O2CellMiddleware;
import wang.excel.normal.produce.iwf.SheetModule;

/**
 * 实体 主体拼接
 * 
 * @param <T>
 */
public class O2CellMiddlewareBodyModule<T> extends SheetModule.Body {
	private static final Logger log = LoggerFactory.getLogger(O2CellMiddlewareBodyModule.class);
	/**
	 * 数据单元格中间件
	 */
	protected O2CellMiddleware<T> middleware;

	/**
	 * 集合数据
	 */
	protected List<T> data;

	/**
	 * 单元格后置
	 */
	protected List<CellPostProcessor> cellPostProcessors;

	/**
	 * 赋值给单元格的实现
	 */
	protected SwapCell swap;

	/**
	 * 空数据的单元格样式
	 */
	protected CellStyleDefine noneStyle;
	/**
	 * 表头单元格样式
	 */
	protected CellStyleDefine headCellStyle;

	/**
	 * 是否需要标题首行
	 */
	protected boolean needHead;

	/**
	 * 默认属性
	 */
	{
		this.needHead = true;
		this.noneStyle = new NoneDataStyle();
		this.headCellStyle = new HeadStyle();
		// 默认添加普通单元格样式
		this.cellPostProcessors = new ArrayList<>();
		this.cellPostProcessors.add(new CellStylePostProcessor(new NormalCellStyle()));
		this.swap = new SimpleSwapCell();
	}

	public O2CellMiddlewareBodyModule(List<T> ts, O2CellMiddleware<T> middleware, SwapCell swap) {
		super();
		Assert.notNull(middleware, "middleware不可为空");
		Assert.notNull(swap, "swap不可为空");
		this.data = ts;
		this.middleware = middleware;
		this.swap = swap;
	}

	public O2CellMiddlewareBodyModule(Class<T> type, String[] includes, String[] excludes) {
		super();
		this.middleware = new WrapIndex4Middleware<>(new Bean2CellMiddleware<>(type, includes, excludes));
	}

	public O2CellMiddlewareBodyModule() {
		super();
	}

	/**
	 * 返回总宽度
	 * 
	 * @return
	 */
	public int colSize() {
		if (middleware != null && middleware.keys() != null) {
			return middleware.keys().length;
		}
		return 0;
	}

	@Override
	public void sheet(Sheet sheet) {
		Assert.notNull(middleware, "middleware不可为空");
		Assert.notNull(swap, "swap不可为空");
		// 先创建表头
		String[] keys = middleware.keys();
		Workbook wb = sheet.getWorkbook();

		if (ArrayUtils.isNotEmpty(keys) && needHead) {
			int num;
			if (sheet.getRow(0) == null) {
				num = -1;
			} else {
				num = sheet.getLastRowNum();
			}
			Row row = sheet.createRow(num + 1);

			for (int i = 0; i < keys.length; i++) {
				Cell cell = row.createCell(i);
				cell.setCellValue(middleware.title(keys[i]));
				if (headCellStyle != null) {
					cell.setCellStyle(headCellStyle.style(wb));
				}

				// 设置宽度
				BaseListProduceParam ep = middleware.param(keys[i]);
				if (ep != null) {
					// 单元格宽度
					sheet.setColumnWidth(i, (int) (256 * ep.getWidth()));
				}
			}
			// 头行高
			row.setHeight((short) 450);
		}
		// 执行的基础条件,有数据
		if (!CollectionUtils.isEmpty(data) && ArrayUtils.isNotEmpty(keys)) {
			// 先计算最大高度,默认10
			short maxHeight = 10;
			for (String key : middleware.keys()) {
				BaseListProduceParam bp = middleware.param(key);
				if (bp != null) {
					short height = bp.getHeight();
					maxHeight = (short) Math.max(height, maxHeight);
				}
			}
			// 固定算法(其实是20不过默认值是10 又有点太低了,所以再乘 1.5)
			maxHeight *= 20 * 1.5;

			// 这是先遍历某一列再继续下一列
			int oldRowNum;
			if (sheet.getRow(0) == null) {
				oldRowNum = -1;
			} else {
				oldRowNum = sheet.getLastRowNum();
			}
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];

				// 定义行编号
				int rowNum = oldRowNum;
				// 定义行号
				int index = -1;
				for (T t : data) {
					// 定义行号
					index++;
					try {
						Row row;
						// 第一列创建新的行
						if (i == 0) {
							row = sheet.createRow(++rowNum);
							// 如果不是默认高度,则主动设置高度
							row.setHeight(maxHeight);
						} else {
							row = sheet.getRow(++rowNum);
						}
						Cell cell = row.createCell(i);
						// 单元格操作
						if (!CollectionUtils.isEmpty(cellPostProcessors)) {
							for (CellPostProcessor c : cellPostProcessors) {
								c.cell(cell);
							}
						}
						CellData data = null;
						if (StringUtils.isNotEmpty(key)) {
							// 赋值
							data = middleware.data(t, key, index);
						}
						if (CellData.isEmpty(data) && noneStyle != null) {
							cell.setCellStyle(noneStyle.style(wb));
						}
						swap.swap(cell, data, null);
					} catch (Exception e) {
						log.info("单元格赋值失败," + e.getMessage());
					}
				}
			}

		}
	}

	public O2CellMiddleware<T> getMiddleware() {
		return middleware;
	}

	public void setMiddleware(O2CellMiddleware middleware) {
		this.middleware = middleware;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public List<CellPostProcessor> getCellPostProcessors() {
		return cellPostProcessors;
	}

	public void setCellPostProcessors(List<CellPostProcessor> cellPostProcessors) {
		this.cellPostProcessors = cellPostProcessors;
	}

	public SwapCell getSwap() {
		return swap;
	}

	public void setSwap(SwapCell swap) {
		this.swap = swap;
	}

	public boolean isNeedHead() {
		return needHead;
	}

	public void setNeedHead(boolean needHead) {
		this.needHead = needHead;
	}

}
