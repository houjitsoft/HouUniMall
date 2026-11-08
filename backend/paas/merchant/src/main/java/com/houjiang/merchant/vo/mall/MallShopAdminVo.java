package com.houjiang.merchant.vo.mall;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class MallShopAdminVo implements Serializable {
    private Integer id;
    private String name;
    private Long userId;
    private String loginAccount;
    private String face;
    @JsonProperty("create_time") private String createTime;
    @JsonProperty("shop_id") private Integer shopId;
    @JsonProperty("super") private Integer isSuper;
}
