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
 * @Description  收藏的店铺
 * @Author Amos
 * @Date 2023-05-26 16:35:37
 * @Version 1.0
 * @author Amos
 */
@TableName(value ="hou_user_favorites_shop")
@Data
public class UserFavoritesShopEntity  implements Serializable {

     @TableId(type = IdType.AUTO)
    private Integer favoritesShopId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺logo
     */
    private String shopLogo;

    private Date favoritesShopTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
