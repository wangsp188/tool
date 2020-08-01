package wang.excel.template.parse;

import wang.util.FileUtil;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 配合 BeanTemplateServer<E> 实现模板解析构建的一致处理
 *
 * @author 10619
 *
 */
public enum ExcelTemplateParseDescribe {
	;


	/**
	 * 解析模板路径
	 */
	private String templatePath;

	/**
	 * 下载解析模板路径
	 */
	private String downTemplatePath;

	/**
	 * 说明
	 */
	private String desc;

	ExcelTemplateParseDescribe() {
	}

	ExcelTemplateParseDescribe(String templatePath, String downTemplatePath) {

		this.templatePath = templatePath;
		this.downTemplatePath = downTemplatePath;
	}

	ExcelTemplateParseDescribe(String templatePath, String downTemplatePath, String desc) {

		this.templatePath = templatePath;
		this.downTemplatePath = downTemplatePath;
		this.desc = desc;
	}

	/**
	 * 获取解析时模板源
	 *
	 * @return
	 * @throws FileNotFoundException 当模板路径是绝盘符路径又找不到时抛出
	 */
	public synchronized InputStream getTemplateInputStream() throws FileNotFoundException {
		InputStream is = null;
		if (FileUtil.isAbsolutePath(templatePath)) {
			is = new FileInputStream(new File(templatePath));
		} else {
			//TODO 根据系统环境获取
		}
		Assert.notNull(is, "解析模板源不可为空");
		return is;
	}

	/**
	 * 获取下载的模板源
	 *
	 * @return
	 * @throws FileNotFoundException 当模板路径是绝盘符路径又找不到时抛出
	 */
	public synchronized InputStream getDownInputStream() throws FileNotFoundException {
		InputStream is = null;
		if (FileUtil.isAbsolutePath(downTemplatePath)) {
			is = new FileInputStream(new File(downTemplatePath));
		} else {
            //TODO 根据系统环境获取
		}
		Assert.notNull(is, "模板下载源不可为空");
		return is;
	}


	public String getDesc() {
		return desc;
	}

	public String getDownTemplatePath() {
		return downTemplatePath;
	}


	public String getTemplatePath() {
		return templatePath;
	}

}
