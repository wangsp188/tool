package wang.excel.normal.produce.iwf;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 表格拼接模块
 */
public interface SheetModule {
	/**
	 * 拼接内容
	 * 
	 * @param sheet
	 */
	void sheet(Sheet sheet);

	/**
	 * 序号用于排序,实现类可自行实现
	 * 
	 * @return
	 */
	int getOrder();

	/**
	 * 头
	 */
	abstract class Title implements SheetModule {
		@Override
		public int getOrder() {
			return Integer.MIN_VALUE + 100;
		}
	}

	/**
	 * 身
	 */
	abstract class Body implements SheetModule {

		@Override
		public int getOrder() {
			return 0;
		}
	}

	/**
	 * 尾
	 */
	abstract class Foot implements SheetModule {
		@Override
		public int getOrder() {
			return Integer.MAX_VALUE - 100;
		}
	}

}
