package com.houjiang.goods.vo;

import com.houjiang.commons.vo.goods.SpecParamJsonBeanVO;
import com.houjiang.commons.vo.goods.WapCommonSpecValueVO;
import com.houjiang.goods.entity.GoodsCategoryAttributesEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
/**
 * @ClassName GdsCommonResultVO
 * @Description GdsCommonResultVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdsCommonResultVO {
	/**
	 * 规格名称
	 */
	private String commonSpecName;
	/**
	 * 商品id
	 */
	private Integer commonId;

	/**
	 * 商品图片
	 */
	private String commonImage;

	/**
	 * 商品规格
	 */
	private String CommonSpecValue;
	/**
	 * 规格名称
	 */
	private Integer commonFormatidTop;
	/**
	 * 底部关联板式
	 */
	private Integer commonFormatidBottom;
	/**
	 * 规格值集合
	 */
	private List<WapCommonSpecValueVO> commonSpecValueList;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;

	/**
	 * 分类名
	 */
	private String catName;

	/**
	 * 分类id
	 */
	private Integer catId;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer commonIsXian;
	/**
	 * 每人限购 0代表不限购
	 */
	private Integer commonLimit;
	/**
	 * 1:商城价2:小宝价格3:商城和小宝价组合
	 */
	private Integer commonType;
	/**
	 * 小宝价格
	 */
	private BigDecimal commonBabyPrice;
	/**
	 * 角标图片
	 */
	private String cornerUrl;
	/**
	 * 1图片2视频
	 */
	private Integer type;
	/**
	 * 视频
	 */
	private String commonVideo;
	/**
	 * 商品规格
	 */
	private String specParam;
	/**
	 * 虚拟商品 0 是 1 否
	 */
	private Integer virtualGoods;
	/**
	 * 虚拟商品有效期
	 */
	private String periodOfValidity;
	/**
	 * 过期退款 0 是 1 否
	 */
	private Integer pastDue;
	/**
	 * 规格参数商品
	 */
	private List<SpecParamJsonBeanVO> specParamList;
	/**
	 * 是否是积分商品
	 */
	private Integer integralGoods;
	/**
	 * 1普通商品   2供应商商品
	 */
	private Integer goodsType;
	/**
	 * 0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
	 */
	private Integer dealerGoodsFlag;
	/**
	 * 发货时效
	 */
	private Integer deliveryTime;

	/**
	 * 售后服务JSON
	 */
	private String afterService;

	/**
	 * 售后服务数组
	 */
	private List<String> afterServiceList;

	/**
	 * 商品类目属性集合
	 */
	private List<GoodsCategoryAttributesEntity> categoryAttributesList;

}
