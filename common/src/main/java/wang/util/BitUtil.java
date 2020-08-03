package wang.util;

/**
 * 位操作工具类
 */
public class BitUtil {


    /**
     * 将整型数字转换为二进制字符串，一共32位，不舍弃前面的0
     *
     * @param number 整型数字
     * @return 二进制字符串
     */
    public static String int2BitStr(int number) {
        return int2BitStr(number, 32);
    }
    /**
     * 将整型数字转换为二进制字符串，指定位数，不舍弃前面的0
     *
     * @param number 整型数字
     * @return 二进制字符串
     */
    public static String int2BitStr(int number,int bit) {
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < bit; i++) {
            sBuilder.append(number & 1);
            number = number >>> 1;
        }
        return sBuilder.reverse().toString();
    }
}
