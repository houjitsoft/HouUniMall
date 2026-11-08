package com.houjiang.commons.vo.goodscat;

import lombok.Data;

import java.util.List;
/**
 * @ClassName GoodsCatSearchChildVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsCatSearchChildVO {

	private List<WebCatMenuVO> allMenu;

	private List<Integer> catIds;



}
