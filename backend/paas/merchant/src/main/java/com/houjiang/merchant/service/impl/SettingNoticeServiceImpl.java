package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.service.ISettingNoticeService;
import com.houjiang.merchant.vo.setting.SettingNoticeDetailVo;
import com.houjiang.merchant.vo.setting.SettingNoticeListedVo;
import com.houjiang.merchant.common.entity.notice.NoticeSetting;
import com.houjiang.merchant.common.mapper.notice.NoticeSettingMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 通知设置服务实现类
 */
@Service
public class SettingNoticeServiceImpl implements ISettingNoticeService {

    @Resource
    NoticeSettingMapper noticeSettingMapper;

    /**
     * 通知设置列表
     *
     * @author Amos
     * @param recipient 1=用户, 2=平台
     * @return List<NoticeSettingListVo>
     */
    @Override
    public List<SettingNoticeListedVo> list(Integer recipient) {
        QueryWrapper<NoticeSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recipient", recipient);
        queryWrapper.orderByAsc("id");

        List<NoticeSetting> noticeSettings = noticeSettingMapper.selectList(queryWrapper);
        List<SettingNoticeListedVo> list = new LinkedList<>();
        for (NoticeSetting n : noticeSettings) {
            SettingNoticeListedVo vo = new SettingNoticeListedVo();
            BeanUtils.copyProperties(n, vo);
            JSONObject systemNotice = JSONObject.parse(n.getSystemNotice());
            JSONObject smsNotice = JSONObject.parse(n.getSmsNotice());
            JSONObject oaNotice = JSONObject.parse(n.getOaNotice());
            JSONObject mnpNotice = JSONObject.parse(n.getMnpNotice());
            if (systemNotice != null && "1".equals(systemNotice.getString("status"))) {
                vo.setSmsNotice(systemNotice);
            }

            if (smsNotice != null && "1".equals(smsNotice.getString("status"))) {
                vo.setSmsNotice(smsNotice);
            }

            if (oaNotice != null && "1".equals(oaNotice.getString("status"))) {
                vo.setSmsNotice(oaNotice);
            }

            if (mnpNotice != null && "1".equals(mnpNotice.getString("status"))) {
                vo.setSmsNotice(mnpNotice);
            }
            vo.setTypeDesc(n.getType()==1?"业务通知":"验证码");
            list.add(vo);
        }

        return list;
    }

    /**
     * 通知设置详情
     *
     * @author Amos
     * @param id 主键
     * @return NoticeSettingDetailVo
     *
     */
    @Override
    public SettingNoticeDetailVo detail(Integer id) {
        NoticeSetting noticeSetting = noticeSettingMapper.selectOne(new QueryWrapper<NoticeSetting>()
                .select(NoticeSetting.class, info ->
                        !info.getColumn().equals("update_time")
                )
                .eq("id", id)
                .last("limit 1"));


        SettingNoticeDetailVo vo = new SettingNoticeDetailVo();
        BeanUtils.copyProperties(noticeSetting, vo);
        vo.setType(noticeSetting.getType().equals(1)?"业务通知":"验证码");
        vo.setSystemNotice(JSONObject.parse(noticeSetting.getSystemNotice()));
        vo.setOaNotice(JSONObject.parse(noticeSetting.getOaNotice()));
        vo.setMnpNotice(JSONObject.parse(noticeSetting.getMnpNotice()));
        vo.setSmsNotice(JSONObject.parse(noticeSetting.getSmsNotice()));
        return vo;
    }

    /**
     * 通知设置保存
     *
     * @author Amos
     * @param params 参数
     */
    @Override
    public void save(JSONObject params) {
        Integer id = Integer.parseInt(params.get("id").toString());
        NoticeSetting noticeSetting = noticeSettingMapper.selectOne(new QueryWrapper<NoticeSetting>()
                .eq("id", id)
                .last("limit 1"));

        JSONObject template = params.getJSONObject("template");
        JSONObject mnp_notice = template.getJSONObject("mnp_notice");
        JSONObject oa_notice = template.getJSONObject("oa_notice");
        JSONObject sms_notice = template.getJSONObject("sms_notice");
        JSONObject system_notice = template.getJSONObject("system_notice");

        noticeSetting.setSystemNotice(system_notice.toJSONString());
        noticeSetting.setSmsNotice(sms_notice.toJSONString());
        noticeSetting.setOaNotice(oa_notice.toJSONString());
        noticeSetting.setMnpNotice(mnp_notice.toJSONString());
        noticeSetting.setUpdateTime(System.currentTimeMillis() / 1000);
        noticeSettingMapper.updateById(noticeSetting);
    }

}
