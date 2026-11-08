package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName DeliveryUpdateVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 9:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class DeliveryUpdateVO {

    /**
     * 上下文信息，用于存储操作的详细内容或描述
     */
    private String context;

    /**
     * 时间戳，记录操作发生的时间
     */
    private String time;

    /**
     * 标题，概括操作的主题或名称
     */
    private String title;

    /**
     * 操作者，标识执行操作的用户或实体
     */
    private String operator;
}
