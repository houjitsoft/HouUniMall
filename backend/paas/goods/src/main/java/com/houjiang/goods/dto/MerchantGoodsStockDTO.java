package com.houjiang.goods.dto;

import com.houjiang.commons.group.Select;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 商品库存表
 * @TableName goods_stock
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MerchantGoodsStockDTO extends PageHelper {
    /**
     * 
     */
    private Integer id;

    /**
     * goodsId
     */
    @NotNull(message = "商品不能为空", groups = {Update.class, Select.class})
    private Integer goodsId;

    /**
     * commonId
     */
    private Integer commonId;

    /**
     * 商品库存
     */
    @NotNull(message = "库存不能为空", groups = {Update.class})
    private Integer goodsStock;

    /**
     * 库存预警值
     */
    private Integer goodsAlarm;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 仓库名称
     */
    private String storageName;

    /**
     * 商品id集合
     */
    private List<Integer> goodsIdList;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 是否预警 1是 0否
     */
    private Integer isEarlyWarning;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 仓库Id
     */
    private Integer storageId;

    /**
     * 商品名称
     */
    private String goodsName;

}