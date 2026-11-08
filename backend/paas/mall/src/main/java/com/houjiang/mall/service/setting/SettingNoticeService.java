package com.houjiang.mall.service.setting;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 通知设置 Service。
 * 对应 like-admin SettingNoticeService。
 *
 * @author Amos
 */
public interface SettingNoticeService {

    /**
     * 通知设置列表。
     *
     * @param recipient 接收人员: 1=用户, 2=平台
     */
    List<Map<String, Object>> list(Integer recipient);

    /**
     * 通知设置详情。
     */
    Map<String, Object> detail(Integer id);

    /**
     * 通知设置保存。
     */
    void save(Map<String, Object> params);
}
