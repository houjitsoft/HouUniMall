package com.houjiang.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 楼层
 * @author Amos
 */
@Data
public class FloorDTO {

	/**
	 * 楼层id
	 */
	private Integer floorAdpageId; 

	/**
	 * 楼层名称
	 */
	private String floorAdpageName; 

	/**
	 * 楼层名称
	 */
	private FloorAdpageNameDTO floorName;

	/**
	 * 左侧轮播,商品的goodsId，用，分割
	 */
	private String leftAdpageBanner; 

	/**
	 * 左侧楼层导航
	 */
	private String leftAdpageNav; 

	/**
	 * 左侧分类集合
	 */
	private List<PlatformLeftAdpageNavDTO> leftAdpageNavList;

	/**
	 * 右侧推荐
	 */
	private String rightHotRecommend; 

	/**
	 * 右侧导航
	 */
	private PlatformRightAdpageNavDTO rightRec;

	/**
	 * 右侧楼层导航
	 */
	private String rightAdpageNav; 

	/**
	 * 右侧分类集合
	 */
	private List<FloorAdpageNameDTO> rightAdpageNavList;

	/**
	 * 楼层默认排序
	 */
	private Integer floorAdpageOrder; 

	/**
	 * 楼层状态1启用2不启用
	 */
	private Integer floorStatus; 

	/**
	 * 楼层更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date floorUpdateTime;



}
