package com.houjiang.goods.vo;

import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.base.ManageGdsBaseInfoVO;
import com.houjiang.commons.vo.goods.FXGoodsListVO;
import com.houjiang.commons.vo.goods.GdsBaseDetailResultVO;
import com.houjiang.commons.vo.goods.ManageGoodsFormatResultVO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import lombok.Data;

import java.util.List;
/**
 * @ClassName GdsDetailPageResultVO
 * @Description GdsDetailPageResultVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdsDetailPageResultVO {
	/**
	 * 商品详情
	 */
	private GdsBaseDetailResultVO goodsBases;
	/**
	 * 商品图片
	 */
	private List<GdsImageDetailResultVO> goodsImages;
	/**
	 * 商品图片
	 */
	private List<ManageGdsBaseInfoVO> ManageGdsBaseInfoList;

	/**
	 * 店铺信息
	 */
	private GdsShopDetailResultVO shopDetail;

	/**
	 * 商品信息
	 */
	private GdsCommonResultVO gdsCommon;
	/**
	 * 底部关联板式
	 */
	private ManageGoodsFormatResultVO commonFormatidBottom;

	/**
	 * 顶部关联板式
	 */
	private ManageGoodsFormatResultVO commonFormatidTop;

	/**
	 * 销售信息
	 */
	private BaseGoodsConfigVO manageGdsGoodsConfig;

	/**
	 * 分销商品
	 */
	private FXGoodsListVO fxGoodsList;
	/**
	 * 是否为该商品商铺的门店账号  0:不是 1:是
	 */
	private Integer shopGate;
	/**
	 * plus会员状态  0:关闭 1:开启
	 */
	private Integer plusStatus;


}
