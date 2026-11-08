package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseMessageTemplateDTO;
import com.houjiang.base.feign.ThirdPartyServiceClient;
import com.houjiang.base.feign.UsermsClient;
import com.houjiang.base.mapper.BaseMessageTemplateMapper;
import com.houjiang.base.entity.BaseMessageTemplateEntity;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.houjiang.base.mapper.BaseSystemMessageMapper;
import com.houjiang.base.service.BaseMessageTemplateService;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.base.BaseMessageTemplateVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseMessageTemplateServiceImpl
 * @Description  消息模板表
 * @Author Amos
 * @Date 2024-09-11 14:19:52
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseMessageTemplateServiceImpl extends ServiceImpl<BaseMessageTemplateMapper, BaseMessageTemplateEntity>
        implements BaseMessageTemplateService {
    @Autowired
    private UsermsClient usermsClient;

    @Autowired
    private BaseSystemMessageMapper baseSystemMessageMapper;

    @Autowired
    private BaseSysConfigsMapper baseSysConfigsMapper;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    /**
     * 查询消息模板列表
     *
     * @param param 查询参数，包含模板类型等条件
     * @return 消息模板列表，以ResponseResult对象封装
     */
    @Override
    public ResponseResult<List<BaseMessageTemplateEntity>> selectMessageTemplateList(BaseMessageTemplateDTO param) {
        // 根据传入的查询参数，构造查询条件
        List<BaseMessageTemplateEntity> list = baseMapper.selectList(new LambdaQueryWrapper<BaseMessageTemplateEntity>()
                .eq(BaseMessageTemplateEntity::getTemplateType, param.getTemplateType()));
        // 返回查询结果
        return ResponseResult.success(list);
    }

    /**
     * 更新消息模板
     * <p>
     * 该方法用于更新数据库中的消息模板信息它接收一个消息模板数据传输对象(BaseMessageTemplateDTO),
     * 将其转换为实体对象(BaseMessageTemplateEntity),并调用底层数据访问层的方法(updateById)来更新数据库中的对应记录如果更新操作成功,
     * 即影响了数据库中的记录,则返回成功的结果对象(ResponseResult.success()),否则返回失败的结果对象(ResponseResult.fail())。
     * </p>
     *
     * @param param BaseMessageTemplateDTO类型的参数,包含了要更新的消息模板的信息
     * @return ResponseResult表示操作结果, 成功或失败
     */
    @Override
    public ResponseResult updateMessageTemplate(BaseMessageTemplateDTO param) {
        BaseMessageTemplateEntity entity = new BaseMessageTemplateEntity();
        BeanUtils.copyProperties(param, entity);
        int count = baseMapper.updateById(entity);
        return count > 0 ? ResponseResult.success() : ResponseResult.fail();
    }

    /**
     * 根据模板ID发送消息
     * 该方法用于根据参数中提供的模板ID，获取相应的消息模板内容，并根据模板的配置发送不同类型的站内信、短信或微信公众号通知
     *
     * @param param 包含模板ID及其他发送消息所需参数的映射
     * @return 响应结果，发送成功或失败的标识
     */
    @Override
    public ResponseResult sendMessageTemplate(Map<String, Object> param) {
        // 从参数中获取模板ID
        Integer templateId = (Integer) param.get("templateId");
        // 根据模板ID查询消息模板实体
        BaseMessageTemplateEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<BaseMessageTemplateEntity>()
                .eq(BaseMessageTemplateEntity::getId, templateId));
        if (entity != null) {
            // 将模板代码存入参数
            param.put("templateCode", entity.getTemplateCode());
            // 发送站内信通知
            if (entity.getMailStatus() == 1) {
                // 根据邮件模板内容设置实际发送的内容
                String content = setContent(entity.getMailTemplateContent(), (List<String>) param.get("list"));
                param.put("messageContent", content);
                param.put("messageTitle", entity.getTemplateName());
                // 根据模板类型设置消息类型
                param.put("messageMold", entity.getTemplateType() == 1 ? 0 : 1);
                // 根据模板类型或ID判断，插入不同的站内信记录
                if (entity.getTemplateType() == 1 || entity.getId() == 6 || entity.getId() == 6) {
                    param.put("creater", "admin");
                    // 用户消息
                    usermsClient.sendPlatformMessageFeign(param);
                } else {
                    // 店铺消息
                    baseSystemMessageMapper.insertSysMessage(param);
                }
            }
            // 发送短信通知 或 邮件
            if (entity.getSmsStatus() == 1 || entity.getEmailStatus() == 1) {
                // 根据模板类型设置接收短信的手机号
                if (entity.getTemplateType() == 3) {
                    String mobile = baseSysConfigsMapper.selectConfigValue(CommonType.configKey.PLATFORM_SMS_MOBILE.getDesc());
                    if (StringUtils.isEmpty(mobile)) {
                        return ResponseResult.fail(BizCodeEnume.PLATFORM_SMS_MOBILE_IS_NULL);
                    }
                    param.put("mobile", mobile);
                } else {
                    // 根据sellerId或userId获取手机号，确保发送目标正确
                    if (param.get("mobile") == null && param.get("sellerId") != null) {
                        Map<String, String> result = getMobile(param.get("sellerId").toString());
                        param.put("mobile", result.get("userMobile"));
                        param.put("interDialingCode", result.get("interDialingCode"));
                    }
                    if (param.get("mobile") == null && param.get("userId") != null) {
                        Map<String, String> result = getMobile(param.get("userId").toString());
                        param.put("mobile", result.get("userMobile"));
                        param.put("interDialingCode", result.get("interDialingCode"));
                    }
                }
                // 将短信模板内容和ID存入参数
                param.put("smsTemplateContent", entity.getSmsTemplateContent());
                param.put("smsTemplateId", entity.getSmsTemplateId());
                param.put("smsStatus", entity.getSmsStatus());
                param.put("emailStatus", entity.getEmailStatus());
                param.put("messageTemplateId", entity.getId());
                // 调用第三方服务发送短信
                thirdPartyServiceClient.sendSms(param);
            }
            // 发送微信公众号通知
            if (entity.getWxStatus() == 1) {
                // 将微信模板内容和ID存入参数
                param.put("wxTemplateContent", entity.getWxTemplateContent());
                param.put("wxTemplateId", entity.getWxTemplateId());
                // 调用第三方服务发送微信公众号通知
                thirdPartyServiceClient.sendNotice(param);
            }
        }
        // 返回发送成功的结果
        return ResponseResult.success();
    }

    /**
     * 根据用户ID获取用户手机号
     *
     * 本函数通过调用用户管理服务（usermsClient）中的queryUserMobileByuserId方法，
     * 根据提供的用户ID查询并返回对应的用户手机号此函数的主要目的是
     * 为了封装对用户手机号的查询逻辑，提供一个简单且易于使用的接口
     *
     * @param userId 用户ID，用于标识特定的用户
     * @return 用户的手机号如果用户不存在或用户未设置手机号，将返回null
     */
    public Map<String,String> getMobile(String userId) {
        Map<String,String> result = usermsClient.queryUserMobileByuserId(userId);
        return result;
    }
    /**
     * 设置内容中的占位符为指定列表中的值
     * 此方法的目的是将内容字符串中包含的占位符（例如 "{1}"、"{2}" 等）替换为列表中的相应值
     * 这在需要动态插入值到预定义格式字符串的应用场景中非常有用，例如格式化消息、报告或数据绑定等
     *
     * @param content 待处理的字符串内容，可能包含占位符（如 "{1}"）
     * @param list    一个字符串列表，提供替换占位符的实际值
     * @return 替换占位符后的字符串如果列表中的值不足以替换所有占位符，返回的字符串将保持未替换的占位符不变
     */
    public String setContent(String content, List<String> list) {
        System.out.println("========"+list);
        // 遍历列表中的每个元素，用列表中的值替换占位符
        for (int i = 1; i <= list.size(); i++) {
            // 检查内容是否包含当前索引的占位符
            if (content.contains("{" + i + "}")) {
                // 将占位符替换为列表中对应索引的值
                content = content.replace("{" + i + "}", list.get(i - 1));
            }
        }
        // 返回替换占位符后的内容
        return content;
    }

    /**
     * 根据模板类型选择列表
     *
     * @param templateId 模板类型，用于筛选消息模板
     * @return ResponseResult 包含消息模板列表的响应结果
     */
    @Override
    public ResponseResult selectListByTemplateId(Integer templateId) {
        // 调用mapper方法获取指定类型的模板列表
        BaseMessageTemplateVO baseMessageTemplateVO = baseMapper.selectListByTemplateId(templateId);
        // 返回包含模板列表的响应结果
        return ResponseResult.success(baseMessageTemplateVO);
    }
}
