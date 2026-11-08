package com.houjiang.commons.dto.base;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 用户消息集合
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberMessageListIntegralDTO extends UserTokenBeanDTO {

    /**
     * 用户id
     */
    private List<String> userIdList;

    /**
     * 类型 1 加积分 0 减积分
     */
    private Integer type;

    /**
     * 积分
     */
    private Integer integral;

    /**
     * 描述
     */
    private String desc;

    /**
     * 用户id
     */
    private String userId;

}
