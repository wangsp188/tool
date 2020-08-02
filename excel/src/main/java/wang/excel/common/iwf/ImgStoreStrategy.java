package wang.excel.common.iwf;

import java.io.File;
import java.util.List;

import org.apache.poi.ss.usermodel.PictureData;

/**
 * excel图片上传保存策略
 */
public interface ImgStoreStrategy {

	/**
	 * 上传图片返回key
	 * 
	 * @param pictureData
	 * @param originalName
	 * @return
	 */
	String uploadReturnKey(PictureData pictureData, String originalName);

	/**
	 * 根据key返回对应图片文件
	 * 
	 * @param key
	 * @return
	 */
	List<File> recoverKey2Files(String key);

}
