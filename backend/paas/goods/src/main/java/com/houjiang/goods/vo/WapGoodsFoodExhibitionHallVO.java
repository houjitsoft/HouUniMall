package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class WapGoodsFoodExhibitionHallVO {

	private String exhibitionHallName;

	private String exhibitionHallImage;

	private String exhibitionHallLogo;

	private String exhibitionHallLogoWap;

	private String exhibitionHallDesc1;

	private String exhibitionHallDesc2;

	private String exhibitionHallDesc3;

	private String goodsId;

	private List<WapHallGoodsQueryResultVO> commonList;

	private List<String> imageDesc;


}
