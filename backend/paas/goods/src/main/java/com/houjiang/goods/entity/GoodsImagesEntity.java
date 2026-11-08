package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  商品图片
 * @Author Amos
 * @Date 2023-05-25 15:07:41
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_images")
@Data
public class GoodsImagesEntity  implements Serializable {
    /**
     * 商品图片id
     */
    @TableId(type = IdType.AUTO)
    private Integer imagesId;
    /**
     * 商品公共内容id
     */
    private Integer commonId;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 商品图片
     */
    private String imagesImage;
    /**
     * 排序
     */
    private Integer imagesDisplayorder;
    /**
     * 默认主题，1是，0否
     */
    private Integer imagesIsDefault;
    /**
     * 主图类型： 1图片2视频
     */
    private Integer type;
    /**
     * 时间戳
     */
    private Date ts;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

