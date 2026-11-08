package com.houjiang.commons.vo.third;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description:
 * @CreateDate: 2025/7/9 00:00
 * @author Amos
 */
@Data
public class UserAndShopInfoReqVo implements Serializable {

    @NotNull(message = "shopId不能为空")
    private Integer shopId;
    @NotNull(message = "userId不能为空")
    private Integer userId;
}
