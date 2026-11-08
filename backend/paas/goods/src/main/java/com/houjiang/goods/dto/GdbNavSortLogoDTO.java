package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName GdbNavSortLogoDTO
 * @Author Amos
 * @Date 2023/6/26 17:28
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbNavSortLogoDTO {

    /**
     * 配置键
     */
    private String configKey;
    /**
     //商品id2:店铺id3:链接4:分类10:红包11:美食中国12:为你推荐13:特价推荐14:新品推荐'15:溯源资讯16:店铺活动17:精选商品18:商城公告',19新人专区;20平台活动专区;21:9.9包邮专区;22:品牌生活;23:好货溯源
/**
     * 配置值:imageType;
     */
    private String configValue;
    /**
     * 配置类型
     */
    private String configType;
}
