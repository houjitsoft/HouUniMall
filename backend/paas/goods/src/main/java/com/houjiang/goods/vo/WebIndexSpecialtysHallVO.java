package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName WebIndexSpecialtysHallVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebIndexSpecialtysHallVO {

	private String exhibitionHallName;

	private String exhibitionHallImage;

	private String exhibitionHallLogo;

	private String exhibitionHallLogoWap;

	private String exhibitionHallDesc1;

	private String exhibitionHallDesc2;

	private String exhibitionHallDesc3;

	private String goodsId;

	private List<WapHallGoodsQueryResultVO> goodsList;

	private List<String> imageDesc;



}
