package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class PlatFormAllGoodsQueryDTO extends PageHelper {

	/**
	 * 商品名称
	 */
	private String commonName; 
	/**
	 *店铺名称
	 */
	private String shopName; 
	/**
	 *店铺名称
	 */
	private Integer shopId; 
	/**
	 * 品牌名称
	 */
	private String brandName;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;
	/**
	 *商品审核 1通过，0未通过，10审核中
	 */
	private Integer commonVerify;
	/**
	 * 分类id，最后一级
	 */
	private Integer catId;
	/**
	 * 分类id
	 */
	private List<Integer> catIds;
	/**
	 * 1:商城价2:小宝价格3:商城和小宝价组合
	 */
	private Integer commonType;
	/**
	 *库存值
	 */
	private Integer inventoryWarning;
	/**
	 *商品是否被锁定，默认为1,不锁定；2锁定,
	 */
	private Integer commonIsLocked;
	/**
	 *商品是否屏蔽：1-否；2-是
	 */
	private Integer commonIsScreen;
	/**
	 * 1特产推荐;2新品推荐;3为你推荐;4蜂狂特价;5新人专区;6平台活动专区;
	 * 	 7:9.9包邮专区;8:品牌生活;9:好货溯源;10:猜你喜欢;11:时令果蔬;
	 * 	 12:节日钜惠;13:限时折扣;14:超级拼团;
	 */
	private Integer opType;
	/**
	 *是否为虚拟商品0是1否
	 */
	private Integer virtualGoods;
	/**
	 * 1普通商品   2供应商商品',
	 */
	private Integer goodsType;
	/**
	 * 1 平台商品管理页面列表  0 非此页面
	 */
	private Integer product_index; 

	/**
	 * 爆品名称
	 */
	private String hotGoodsName;
	/**
	 * 店铺类型
	 */
	private Integer shopType;

	/**
	 * 用户id
	 */
	private String userId;

}
