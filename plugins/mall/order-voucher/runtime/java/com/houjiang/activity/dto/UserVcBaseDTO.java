package com.houjiang.activity.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 代金券
 * @author Amos
 */
@Data
public class UserVcBaseDTO extends PageHelper {

    /**
     * 代金券状态(1-未用,2-已用,3-过期,4-收回)
     */
    private Integer voucherState;

    /**
     * 领取人id
     */
    private String voucherOwnerId;

    /**
     * 代金券模版编号
     */
    private Integer voucherTId;


}
