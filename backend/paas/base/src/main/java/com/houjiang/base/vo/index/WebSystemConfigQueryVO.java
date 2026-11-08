package com.houjiang.base.vo.index;

import lombok.Data;

/**
 * @ClassName WebSystemConfigQueryVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebSystemConfigQueryVO {
	/**
	 * 操作类型，1关键字，2商品编号，3链接,4json字符串
	 */
	private String opType;
	/**
	 * 配置值
	 */
	private String configValue;
	/**
	 * 操作类型值
	 */
	private String opValue;


}
