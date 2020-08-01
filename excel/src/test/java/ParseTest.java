import wang.excel.ExcelParseUtil;
import wang.excel.common.model.ParseResult;
import wang.excel.normal.parse.iwf.Sheet2ParseParam;
import wang.excel.normal.parse.model.ParseParam;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ParseTest {

    Class type ;

    //文件根
    String root ;
    String name;


    //解析解果  每一个单元测试都会给他赋值
    ParseResult result;




    @Before
    public void before() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        //桌面
        File home = fsv.getHomeDirectory();
        root = home.getAbsolutePath() + "/import/";
    }

    @After
    public void after(){
        System.out.println("此次解析结果是:-------------------------");
        System.out.println(result);
    }

    /**
     * 常用属性演示
     * @throws IOException
     */
    @Test
    public void t1() throws IOException {
//        Workbook-----整个工作簿  excel文件 --就是个文件
//        ,Sheet-----其中的一个sheet页 ---根据sheet下表获取
//        ,Row-------一行 ------根据行下标获取
//        ,Cell;-----一个单元格 -----根据行列下标获取


        name="14.xls";

        type = Person.class;
        final ParseParam common = ParseParam.common(type);
//        ParseParam<T> p = new ParseParam<T>();
//        // 类啊
//        p.setNestType(cz);
//        // 第三行开始读数据
//        p.setStartRow(2);
//        // 标题选择器(标头行列数选择)
//        p.setTitleCellFinder(new SimpleTitleCellFinder(1, 0,new TitleCellFilter(){
//            @Override
//            public boolean filter(Cell cell, String cellVal) {
//                return cellVal.trim().equals("序号");
//            }
//        }));
//        // 单表
//        p.setOne2many(false);
//        // 注解解析字段
//        p.setCol2Field(new AnnotationCol2Field<T>(cz));
//        // 默认解析实现
//        p.setParse2Bean(SimpleParse2Bean.common());
//        // 最大9999个实体
//        p.setMaxParse(9999);

        Sheet2ParseParam sheet2ParseParam = new Sheet2ParseParam() {
            @Override
            public ParseParam parseParam(Sheet sheet) {
//                //获取sheet下标
//                int index = sheet.getWorkbook().getSheetIndex(sheet);
//                //仅读取第一个sheet
//                if (index == 0) {
//                    return common;
//                }
//                return common;
                return ParseParam.common(type);
            }
        };

        result = ExcelParseUtil.excelParse(new FileInputStream(root+name), sheet2ParseParam);
    }










    /**
     * 自定义解析逻辑
     * @throws IOException
     */
    @Test
    public void t2() throws IOException {
//        单表
//        name="6.xls";
//        type = Person.class;
//        final ParseParam common = ParseParam.common(type);
//
//
//        //自定义列的解析操作
//        common.registConvert("age", new ImportConvert() {
//            @Override
//            public Integer parse(Cell cell, List imgs) throws Exception {
//                return 12;
//            }
//        });


        name = "1.xls";
        type = PersonHouse.class;
        final  ParseParam common = ParseParam.commonNest(type);

//
//
//        common.registConvert("houses.owner", new ImportConvert() {
//            @Override
//            public String parse(Cell cell, List imgs) throws Exception {
//                return "这是哪儿";
//            }
//        });


        Sheet2ParseParam sheet2ParseParam = new Sheet2ParseParam() {
            @Override
            public ParseParam parseParam(Sheet sheet) {
                //获取sheet下标
                int index = sheet.getWorkbook().getSheetIndex(sheet);
                //仅读取第一个sheet
                if (index == 0) {
                    return common;
                }
                return null;
            }
        };
        result = ExcelParseUtil.excelParse(new FileInputStream(root+name), sheet2ParseParam);
    }



    /**
     * 不同sheet不同逻辑
     * @throws IOException
     */
    @Test
    public void t3() throws IOException {
        name="6.xls";
        type = Person.class;
        final ParseParam person = ParseParam.common(type);
        final ParseParam house = ParseParam.common(House.class);

        Sheet2ParseParam sheet2ParseParam = new Sheet2ParseParam() {
            @Override
            public ParseParam parseParam(Sheet sheet) {
                //获取sheet下标
                int index = sheet.getWorkbook().getSheetIndex(sheet);
                //第一个人
                if (index == 0) {
                    return person;
                }
                //第二个房子
                if (index == 1) {
                    return house;
                }
                return null;
            }
        };

        result = ExcelParseUtil.excelParse(new FileInputStream(root+name), sheet2ParseParam);
    }




}
