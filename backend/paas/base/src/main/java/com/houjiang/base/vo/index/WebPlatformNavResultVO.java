package com.houjiang.base.vo.index;

import lombok.Data;

import java.util.List;
/**
 * @ClassName WebPlatformNavResultVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebPlatformNavResultVO {
	/**
	 * 导航
	 */
	private List<WebSystemPlatformNavVO> platformNav;
	/**
	 * 热搜词
	 */
	private List<String> hotWords;
}


