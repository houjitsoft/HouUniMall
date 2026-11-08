package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.List;

/**
 * 所有商品
 * @author Amos
 */
@Data
public class PlatFormAllGoodsDTO extends PageHelper {

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品是否被锁定，默认为1,不锁定；2锁定
     */
    private Integer commonIsLocked;

    /**
     * 商品是否屏蔽：1-否；2-是
     */
    private Integer commonIsScreen;

    /**
     * 是否为虚拟商品0是1否
     */
    private Integer virtualGoods;

    /**
     * 1查询普通店铺商品 2查询供应商商品  3查询全部
     */
    private Integer goodsType;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 品牌名称
     */
    private String brandName;


    /**
     * 分类id，最后一级
     */
    private Integer catId;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 库存值
     */
    private Integer inventoryWarning;

    /**
     * 1特产推荐;2新品推荐;3为你推荐;4蜂狂特价;5新人专区;6平台活动专区;
     * 7:9.9包邮专区;8:品牌生活;9:好货溯源;10:猜你喜欢;11:时令果蔬;
     * 12:节日钜惠;13:限时折扣;14:超级拼团;
     */
    private Integer opType;
    /**
     * 爆品名称
     */
    private String hotGoodsName;
    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 1 平台商品管理页面列表  0 非此页面
     */
    private Integer productIndex;

    /**
     * 1:普通商家店铺 2:自营店铺 3:供应商  4  品牌店铺
     */
    private Integer shopBusiness;
    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通, 3 京东VOP
     */
    private Integer supplierSource;
    /**
     * 供应商商品id
     */
    private String supplyCommonId;

    /**
     * 分类id集合
     */
    private List<Integer> catIdList;
}
