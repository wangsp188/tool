//import wang.excel.ExcelProduceUtil;
//import wang.excel.common.model.CellData;
//import wang.excel.template.produce.ExcelTemplateProduceServer;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.*;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.swing.filechooser.FileSystemView;
//import java.io.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class TemplateProduceTest {
//
//
//    //将要构建的工作簿
//    Workbook workbook;
//    //输出地址---相当于下载地址
//    String outPath ;
//
//    //test之前
//    @Before
//    public void before() {
//        FileSystemView fsv = FileSystemView.getFileSystemView();
//        //桌面
//        File home = fsv.getHomeDirectory();
//        File dic = new File(home.getAbsolutePath() + "/export");
//        if(!dic.exists()){
//            dic.mkdir();
//        }
//        //查看所有文件数量
//        int number = dic.list().length+1;
//        outPath = dic.getAbsolutePath()+"/"+number+".xls";
//    }
//
//    //test之后
//    @After
//    public void after() throws IOException {
//        //写出文件到桌面模拟构建操作
//        FileOutputStream outputStream = new FileOutputStream(new File(outPath));
//        //写出去
//        workbook.write(outputStream);
//    }
//
//
//    @Test
//    public void t3() throws FileNotFoundException {
//        //数据构建
//        PersonHouse personHouse = new PersonHouse("我的",18,1);
//        final ArrayList<House> houses = new ArrayList<>();
//        houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));
//        houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));houses.add(new House("位置1234456","我自己"));
//        houses.add(new House("位置2","我自己2"));
//
//
//
//
//
//
//        personHouse.setHouses(houses);
////        personHouse.setImg("C:\\Users\\Administrator\\Desktop\\微信截图_20200411151721.png");
//
//        //模板?
//        FileInputStream templateStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\tem.xls");
//
//
//        //模板构建工具
//        ExcelTemplateProduceServer server = new ExcelTemplateProduceServer();
//
//
//        Map<String, Object> exportExtend = new HashMap<>();
//        exportExtend.put("woaini",new CellData("我爱你"));
//
//
//        workbook = server.create(templateStream,personHouse,exportExtend);
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    @Test
//    public void t2() throws FileNotFoundException {
//        //模板构建,,,,,
//        //定义模板
//        //数据
//        //表格
//
//        //下标和key的映射
//        Map<String, String> map =new HashMap<>();
//        map.put("0_1_3","name");
//        map.put("0_1_4","age");
//
//        Map<String, String> data = new HashMap();
//        data.put("name","花花");
//        data.put("age","18");
//
//        workbook = export(map,data);
//    }
//
//    private Workbook export(Map<String, String> map, Map<String, String> data) {
//        HSSFWorkbook workbook = new HSSFWorkbook();
//        for (String s : map.keySet()) {
//            String[] indexs = s.split("_");
//            int sheetIndex = Integer.parseInt(indexs[0]);
//            int rowIndex = Integer.parseInt(indexs[1]);
//            int colIndex = Integer.parseInt(indexs[2]);
//
//            Sheet sheet;
//            try {
//                sheet = workbook.getSheetAt(sheetIndex);
//            } catch (Exception e) {
//               sheet = workbook.createSheet(sheetIndex+"");
//            }
//            Row row = sheet.getRow(rowIndex);
//            if(row==null){
//                row = sheet.createRow(rowIndex);
//            }
//            Cell cell = row.createCell(colIndex);
//            cell.setCellValue(data.get(map.get(s)));
//        }
//        return workbook;
//    }
//
//
//    @Test
//    public void t1() throws IOException, InvalidFormatException {
//        List<Person> list = new ArrayList<>();
//        list.add(new Person("这是",18,"1"));
//        list.add(new Person("这是2",19,"2"));
//
//
//        Workbook workbook2 = WorkbookFactory.create(
//                new File("C:\\Users\\Administrator\\Desktop\\title.xls")
//        );
//        Sheet sheet = workbook2.getSheetAt(0);
//
//        String[] fields = {"name","age","sex"};
//
//
//        //清单构建
//        workbook = ExcelProduceUtil.listProduce(
//                sheet,//表格头部的模板sheet页
//                Person.class//哪个类
//
//                ,fields
//
//                ,null//自定义构建逻辑
//                ,list//数据集合
//        );
//    }
//
//
//
//
//
//
//
//}
