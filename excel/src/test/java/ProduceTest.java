import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import wang.excel.ExcelProduceUtil;
import wang.excel.common.iwf.ImgProduceStrategy;
import wang.excel.common.model.BaseListProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.normal.produce.ExcelNormalProduceServer;
import wang.excel.normal.produce.iwf.O2CellMiddleware;
import wang.excel.normal.produce.iwf.SheetModule;
import wang.excel.normal.produce.iwf.WrapO2CellData;
import wang.excel.normal.produce.iwf.impl.SimpleBeanSheetModule;
import wang.excel.normal.produce.iwf.impl.WrapO2CellMiddleware;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProduceTest {

	// 将要构建的工作簿
	Workbook workbook;
	// 数据集合
	List datas = new ArrayList();
	Class type;
	// 标题
	String title;
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
		outPath = dic.getAbsolutePath() + "/" + number + ".xls";
		// 定义标题
		title = String.format("测试-%d", number);
	}

	// test之后
	@After
	public void after() throws IOException {
		// 写出文件到桌面模拟构建操作
		FileOutputStream outputStream = new FileOutputStream(new File(outPath));
		// 写出去
		workbook.write(outputStream);
	}

	/**
	 * 单表 单元测试
	 * 
	 * @throws IOException
	 */
	@Test
	public void t1() {
//        类似此函数
//        ExportW.listExport(null,"下载",data,type,title);

		// 数据整理
		// 类
		type = Person.class;
		// 数据
		Person person = new Person("王少鹏", null, Sex.man);
		person.setImg("C:\\Users\\Administrator\\Desktop\\7.png");
		datas.add(person);
		datas.add(new Person("石春蕊", 111, Sex.man));

		// 定义普通 模块
		SheetModule module = new SimpleBeanSheetModule<T>(type, datas, title);
		// 填充
		ExcelNormalProduceServer server = new ExcelNormalProduceServer(module);

		// 构建直接执行方法
		workbook = server.produce();

	}

	/**
	 * 自定义构建列
	 * 
	 * @throws IOException
	 */
	@Test
	public void t4() {
//        类似此函数
//        ExportW.listExport(null,"下载",data,type,title);

		// 类
		type = Person.class;
		// 数据
		datas.add(new Person("王少鹏", 11, Sex.man));
		datas.add(new Person("石春蕊", 111, Sex.man));

		String[] includs = new String[] { "name", "age" };
		String[] excluds = new String[] { "age" };

		// 定义普通模块
		SheetModule module = new SimpleBeanSheetModule<T>(type, datas, title, null, excluds, new ArrayList());
		// 填充
		ExcelNormalProduceServer server = new ExcelNormalProduceServer(module);
		// 构建
		workbook = server.produce();
	}

	/**
	 * 多表
	 * 
	 * @throws IOException
	 */
	@Test
	public void t2() {
//        类似此函数
//        ExportW.one2ManylistExport(null,"下载",data,type,title,null);
		// 类
		type = PersonHouse.class;

		// 数据构建
		PersonHouse personHouse = new PersonHouse("我的", 18, 1);
		ArrayList<House> houses = new ArrayList<>();
		houses.add(new House("位置1", "我自己"));
//        houses.add(new House("位置2","我自己2"));

		personHouse.setHouses(new House("位置1", "我自己"));

		datas.add(personHouse);

		PersonHouse personHouse2 = new PersonHouse("我的2", 182, 2);
		ArrayList<House> houses2 = new ArrayList<>();
		houses2.add(new House("位置3", "我自己3"));
//        houses2.add(new House("位置4","我自己4"));

		personHouse2.setHouses(new House("位置3", "我自己3"));
		datas.add(personHouse2);

		personHouse.setHouses2(houses2);
		personHouse2.setHouses2(houses);

		// 自定义列操作

		WrapO2CellMiddleware middleware = new WrapO2CellMiddleware();
		middleware.setData(new WrapO2CellData<House>() {
			@Override
			public CellData data(O2CellMiddleware delegate, House o, String key, Integer index) {
				if (key.equals("location")) {
					return new CellData("这是哪儿");
				}
				return delegate.data(o, key, index);
			}
		});

		Map<String, WrapO2CellMiddleware> warpO2CellMiddlewareMap = new HashMap<>();
		warpO2CellMiddlewareMap.put("houses", middleware);

		// 主子模块
		SheetModule module = new SimpleBeanSheetModule(type, datas, title, null, new ArrayList(), warpO2CellMiddlewareMap);
		// 填充
		ExcelNormalProduceServer server = new ExcelNormalProduceServer(module);
		// 构建
		workbook = server.produce();
	}

	/**
	 * 自定义操作
	 */
	@Test
	public void t3() {

		// 类
		type = House.class;
		// 数据
		House e = new House("此时风味v111", "啊");
		e.setChanquan(10);
		datas.add(e);
		datas.add(new House("你在长撒长撒擦哪", "在哪"));

		// 自定义列操作 wrap
		WrapO2CellMiddleware middleware = new WrapO2CellMiddleware();
//
//        middleware.setData(new WarpO2CellData<House>() {
//            @Override
//            public CellData data(O2CellMiddleware warped, House o, String key, Integer index) {
//                if(key.equals("location")){
//                    return new CellData("这是哪儿");
//                }
//                return warped.data(o,key,index);
//            }
//        });

		// 定义普通模块
		SimpleBeanSheetModule module = new SimpleBeanSheetModule<T>(type, datas, title, null, null, middleware);

		// 填充
		ExcelNormalProduceServer server = new ExcelNormalProduceServer(module);
		// 构建
		workbook = server.produce();
	}

	@Test
	public void t5() {
		ArrayList<Map<String, Object>> data = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("name", "王少鹏" + i);
			map.put("age", ImgProduceStrategy.achoo_2);
			data.add(map);
		}
		Map<String, BaseListProduceParam> paramMap = new HashMap<>();
		BaseListProduceParam nameParam = new BaseListProduceParam();

		paramMap.put("name", new BaseListProduceParam("姓名"));
		paramMap.put("age", new BaseListProduceParam("不知道啥"));

		workbook = ExcelProduceUtil.mapProduce(data, "map导出", new String[] { "name", "age" }, paramMap);

	}

}
