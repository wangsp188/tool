import wang.excel.common.iwf.Excel;
import wang.excel.common.iwf.ImgProduce;
import wang.excel.common.iwf.NestExcel;

import java.util.List;


public class PersonHouse {


    @Excel(name = "姓名")
    private String name;

    @Excel(name = "年龄")
    private Integer age;

    @Excel(name = "性别" ,replace = {"1_男","2_女"})
    private Integer sex;

    //如果是图片,首先这个列是地址(图片访问地址)
    @Excel(name="头像", imgStoreStrategy = "absoule",height = 30, imgProduceStrategy = ImgProduce.adaptable)
    private String img;

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
    //域


    //单独的域1
    @NestExcel(name = "房子")
    private House houses;


    //单独的域2
    @NestExcel(name = "房子2")
    private List<House> houses2;




    public PersonHouse(String name, Integer age, Integer sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    public PersonHouse() {
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

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public House getHouses() {
        return houses;
    }

    public void setHouses(House houses) {
        this.houses = houses;
    }

    public List<House> getHouses2() {
        return houses2;
    }

    public void setHouses2(List<House> houses2) {
        this.houses2 = houses2;
    }
}
