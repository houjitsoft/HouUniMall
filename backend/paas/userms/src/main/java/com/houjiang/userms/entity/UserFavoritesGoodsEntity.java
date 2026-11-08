package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  收藏的商品
 * @Author Amos
 * @Date 2023-05-26 16:24:17
 * @Version 1.0
 * @author Amos
 */
@TableName(value ="hou_user_favorites_goods")
@Data
public class UserFavoritesGoodsEntity  implements Serializable {
    /**
     * 商品收藏编号
     */
    @TableId(type = IdType.AUTO)
    private Integer favoritesGoodsId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 货品ID
     */
    private Integer goodsId;
    /**
     * 收藏时间
     */
    private Date favoritesGoodsTime;
    /**
     * 时间戳
     */
    private Date ts;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
