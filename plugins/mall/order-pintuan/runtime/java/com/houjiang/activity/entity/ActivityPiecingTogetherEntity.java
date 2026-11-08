package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.activity.vo.PintuanGoodsSpecListVO;
import com.houjiang.commons.vo.goods.GdsDetailsCommonQueryResultVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description  拼团主表
 * @Author Amos
 * @Date 2023-05-25 14:27:05
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_piecing_together")
@Data
public class ActivityPiecingTogetherEntity  implements Serializable {
    /**
     * 主键，自增长
     */
    @TableId(type = IdType.AUTO)
    private Integer pintuanId;
    /**
     * 店铺ID
     */
    private Integer shopId;
    /**
     * 商家ID
     */
    private String userId;
    /**
     * 拼团活动名称
     */
    private String name;
    /**
     * 几人成团
     */
    private Integer personNum;
    /**
     * 开团时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 是否可用，1正常 0结束 2 待开启
     */
    private Integer status;
    /**
     * 商品ID
     */
    private Integer commonId;
    /**
     * 持续时间
     */
    private Integer startHour;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 排序
     */
    private Integer displayOrder;

    /**
     * spu商品
     */
    @TableField(exist = false)
    private GdsDetailsCommonQueryResultVO commonBase;

    /**
     * 规格列表
     */
    @TableField(exist = false)
    private List<PintuanGoodsSpecListVO> goodsSpecList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

