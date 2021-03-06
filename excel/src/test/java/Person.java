import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PictureData;
import wang.excel.common.iwf.Excel;

import java.util.List;
public class Person {

	@Excel(name = "姓名", width = 60)
	private String name;

	// 每一属性都有对象的解析参数
	@Excel(name = "年龄", order = 2, nullStr = "不知道多大")
	private Integer age;

	@Excel(name = "性别")
	private Sex sex;

	// 如果是图片,首先这个列是地址(图片访问地址)
	@Excel(name = "头像", height = 30)
	private String img;

	public Person(String name, Integer age, Sex sex) {
		this.name = name;
		this.age = age;
		this.sex = sex;
	}

	public Person() {
	}

	private String importSex(Cell cell, List<PictureData> imgs) {
//        System.out.println(cell.getStringCellValue());
		return "只是性别";
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	@Override
	public String toString() {
		return "Person{" + "name=" + name + ", age=" + age + ", sex=" + sex + ", img=" + img + '}';
	}
}
