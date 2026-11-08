package com.houjiang.base.vo.index;

import com.houjiang.commons.vo.goods.GoodsCatVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName WebIndexQueryVO
 * @Description web首页
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebIndexQueryVO {

	/**
	 * 查询首页导航
	 */
	private List<WebSystemPlatformNavVO> platformNav;
	/**
	 * 轮播
	 */
	private List<WebSystemConfigQueryVO> indexSlides;
	/**
	 * 查询一级分类
	 */
	private List<GoodsCatVO> goodsCats;
	/**
	 * 热搜词
	 */
	private List<String> hotWords;
	/**
	 * 热搜商品
	 */
	private List<WapHallGoodsQueryVO> hotGoods;
	/**
	 * 疯狂快报
	 */
	private WebIndexArticleGroupVO articleGroup;
	/**
	 * 轮播下广告位
	 */
	private List<WebSystemConfigQueryVO> indexSlidesAds;

}
