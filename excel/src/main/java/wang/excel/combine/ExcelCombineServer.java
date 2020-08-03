package wang.excel.combine;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.excel.combine.iwf.WorkbookProcess;
import wang.excel.combine.model.WorkbookPart;
import wang.excel.common.iwf.SheetCopy;
import wang.excel.common.iwf.WorkbookType;
import wang.excel.common.iwf.impl.SimpleSheetCopy;
import wang.model.ResultSuper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 组合拼接workbook
 */
public class ExcelCombineServer {
	private static final Logger log = LoggerFactory.getLogger(ExcelCombineServer.class);

	/**
	 * 成员
	 */
	private List<WorkbookPart> parts;

	/**
	 * 目标类型
	 */
	private WorkbookType workbookType = WorkbookType.HSSF;

	/**
	 * 是否忽略构建异常
	 */
	private boolean ignoreBuildException = false;

	public ExcelCombineServer(List<WorkbookPart> parts, WorkbookType workbookType) {
		this.parts = parts;
		this.workbookType = workbookType;
	}

	public ExcelCombineServer() {
	}

	/**
	 * 添加组成部分
	 * 
	 * @param part
	 * @return
	 */
	public ExcelCombineServer addPart(WorkbookPart part) {
		if (parts == null) {
			parts = new ArrayList<>();
		}
		if (part != null) {
			parts.add(part);
		}
		return this;
	}

	/**
	 * 拼接工作簿
	 * 
	 * @return 拼接结果
	 * @throws 线程阻断异常,工作簿构建异常,sheet复制异常
	 */
	public Workbook combine() throws WorkbookProcess.WorkbookBuildException, SimpleSheetCopy.SheetCopyException {
		// 定义结果
		Workbook workbook;
		switch (workbookType) {
		case HSSF:
			workbook = new HSSFWorkbook();
			// 默认字体
			Font font1 = workbook.getFontAt((short) 0);
			font1.setCharSet(HSSFFont.DEFAULT_CHARSET);
			font1.setFontHeightInPoints((short) 12);
			font1.setFontName("宋体");//
			break;
		case XSSF:
			workbook = new XSSFWorkbook();
			// 默认字体
			Font font2 = workbook.getFontAt((short) 0);
			font2.setCharSet(HSSFFont.DEFAULT_CHARSET);
			font2.setFontHeightInPoints((short) 11);
			font2.setFontName("宋体");//
			break;
		default:
			throw new IllegalArgumentException("未知的工作簿类型");
		}
		if (!CollectionUtils.isEmpty(parts)) {
			// 创建代理对象
			createWorkbookProcessProxy();
			// 锁
			final CountDownLatch countDownLatch = new CountDownLatch(parts.size());
			// 多线程创建工作簿
			ResultSuper ok = createWorkbook(countDownLatch, !ignoreBuildException);
			// 候着
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 出现异常
			if (!ok.isComplete() && !ignoreBuildException) {
				throw new WorkbookProcess.WorkbookBuildException(ok.getErrMsg());
			}
			// 拼接工作簿
			doCombine(workbook);

		}
		return workbook;
	}

	/**
	 * 拼接工作簿
	 * 
	 * @param workbook 原始工作簿
	 * @throws Exception 复制表格异常
	 */
	private void doCombine(Workbook workbook) throws SimpleSheetCopy.SheetCopyException {
		Set<String> names = new HashSet<>();
		// 复制拼接
		for (WorkbookPart part : parts) {
			WorkbookProcess workbookBuilder = part.getWorkbookProcess();
			SheetCopy sheetCopy = part.getSheetCopy();
			// 构建
			Workbook build = null;
			try {
				build = workbookBuilder.build();
			} catch (WorkbookProcess.WorkbookBuildException ignored) {
			}
			// 不是null再说
			if (build != null) {
				int sheetSize = build.getNumberOfSheets();
				// 所有sheet
				for (int y = 0; y < sheetSize; y++) {
					Sheet sheet = build.getSheetAt(y);
					// 是否过滤
					if (!workbookBuilder.skip(sheet)) {
						// 命名
						String name = workbookBuilder.name(sheet);
						if (name == null) {
							name = sheet.getSheetName();
						}
						while (!names.add(name)) {
//						    name = name+1;
							// 有没有目标-
							int lastIndexOf = name.lastIndexOf("-");
							if (lastIndexOf != -1) {
								try {
									// 如果有
									String substring = name.substring(lastIndexOf + 1);
									// 尝试将后面解析成数字
									int i = Integer.parseInt(substring);
									name = name.substring(0, lastIndexOf + 1) + (i + 1);
								} catch (Exception e) {
									e.printStackTrace();
									name = name + "-2";
								}
							} else {
								name = name + "-2";
							}
						}
						// 创建
						Sheet workbookSheet = workbook.createSheet(name);
						// 复制
						sheetCopy.copySheet(sheet, workbookSheet);
					}

				}

			}

		}
	}

