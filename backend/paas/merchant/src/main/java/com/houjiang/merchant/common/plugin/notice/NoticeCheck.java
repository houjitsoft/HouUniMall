package com.houjiang.merchant.common.plugin.notice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.notice.NoticeRecord;
import com.houjiang.merchant.common.entity.server.Sys;
import com.houjiang.merchant.common.entity.smsLog.SmsLog;
import com.houjiang.merchant.common.enums.NoticeEnum;
import com.houjiang.merchant.common.enums.SmsEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.notice.NoticeRecordMapper;
import com.houjiang.merchant.common.mapper.smsLog.SmsLogMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;

import jakarta.annotation.Resource;

/**
 * 通知验证码验证器
 * @author Amos
 */
public class NoticeCheck {

    public static Boolean verify(Integer sceneId, String code, String mobile) {

        SmsLogMapper smsLogMapper = SpringUtils.getBean(SmsLogMapper.class);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("mobile", mobile);
        queryWrapper.eq("send_status", SmsEnum.SEND_SUCCESS.getCode());
        queryWrapper.in("scene_id", NoticeEnum.getSmsScene());
        queryWrapper.eq("is_verify", YesNoEnum.NO.getCode());
        if (sceneId.equals(0) == false) {
            queryWrapper.eq("scene_id", sceneId);
        }
        queryWrapper.orderByDesc("send_time");
        queryWrapper.last("limit 1");
        SmsLog smsLog = smsLogMapper.selectOne(queryWrapper);

        if (StringUtils.isNull(smsLog)) {
            return false;
        }

        // 没有验证码 或 最新验证码已校验 或 已过期(有效期：5分钟)
        if (smsLog.getIsVerify().equals(1) || smsLog.getSendTime() < System.currentTimeMillis() / 1000 - (5*60)) {
            return false;
        }

        // 更新校验状态
        smsLog.setCheckNum(smsLog.getCheckNum() + 1);
        if (smsLog.getCode().equals(code)) {
            smsLog.setIsVerify(YesNoEnum.YES.getCode());
            smsLogMapper.updateById(smsLog);
            return true;
        } else {
            smsLogMapper.updateById(smsLog);
            return false;
        }
    }

}
