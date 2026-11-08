package com.houjiang.merchant.vo.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.houjiang.merchant.vo.mall.MallShopAdminVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class LoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录令牌")
    @JsonProperty("shop-token")
    private String shopToken;

    @Schema(description = "shopAdminVo")
    @JsonProperty("shop")
    private MallShopAdminVo shopAdminVo;
}
