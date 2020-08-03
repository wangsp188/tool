import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import wang.excel.combine.ExcelCombineServer;
import wang.excel.combine.iwf.WorkbookProcess;
import wang.excel.combine.model.WorkbookPart;
import wang.excel.common.iwf.SheetCopy;
import wang.excel.normal.produce.ExcelNormalProduceServer;
import wang.excel.normal.produce.iwf.SheetModule;
import wang.excel.normal.produce.iwf.impl.SimpleBeanSheetModule;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CombineTest {

	// 将要构建的工作簿
	Workbook workbook;
	// 输出地址---相当于下载地址
	String outPath;

	// test之前
	@Before
	public void before() {
		FileSystemView fsv = FileSystemView.getFileSystemView();
		// 桌面
		File home = fsv.getHomeDirectory();
		File dic = new File(home.getAbsolutePath() + "/export");
		if (!dic.exists()) {
			dic.mkdir();
		}
		// 查看所有文件数量
		int number = dic.list().length + 1;
		outPath = dic.getAbsolutePath() + "/" + number + ".xlsx";
		// 定义标题
	}

	// test之后
	@After
	public void after() throws IOException {
		// 写出文件到桌面模拟构建操作
		FileOutputStream outputStream = new FileOutputStream(new File(outPath));
		// 写出去
		workbook.write(outputStream);
	}

	@Test
	public void t1() throws WorkbookProcess.WorkbookBuildException, SheetCopy.SheetCopyException {

		Class type = Person.class;
		ArrayList<Object> datas = new ArrayList<>();
		// 数据
		Person person = new Person("王少鹏", null, Sex.man);
		person.setImg("C:\\Users\\Administrator\\Desktop\\7.png");
//        datas.add(person);
		for (int i = 0; i < 15000; i++) {
			datas.add(new Person("石春蕊", 111, Sex.man));
		}
//
//
//
//        //定义普通  模块
//        SheetModule module = new SimpleBeanSheetModule(type, datas, "你想干啥");
//        //填充
//        ExcelNormalProduceServer server2 = new ExcelNormalProduceServer(module);
//        //构建直接执行方法
//        Workbook workbook2 = server2.produce();

		// 定义普通 模块
		SheetModule module2 = new SimpleBeanSheetModule(type, datas, "你想干啥222222");
		// 填充
		ExcelNormalProduceServer server22 = new ExcelNormalProduceServer(module2);
//        server22.setWorkbookType(WorkbookType.XSSF);
		// 构建直接执行方法
		Workbook workbook22 = server22.produce();

		// 工作簿拼接工具
		ExcelCombineServer server = new ExcelCombineServer();
//        //添加模块1
//        server.addPart(new WorkbookPart(workbook2));
//        //添加模块2
//        server.addPart(new WorkbookPart(workbook22));
//        //添加模块2
//        server.addPart(new WorkbookPart(workbook22));

		for (int i = 0; i < 40; i++) {
			// 添加模块2
			server.addPart(new WorkbookPart(workbook22));
		}
//        server.setWorkbookType(WorkbookType.XSSF);

		workbook = server.combine();

	}

}
