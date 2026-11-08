package com.houjiang.commons.dto.goods;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListPageQueryParamDTO extends PageHelper {

	private Integer catId;

	private String catIdstring;

	private String commonName;

	/**
	 * 0 默认排序 1价格排序，2销量排序 3综合排序 4更新时间 5评论数 6积分排序
	 */
	private Integer orderType;

	/**
	 * 价格排序 1降序，2升序
	 */
	private Integer priceOrder;

	/**
	 * 评论排序 1降序，2升序
	 */
	private Integer commentOrder;

	/**
	 * 最低价
	 */
	private BigDecimal minPrice;

	/**
	 * 最高价
	 */
	private BigDecimal maxPrice;

	/**
	 * 店铺自营
	 */
	private String shopSelfSupport;

	/**
	 * 1降序，2升序
	 */
	private Integer saleNum;

	private List<Integer> catIds;

	private Integer brandId;

	private List<Integer> brandIdList;

	/**
	 * 品牌名称
	 */
	private String brandName;

	/**
	 * 商品分类属性
	 */
	private String attributeContentName;

	private Integer shopId;

	/**
	 * 是否是积分商品 0是 1否
	 */
	private Integer integralGoods;

	/**
	 * 热门兑换 1是 2否
	 */
	private Integer hotExchange;

	private String shopName;

	/**
	 * 1 一键上架  2立即购买
	 */
	private Integer sellDeliver;

	/**
	 * 1普通商品 2供应商商品
	 */
	private Integer goodsType;

	private Integer isApp;

	private Integer attributeContentId;

	private List<Integer> attributeContentIds;

	private List<String> attributeContentIdList;

	private List<Integer> brandIds;

	/**
	 * 门店id
	 */
	private Integer shopGateId;

	/**
	 * 属性名称
	 */
	private String attrName;
	/**
	 * 图片base64
	 */
	private String base64Str;
	/**
	 * 图片路径
	 */
	private String imagePath;

	/**
	 * 商品id集合
	 */
	private List<Integer> commonIdList;
	/**
	 * 文件后缀 eg: .docx
	 */
	private String streamName;
	/**
	 * AI 会话Id
	 */
	private Integer conversationId;
}
