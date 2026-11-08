package com.houjiang.merchant.common.plugin.notice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.notice.NoticeSetting;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.notice.NoticeSettingMapper;
import com.houjiang.merchant.common.plugin.notice.engine.SmsNoticeHandle;
import com.houjiang.merchant.common.plugin.notice.template.SmsTemplate;
import com.houjiang.merchant.common.plugin.notice.vo.NoticeSmsVo;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;

/**
 * 通知驱动
 * @author Amos
 */
public class NoticeDriver {

    public static void handle(NoticeSmsVo noticeSmsVo) {
        // 场景模板
        NoticeSettingMapper noticeSettingMapper = SpringUtils.getBean(NoticeSettingMapper.class);
        NoticeSetting noticeSetting = noticeSettingMapper.selectOne(
                new QueryWrapper<NoticeSetting>()
                        .eq("scene_id", noticeSmsVo.getScene())
                        .last("limit 1"));

        if (StringUtils.isNull(noticeSetting)) {
            throw new OperateException("消息场景不存在!");
        }

        // 短信通知x
        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setName(noticeSetting.getSceneName());
        smsTemplate.setType(noticeSetting.getType());
        smsTemplate.setParams(noticeSetting.getSmsNotice());
        if (StringUtils.isNotNull(smsTemplate.getStatus()) && smsTemplate.getStatus().equals(1)) {
            (new SmsNoticeHandle()).send(noticeSmsVo, smsTemplate);
        }
    }

}
