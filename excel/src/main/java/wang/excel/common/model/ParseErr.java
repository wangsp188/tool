package wang.excel.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析时错误信息的保存实体
 * 
 * @author 10619
 *
 */
public class ParseErr extends ParseOneResult {

	private List<ErrInfo> errInfos;

	public ParseErr() {
		super();
	}

	public ParseErr(String errMsg) {
		this(null,null, null, errMsg);
	}

	public ParseErr(String sheetName,Integer rowNum, Integer colNum, String errMsg) {
		this();
		errInfos = new ArrayList<>();
		errInfos.add(new ErrInfo(sheetName, rowNum, colNum, errMsg));
	}

	/**
	 * 添加错误信息
	 * 
	 * @param rowNum
	 * @param colNum
	 * @param errMsg
	 * @return
	 */
	public ParseErr addErrInfo(String sheetName,Integer rowNum, Integer colNum, String errMsg) {
		if (errInfos == null) {
			errInfos = new ArrayList<>();
		}
		errInfos.add(new ErrInfo(sheetName, rowNum, colNum, errMsg));
		return this;
	}

	/**
	 * 获取错误量
	 * 
	 * @return
	 */
	public int errSize() {
		return errInfos == null ? 0 : errInfos.size();
	}

	/**
	 * 添加错误信息
	 * 
	 * @param rowNum
	 * @param colNum
	 * @param errMsg
	 * @return
	 */
	public ParseErr addErrInfo(ErrInfo info) {
		if (info != null) {
			if (errInfos == null) {
				errInfos = new ArrayList<>();
			}
			errInfos.add(info);
		}

		return this;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public String detail() {
		StringBuilder sb = new StringBuilder();
		String str = resource==null?null:resource.desc();
		if (str != null)
			sb.append(str);
		if (errInfos != null) {
			for (ErrInfo err : errInfos) {
				if (err != null) {
					sb.append(err.toString()).append("；");
				}
			}
		}
		return sb.toString();
	}

	public List<ErrInfo> getErrInfos() {
		return errInfos;
	}

	public void setErrInfos(List<ErrInfo> errInfos) {
		this.errInfos = errInfos;
	}

	// 错误信息描述
	public static class ErrInfo {
		private String msg;

		/**
		 * 表革命
		 */
		private String sheetName;

		/**
		 * 行下表
		 */
		private Integer rowNum;
		/**
		 * 列下标
		 */
		private Integer colNum;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(sheetName!=null){
				sb.append("表:").append(sheetName);
			}
			if (rowNum != null) {
				sb.append("第").append(rowNum + 1).append("行");
			}
			if (colNum != null) {
				sb.append("第").append(colNum + 1).append("列");
			}
			if (msg != null) {
				sb.append(msg);
			}
			return sb.toString();
		}

		public ErrInfo() {
		}

		public ErrInfo(String msg) {
			this.msg = msg;
		}

		public ErrInfo(String sheetName, int rowNum, int colNum, String msg) {
			this.sheetName = sheetName;
			this.msg = msg;
			this.rowNum = rowNum;
			this.colNum = colNum;
		}

	}


}
