package com.houjiang.commons.vo.goods;


import com.houjiang.commons.dto.goods.GoodsCustomInformationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResultVO {

    /**
     * 规格名称
     */
    private String commonSpecName;
    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品id
     */
    private String commonIdStr;

    /**
     * 商品图片
     */
    private String commonImage;

    /**
     * 规格值
     */
    private String commonSpecValue;

    /**
     * 顶部关联板式
     */
    private Integer commonFormatidTop;

    /**
     * 低部关联板式
     */
    private Integer commonFormatidBottom;

    /**
     * 商品规格数据集合
     */
    private List<WapCommonSpecValueVO> commonSpecValueList;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 分类名称
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
     * 规格参数
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
    private List<SpecParamJsonBeanDTO> specParamList;

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
     * 单位
     */
    private String dw;

    /**
     * 现货 ? 订货
     */
    private Integer deliveryDate;

    /**
     * 订货预计发货时间
     */
    private Integer goodsOrderDate;

    /**
     * 自定义信息JSON
     */
    private String customInformation;

    /**
     * 自定义信息数组
     */
    private List<GoodsCustomInformationDTO> customInformationList;

    /**
     * 售后服务JSON
     */
    private String afterService;

    /**
     * 售后服务数组
     */
    private List<String> afterServiceList;

    /**
     * 技术负责人微信
     */
    private String technicalDirectorWx;

    /**
     * 技术负责人电话
     */
    private String technicalDirectorTel;

    /**
     * 生产制造商
     */
    private String manufacturer;

    /**
     * 商品类目属性集合
     */
    private List<GoodsCategoryAttributesVO> categoryAttributesList;

    private Integer supplierSource;

    private String commonBody;

}
