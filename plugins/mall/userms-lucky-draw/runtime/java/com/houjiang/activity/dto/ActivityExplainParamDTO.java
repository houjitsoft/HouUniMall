package com.houjiang.activity.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ActivityExplainParamDTO extends PageUserTokenBeanDTO {

    /**
     * 活动ID
     */
    private Integer activityId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 红包所有者id
     */
    private Integer redpacketOwnerId;

    /**
     * 1个人 2所有用户获奖记录
     */
    private Integer isPersonRecord;

    /**
     * 总抽奖次数
     */
    private Integer luckyDrawTimes;

    /**
     * 剩余抽奖次数
     */
    private Integer surplusDrawTimes;

    /**
     * 每日抽奖次数
     */
    private Integer numberOfDailyLottery;

    /**
     * 奖项id
     */
    private Integer prizeId;

    /**
     * 奖项id
     */
    private Integer prizeId2;

    /**
     * 用户名称
     */
    private String userName;


}
