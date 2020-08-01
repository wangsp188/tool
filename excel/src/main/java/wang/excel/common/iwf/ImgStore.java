package wang.excel.common.iwf;

import org.apache.poi.ss.usermodel.PictureData;

import java.io.File;
import java.util.List;

public interface ImgStore {

    /**
     * 上传图片返回key
     * @param pictureData
     * @param originalName
     * @return
     */
    String uploadReturnKey(PictureData pictureData,String originalName);


    /**
     * 根据key返回对应图片文件
     * @param key
     * @return
     */
    List<File> recoverKey2Files(String key);

}
