package com.houjiang.commons.vo.base;

import lombok.Data;
/**
 * @ClassName WabSystemConfigHallVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WabSystemConfigHallVO {

	private String configKey;
	/**
	 * 配置值
	 */
	private String configValue;
	/**
	 * 操作类型，1关键字，2商品编号，3链接,4json字符串(shopId keyword url)5op_value不传值
	 */
	private String opType;

	private String opValue;

	private String configComment;



}
