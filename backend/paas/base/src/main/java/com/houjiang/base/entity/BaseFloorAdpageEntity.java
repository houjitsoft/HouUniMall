package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  pc首页楼层广告
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_floor_adpage")
@Data
public class BaseFloorAdpageEntity  implements Serializable {
    /**
     * 楼层id
     */
    @TableId(type = IdType.AUTO)
    private Integer floorAdpageId;
    /**
     * 楼层名称
     */
    private String floorAdpageName;
    /**
     * 左侧轮播
     */
    private String leftAdpageBanner;
    /**
     * 特色推荐
     */
    private String rightHotRecommend;
    /**
     * 左侧楼层导航
     */
    private String leftAdpageNav;
    /**
     * 右侧楼层导航
     */
    private String rightAdpageNav;
    /**
     * 楼层默认排序
     */
    private Integer floorAdpageOrder;
    /**
     * 楼层状态1启用2不启用
     */
    private Integer floorStatus;
    /**
     * 楼层更新时间
     */
    private Date floorUpdateTime;
    /**
     * 1 普通店铺配置  2供应商配置
     */
    private Integer floorType;
    /**
     * 页面类型  5单楼层 6多楼层 1 天天特价 2 特色专题  3 为你推荐 4家居馆  (1、2、3、4 状态在gdb_pc_index_config表）
     */
    private Integer pageType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

