package wang.excel.normal.produce.iwf.impl;

import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.excel.normal.produce.iwf.*;

/**
 * 表格木块拼接和表格模块语义化的整合
 */
public class SemanticModule implements SheetModule {
	private static Logger log = LoggerFactory.getLogger(SemanticModule.class);

	@Override
	public boolean sheet(Sheet sheet) {
		boolean r = true;
		try {
			if (this instanceof SheetSemantic) {
				if (this instanceof Title) {
					((Title) this).title(sheet);
				}
				if (this instanceof Body) {
					((Body) this).body(sheet);
				}
				if (this instanceof Foot) {
					((Foot) this).foot(sheet);
				}
			} else {
				r = this.overrideSheet(sheet);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("操作失败" + e.getMessage());
		}
		return r;
	}

	/**
	 * 重写的
	 * 
	 * @param sheet
	 * @return
	 */
	public boolean overrideSheet(Sheet sheet) {
		return true;
	}

	@Override
	public int compareTo(SheetModule o) {
		if (o == null) {
			return 1;
		}
		if (this instanceof SheetSemantic) {
			if (!(o instanceof SheetSemantic)) {
				return 1;
			}
			SheetSemantic mt = (SheetSemantic) this;
			SheetSemantic mo = (SheetSemantic) o;
			if (mt.tn != mo.tn) {
				return mt.tn - mo.tn;
			} else {
				return this.getOrder() - o.getOrder();
			}
		} else {
			if (o instanceof SheetSemantic) {
				return -1;
			}
			return this.getOrder() - o.getOrder();

		}
	}

	/**
	 * 获取排序信息
	 * 
	 * @return
	 */
	@Override
	public int getOrder() {
		return 1;
	}

}
