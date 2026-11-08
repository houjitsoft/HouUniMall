package com.houjiang.commons.vo.goods;

import com.houjiang.commons.vo.base.ManageGdsBaseInfoVO;
import com.houjiang.commons.vo.shop.ShopDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailPageVO {

    /**
     * 商品详情
     */
    private BaseDetailVO goodsBases;

    /**
     * 商品图片
     */
    private List<ImageDetailVO> goodsImages;

    /**
     * 商品图片
     */
    private List<ManageGdsBaseInfoVO> ManageGdsBaseInfoList;

    /**
     * 店铺详细信息
     */
    private ShopDetailVO shopDetail;

    /**
     * 商品公共信息
     */
    private CommonResultVO gdsCommon;

    /**
     * 低部关联板式
     */
    private ManageGoodsFormatVO commonFormatidBottom;

    /**
     * 顶部关联板式
     */
    private ManageGoodsFormatVO commonFormatidTop;

    /**
     * 经销商品配置
     */
    private ManageGdsGoodsConfigVO manageGdsGoodsConfig;

    /**
     * 分销商品列表
     */
    private DistributionGoodsListVO fxGoodsList;

    /**
     * 同commonId的不同规格商品
     */
    private List<BaseDetailVO> gdsBaseDetailList;

    /**
     * 状态
     */
    private Integer zhuangTai;

    /**
     * 二维码地址
     */
    private String qrUrl;

}
