package com.houjiang.commons.dto.yiyatong;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * @ClassName MsgCallBackDTO
 * @Description
 * @Author Amos
 * @Date 2024/12/10 10:48
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MsgCallBackDTO extends UserTokenBeanDTO {

    /**
     * 消息ID，用于唯一标识一条消息
     */
    private String messageId;

    /**
     * 应用密钥，用于验证消息的来源和合法性
     */
    private String appKey;

    /**
     * 枚举编号，用于标识消息的类型或来源
     */
    private String enumNum;

    /**
     * 消息内容，存储实际的消息信息
     */
    private String content;

    /**
     * 创建时间，记录消息的生成时间，用于后续的消息排序或过滤
     */
    private String createTime;

    /**
     * 签名，用于验证消息的完整性和来源
     */
    private String sign;


}
