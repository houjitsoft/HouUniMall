package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.vo.goods.ModularizationdGoodsVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * @ClassName
 * @Description  组件化 页面
 * @Author Amos
 * @Date 2023-07-04 17:58:51
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_modularizationd")
@Data
public class BaseModularizationdEntity  implements Serializable {
    /**
     * 页面id
     */
    @TableId(type = IdType.AUTO)
    private Integer pageId;
    /**
     * 页面名称
     */
    private String pageName;
    /**
     * 页面内容
     */
    private String pageData;
    /**
     * 0 未发布 1 以发布
     */
    private Integer pageType;
    /**
     * 0 未使用 1已使用
     */
    private Integer pageStart;
    /**
     * 0 不是系统默认 1 是系统默认
     */
    private Integer pageDefault;
    /**
     * 系统默认页面 封面url
     */
    private String pageCoverUrl;
    /**
     * 是否是首页 1 是 2 二级页
     */
    private Integer pageIsIndex;
    /**
     * 本页使用的移动端模板 code（对应模板库，如 simple / vivid / premium）。
     * 为空表示不套用模板，前台沿用 pageData 里的原始版式。
     */
    private String pageTemplateCode;
    /**
     * 页面所属端：mobile=移动端(H5/小程序) pc=PC端。
     * 表默认值 mobile，历史数据全部为 mobile，uniapp 前台行为不受影响。
     */
    private String pagePlatform;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 当前页面的所有商品id
     */
    private String goodsallidjson;

    /**
     * 当前页面商品详情
     */
    @TableField(exist = false)
    private List<ModularizationdGoodsVO> goodsList;

    /**
     * 前台渲染用的模板描述（style / themeColor / columns / templateCode）。
     * 非表字段，由服务端按 pageTemplateCode 解析后下发。
     */
    @TableField(exist = false)
    private Map<String, Object> templateView;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
