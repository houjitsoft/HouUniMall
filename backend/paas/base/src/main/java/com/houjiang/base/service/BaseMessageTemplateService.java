package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseMessageTemplateDTO;
import com.houjiang.base.entity.BaseMessageTemplateEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseMessageTemplateService
 * @Description  消息模板表
 * @Author Amos
 * @Date 2024-09-11 14:19:52
 * @Version 1.0
 * @author Amos
 */
public interface BaseMessageTemplateService extends IService<BaseMessageTemplateEntity> {


    /**
     * 查询消息模板列表
     *
     * @param param 查询参数
     * @return 消息模板列表
     */
    ResponseResult<List<BaseMessageTemplateEntity>> selectMessageTemplateList(BaseMessageTemplateDTO param);

    /**
     * 更新消息模板
     *
     * @param param 经过验证的更新参数，包含要更新的消息模板信息
     * @return 更新操作的结果，包含状态和可能的消息内容
     * <p>
     * 此方法接收一个经过验证的消息模板更新参数（BaseMessageTemplateDTO），通过调用
     * baseMessageTemplateService.updateMessageTemplate方法，实现对指定消息模板的更新。
     * 使用@Validated注解确保传入的参数符合更新操作的业务规则。
     */
    ResponseResult updateMessageTemplate(BaseMessageTemplateDTO param);

    /**
     * 使用POST方法发送消息模板
     *
     * @param param 包含消息模板参数的Map对象，通过请求体发送
     * @return 返回服务器的响应结果
     * <p>
     * 通过此端点，客户端可以提交一个包含消息模板所需参数的Map对象
     * 具体的参数和逻辑处理由baseMessageTemplateService的sendMessageTemplate方法实现
     */
    ResponseResult sendMessageTemplate(Map<String, Object> param);
    /**
     * 根据模板类型查询列表
     *
     * 该函数用于根据传入的模板类型，查询对应的模板列表。
     *
     * @param templateId 模板类型，用于指定要查询的模板类型。传入的值为整数类型。
     * @return ResponseResult 返回查询结果，包含查询到的模板列表信息。返回类型为ResponseResult。
     */
    ResponseResult selectListByTemplateId(Integer templateId);
}
