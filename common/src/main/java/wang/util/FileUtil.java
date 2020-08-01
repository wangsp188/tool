package wang.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class FileUtil {

    /**
     * 是不是绝对路径
     * @param path
     * @returni
     */
    public static boolean isAbsolutePath(String path) {
        Matcher mat = Pattern.compile("^\\w:[/\\\\].+|^/.+").matcher(path);
        return mat.matches();
    }

    /**
     * 是否是压缩文件
     * @param file
     * @return
     */
    public static boolean isZipFile(File file) {
        try {
            if (null == file) return false;
            new ZipFile(file);
            return true;
        } catch (IOException var2) {
            return false;
        }
    }

}
