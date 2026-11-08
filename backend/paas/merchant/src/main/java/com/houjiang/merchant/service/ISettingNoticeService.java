package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.vo.setting.SettingNoticeDetailVo;
import com.houjiang.merchant.vo.setting.SettingNoticeListedVo;

import java.util.List;

/**
 * 通知设置服务接口类
 */
public interface ISettingNoticeService {

    /**
     * 通知设置列表
     *
     * @author Amos
     * @param recipient 1=用户, 2=平台
     * @return List<NoticeSettingListVo>
     */
    List<SettingNoticeListedVo> list(Integer recipient);

    /**
     * 通知设置详情
     *
     * @author Amos
     * @param id 主键
     * @return NoticeDetailVo
     */
    SettingNoticeDetailVo detail(Integer id);

    /**
     * 通知设置保存
     *
     * @author Amos
     * @param params 参数
     */
    void save(JSONObject params);

}
