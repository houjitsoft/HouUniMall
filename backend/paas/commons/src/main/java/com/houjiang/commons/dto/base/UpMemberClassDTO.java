package com.houjiang.commons.dto.base;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UpMemberClassDTO {
    /**
     * 用户id
     */
    private String memberId;
    /**
     * 会员等级id
     */
    private Integer memberClassId;
    private String [] memberArray;


}
