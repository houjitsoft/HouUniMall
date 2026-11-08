package com.houjiang.base.vo.index;

import lombok.Data;

/**
 * @ClassName WebIndexQueryVO
 * @Description 页面导航
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebSystemPlatformNavVO {
	/**
	 * 导航Id
	 */
	private Integer navId;
	/**
	 * 导航标题
	 */
	private String navTitle;
	/**
	 * 导航链接
	 */
	private String navUrl;
	/**
	 * 是否以新窗口打开，0为否，1为是，默认为0
	 */
	private Integer navNewOpen;
	/**
	 * 是否被推荐
	 */
	private Integer navIsRec;


}
