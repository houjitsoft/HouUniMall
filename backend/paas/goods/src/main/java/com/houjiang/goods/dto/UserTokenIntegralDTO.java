package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName UserTokenIntegralDTO
 * @Author Amos
 * @Date 2023/6/25 15:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserTokenIntegralDTO extends UserTokenIntegral2DTO{

    /**
     *
     */
    private Integer currentPage;
    /**
     *
     */
    private Integer pageSize;
    /**
     *
     */
    private String userId;
}
