package com.houjiang.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.vo.goods.ModularizationdGoodsVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class ModularizationdDTO  extends PageHelper {
    /** 页面id */
    private Integer pageId;
    /** 页面名称 */
    private String pageName;
    @JsonProperty("pagedata")
    /** 页面内容 */
    private Object pageData;
    /** 0 未发布 1 以发布 */
    private Integer pageType;
    /** 0 未使用 1已使用 */
    private Integer pageStart;
    /** 0 不是系统默认 1 是系统默认 */
    private Integer pageDefault;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 首页/二级页  1 首页 2 二级页 */
    private Integer pageIsIndex;
    /** 封面图片 */
    private String pageCoverUrl;
    private Integer currentPage;
    private Integer pageSize;
    private String token;
    /** 二级页面地址 */
    private String pageUrl;
    private List<String> goodsAllId;
    private String goodsAllIdJson;
    /** 当前页面商品详情 */
    private List<ModularizationdGoodsVO> goodsList;

    private Integer shopId;
    private Integer shopType;

    /**
     * 店铺粉丝数
     */
    private Integer shopCollect;

    /**
     * 会员规则协议
     */
    private String shopLogo;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 该页面使用的移动端模板 code（对应 hou_base_modularizationd.page_template_code）。
     * 为空表示沿用页面自身 pageData 里的原始版式。
     */
    private String templateCode;

    /**
     * 页面所属端：mobile=移动端(H5/小程序) pc=PC端。
     * 对应 hou_base_modularizationd.page_platform；为空表示不限端，返回全部。
     * 历史数据均为 mobile，故未传该参数时行为与改动前一致。
     */
    private String pagePlatform;

}
