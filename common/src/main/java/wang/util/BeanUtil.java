package wang.util;

import java.io.*;

public class BeanUtil {

	/**
	 * 将字节数据读取为对象
	 *
	 * @param bytes
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T> T byteArr2Object(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(arrayInputStream);
			return (T) objectInputStream.readObject();
		} catch (Exception e) {
			throw new RuntimeException("反序列化失败", e);
		}
	}

	/**
	 * 将对象转换为字节数组
	 *
	 * @param obj 对象 @return @throws
	 */
	public static byte[] object2ByteArr(Object obj) {
		if (obj == null) {
			return null;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException("序列化失败!", e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * 通过序列化clone对象
	 * 
	 * @param t
	 * @param <T>
	 * @return
	 */
	public static <T> T clone(T t) {
		byte[] bytes = object2ByteArr(t);
		return byteArr2Object(bytes);
	}

}