	/**
	 * 创建工作簿
	 * 
	 * @param countDownLatch 锁
	 * @param fastReturn     是否快速返回
	 * @return 创建结果
	 */
	private ResultSuper createWorkbook(final CountDownLatch countDownLatch, final boolean fastReturn) {
		// 线程池
		final ExecutorService executorService = Executors.newFixedThreadPool(Math.min(8, Long.valueOf(countDownLatch.getCount()).intValue()));

		final ResultSuper resultSuper = new ResultSuper();
		for (final WorkbookPart part : parts) {
			final WorkbookProcess workbookProcess = part.getWorkbookProcess();
			executorService.execute(() -> {
				try {
					workbookProcess.build();
				} catch (Exception e) {
					e.printStackTrace();
					// 记录错误信息
					String errMsg = resultSuper.getErrMsg() == null ? "" : resultSuper.getErrMsg();
					String name = part.getName();
					String message = e.getMessage();
					if (name != null || message != null) {
						if (name != null) {
							errMsg += name;
						}
						errMsg += "创建失败";
						if (message != null) {
							errMsg += message;
						}
						errMsg += "；";
					}
					if (StringUtils.isEmpty(errMsg)) {
						errMsg = "创建失败；";
					}
					resultSuper.setErrMsg(errMsg);
					// 如果补忽略异常就直接砍掉所有线程
					if (fastReturn) {
						// 解锁线程
						while (countDownLatch.getCount() > 0) {
							countDownLatch.countDown();
						}
						// 停止线程
						executorService.shutdownNow();
					}
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		return resultSuper;
	}

	/**
	 * 创建代理对象并返回workbook总数 排除无效的part部分
	 * 
	 * @return
	 */
	private void createWorkbookProcessProxy() {
		Iterator<WorkbookPart> iterator = parts.iterator();
		while (iterator.hasNext()) {
			WorkbookPart part = iterator.next();
			// 验证
			boolean can = checkPart(part);
			if (can) {
				WorkbookProcess workbookBuilder = part.getWorkbookProcess();
				// 构建代理
				WorkbookProcess proxy = (WorkbookProcess) Proxy.newProxyInstance(WorkbookProcess.class.getClassLoader(), new Class[] { WorkbookProcess.class }, new WrapWorkbookProcess(workbookBuilder));
				part.setWorkbookProcess(proxy);
			} else {
				iterator.remove();
			}
		}
	}

	/**
	 * 验证单个成员是否可行
	 * 
	 * @param part
	 * @return
	 */
	private boolean checkPart(WorkbookPart part) {
		return part != null && part.getWorkbookProcess() != null && part.getSheetCopy() != null;
	}

	public List<WorkbookPart> getParts() {
		return parts;
	}

	public void setParts(List<WorkbookPart> parts) {
		this.parts = parts;
	}

	public WorkbookType getWorkbookType() {
		return workbookType;
	}

	public void setWorkbookType(WorkbookType workbookType) {
		this.workbookType = workbookType;
	}

	public boolean isIgnoreBuildException() {
		return ignoreBuildException;
	}

	public void setIgnoreBuildException(boolean ignoreBuildException) {
		this.ignoreBuildException = ignoreBuildException;
	}

	/**
	 * 拦截获取workbook
	 */
	private static class WrapWorkbookProcess implements InvocationHandler {
		private final WorkbookProcess workbookProcess;
		private Workbook workbook;

		public WrapWorkbookProcess(WorkbookProcess workbookProcess) {
			this.workbookProcess = workbookProcess;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.equals(WorkbookProcess.class.getDeclaredMethod("build"))) {
				if (workbook != null) {
					return workbook;
				} else {
					try {
						workbook = (Workbook) method.invoke(workbookProcess);
					} catch (Exception e) {
						log.error("创建workbook失败,msg:{}",e.getMessage());
					}
					return workbook;
				}
			}
			// 执行
			return method.invoke(workbookProcess, args);
		}
	}

}
