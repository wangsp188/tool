import wang.excel.common.iwf.Excel;
import wang.excel.common.model.CellData;

public class House {
    @Excel(name = "居住地",width = 40)
    private String location;

    @Excel(name = "所有者",order = -1)
    private String owner;

    @Excel(name = "产权",order = 3)
    private Integer chanquan;



    public Integer getChanquan() {
        return chanquan;
    }

    public void setChanquan(Integer chanquan) {
        this.chanquan = chanquan;
    }

    public House(String location, String owner) {
        this.location = location;
        this.owner = owner;
    }

    public House() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "House{" +
                "location='" + location + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }

    private CellData own(House owner){
        return new CellData("我也不知道怎么说了"+owner);
    }
}
