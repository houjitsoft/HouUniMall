package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseMessageTemplateDTO;
import com.houjiang.base.entity.BaseMessageTemplateEntity;
import com.houjiang.base.service.BaseMessageTemplateService;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseMessageTemplateController
 * @Description  消息模板表
 * @Author Amos
 * @Date 2024-09-11 14:19:51
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseMessageTemplate")
public class BaseMessageTemplateController {
    /**
     * 服务对象
     */
    @Resource
    private BaseMessageTemplateService baseMessageTemplateService;

    /**
     * 查询消息模板列表
     *
     * @param param 查询参数
     * @return 消息模板列表
     */
    @PostMapping("/selectList")
    public ResponseResult<List<BaseMessageTemplateEntity>> selectMessageTemplateList(
            @RequestBody @Validated({Select.class}) BaseMessageTemplateDTO param) {
        // 将查询参数传递给服务层，以获取消息模板列表
        return baseMessageTemplateService.selectMessageTemplateList(param);
    }

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
    @PostMapping("/update")
    public ResponseResult updateMessageTemplate(@RequestBody @Validated({Update.class}) BaseMessageTemplateDTO param) {
        return baseMessageTemplateService.updateMessageTemplate(param);
    }

    /**
     * 使用POST方法发送消息模板
     *
     * @param param 包含消息模板参数的Map对象，通过请求体发送
     * @return 返回服务器的响应结果
     * <p>
     * 通过此端点，客户端可以提交一个包含消息模板所需参数的Map对象
     * 具体的参数和逻辑处理由baseMessageTemplateService的sendMessageTemplate方法实现
     */
    @PostMapping("/sendMessageTemplate")
    public ResponseResult sendMessageTemplate(@RequestBody Map<String, Object> param) {
        return baseMessageTemplateService.sendMessageTemplate(param);
    }
    /**
     * 查询消息模板列表
     *
     * @param templateId 查询参数
     * @return 消息模板列表
     */
    @PostMapping("/selectListByTemplateId")
    public ResponseResult selectListByTemplateId(@RequestParam Integer templateId) {
        // 将查询参数传递给服务层，以获取消息模板列表
        return baseMessageTemplateService.selectListByTemplateId(templateId);
    }
}
