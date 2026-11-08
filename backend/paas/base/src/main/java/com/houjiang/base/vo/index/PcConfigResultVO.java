package com.houjiang.base.vo.index;

import com.houjiang.commons.vo.shop.PcConfigShopVO;
import lombok.Data;

import java.util.List;
/**
 * @ClassName PcConfigResultVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class PcConfigResultVO {
    /**
     * 模块id
     */
    private Integer pageId;
    /**
     * 模块名称
     */
    private String pageName;
    /**
     * 1 商品 2 店铺 3 分类
     */
    private Integer pageType;
    /**
     * 标题
     */
    private String pageTitle;
    /**
     * 二级标题
     */
    private String pageTwoTitle;
    /**
     * 1   开启 2 关闭
     */
    private Integer pageStart;
    /**
     * 排序
     */
    private Integer pageSort;
    /**
     * 商品
     */
    private List<PcConfigGoodsVO> goods;
    /**
     * 店铺
     */
    private List<PcConfigShopVO>  shops;
    /**
     * 分类
     */
    private PcConfigCatVO cat;
}
