package com.houjiang.merchant.common.plugin.notice.engine;

import com.houjiang.merchant.common.entity.notice.NoticeRecord;
import com.houjiang.merchant.common.entity.smsLog.SmsLog;
import com.houjiang.merchant.common.enums.NoticeEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.notice.NoticeRecordMapper;
import com.houjiang.merchant.common.mapper.smsLog.SmsLogMapper;
import com.houjiang.merchant.common.plugin.notice.vo.NoticeSmsVo;
import com.houjiang.merchant.common.plugin.notice.template.SmsTemplate;
import com.houjiang.merchant.common.plugin.sms.SmsDriver;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;

import java.util.*;

/**
 * 短信通知
 */
public class SmsNoticeHandle {

    /**
     * 发送短信通知
     *
     * @author Amos
     * @param noticeSmsVo  基础配置
     * @param smsTemplate   短信模板
     */
    public void send(NoticeSmsVo noticeSmsVo, SmsTemplate smsTemplate) {
        // 基础参数
        String mobile = noticeSmsVo.getMobile();
        Integer scene = noticeSmsVo.getScene();

        // 模板参数
        Map<String, String> params = new LinkedHashMap<>();
        if (StringUtils.isNotNull(noticeSmsVo.getParams())) {
            for (String s : noticeSmsVo.getParams()) {
                String[] arr =  s.split(":");
                String key = arr[0].trim();
                String val = arr[1].trim();
                params.put(key, val);
            }
        }

        // 消息记录
        NoticeRecordMapper noticeRecordMapper = SpringUtils.getBean(NoticeRecordMapper.class);
        int expire = StringUtils.isNull(noticeSmsVo.getExpire()) ? 0 : noticeSmsVo.getExpire();
        NoticeRecord noticeRecord = new NoticeRecord();
        noticeRecord.setSceneId(scene);
        noticeRecord.setUserId(0);
        noticeRecord.setTitle(smsTemplate.getName());
        noticeRecord.setContent(this.getContent(params, smsTemplate.getContent()));
        noticeRecord.setRecipient(NoticeEnum.SENDER_SMS.getCode());
        noticeRecord.setRead(NoticeEnum.VIEW_UNREAD.getCode());
        noticeRecord.setCreateTime(System.currentTimeMillis() / 1000);
        noticeRecord.setUpdateTime(System.currentTimeMillis() / 1000);
        noticeRecord.setNoticeType(NoticeEnum.SENDER_SMS.getCode());
        noticeRecordMapper.insert(noticeRecord);

        //记录到sms log
        SmsLogMapper smsLogMapper = SpringUtils.getBean(SmsLogMapper.class);
        SmsLog smsLog = new SmsLog();
        smsLog.setSceneId(noticeSmsVo.getScene());
        smsLog.setMobile(noticeSmsVo.getMobile());
        smsLog.setContent(this.getContent(params, smsTemplate.getContent()));
        smsLog.setCode(noticeSmsVo.getCode());

        // 消息发送
        if (StringUtils.isNotEmpty(mobile)) {
            try {
                (new SmsDriver())
                        .setMobile(mobile)
                        .setTemplateCode(smsTemplate.getTemplateId())
                        .setTemplateParam(this.getSmsParams(params, smsTemplate.getContent()))
                        .sendSms();

                noticeRecord.setUpdateTime(System.currentTimeMillis() / 1000);
                noticeRecordMapper.updateById(noticeRecord);

                //记录到log
                smsLog.setIsVerify(0);
                smsLog.setCheckNum(0);
                smsLog.setSendStatus(1);
                smsLog.setSendTime(System.currentTimeMillis() / 1000);
                smsLog.setCreateTime(System.currentTimeMillis() / 1000);
                smsLog.setUpdateTime(System.currentTimeMillis() / 1000);
                smsLogMapper.insert(smsLog);

            } catch (OperateException e) {
                noticeRecord.setUpdateTime(System.currentTimeMillis() / 1000);
                noticeRecordMapper.updateById(noticeRecord);

                //记录到log
                smsLog.setIsVerify(0);
                smsLog.setCheckNum(0);
                smsLog.setSendStatus(2);
                smsLog.setResults(e.getMsg());
                smsLog.setSendTime(System.currentTimeMillis() / 1000);
                smsLog.setCreateTime(System.currentTimeMillis() / 1000);
                smsLog.setUpdateTime(System.currentTimeMillis() / 1000);
                smsLogMapper.insert(smsLog);

                throw new OperateException(e.getMsg());
            }
        }
    }

    /**
     * 获取短信内容
     *
     * @author Amos
     * @param params 短信参数
     * @param content 短信模板
     * @return String 短信内容
     */
    private String getContent(Map<String, String> params, String content) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String searchReplace = "\\$\\{" + entry.getKey() + "}";
            content = content.replaceAll(searchReplace, entry.getValue());
        }

        return content;
    }

    /**
     * 腾讯云参数处理
     *
     * @author Amos
     * @param params 短信参数
     * @param content 短信内容
     * @return Map<String, String>
     */
    private Map<String, String> getSmsParams(Map<String, String> params, String content) {
        String engine = ConfigUtils.get("sms", "engine", "");
        if (!engine.equals("tencent")) {
            return params;
        }

        // 获取内容变量
        List<String> arr = new LinkedList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String search = "\\$\\{" + entry.getKey() + "}";
           if (content.indexOf(search) != 1 && !arr.contains(entry.getKey())) {
               arr.add(entry.getKey());
           }
        }

        // 获取变量名称
        List<Integer> arrIndex = new LinkedList<>();
        Map<Integer, String> arr2 = new LinkedHashMap<>();
        if (arr.size() > 0) {
            for (String v: arr) {
                int k = content.indexOf(v);
                arrIndex.add(k);
                arr2.put(k, v);
            }
        }

        // 从小到大排序
        List<String> arr3 = new LinkedList<>();
        Collections.sort(arrIndex);
        for (Integer i : arrIndex) {
            arr3.add(arr2.get(i));
        }

        // 取变量对应值
        Map<String, String> arr4 = new LinkedHashMap<>();
        for (String v : arr3) {
            if (StringUtils.isNotNull(params.get(v))) {
                arr4.put(params.get(v), "");
            }
        }

        return arr4;
    }

}
